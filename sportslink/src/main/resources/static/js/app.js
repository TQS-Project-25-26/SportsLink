/* App logic para main_page_user - busca, filtros, mapa, modal, favoritos */
(() => {
  const BASE = "/api/rentals";

  // Helpers
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

  const showToast = (msg, timeout = 3500) => {
    let t = document.getElementById("app-toast");
    if (!t) {
      t = document.createElement("div");
      t.id = "app-toast";
      t.className = "toast";
      document.body.appendChild(t);
    }
    t.textContent = msg;
    t.classList.add("visible");
    clearTimeout(t._hid);
    t._hid = setTimeout(() => t.classList.remove("visible"), timeout);
  };

  async function apiFetch(path, options = {}) {
    try {
      // Adicionar headers de autenticação se não estiverem já presentes
      const headers = options.headers || {};
      const allHeaders = {
        ...authHeaders(),  // Inclui Authorization e Content-Type
        ...headers
      };

      const fetchOptions = {
        ...options,
        headers: allHeaders,
        credentials: 'include'  // Incluir cookies
      };

      const res = await fetch(path, fetchOptions);

      // Se 403 Forbidden, redirecionar para login
      if (res.status === 403) {
        logout();
        return { ok: false, status: 403, body: { message: "Session expired" } };
      }

      // try to parse JSON if possible
      const contentType = res.headers.get("content-type") || "";
      const body = contentType.includes("application/json")
        ? await res.json()
        : await res.text();
      return { ok: res.ok, status: res.status, body };
    } catch (err) {
      return { ok: false, status: 0, error: err };
    }
  }

  // API functions
  async function searchFacilities({
    location,
    sport,
    startTime,
    endTime,
  } = {}) {
    const params = new URLSearchParams();
    if (location) params.set("location", location);
    if (sport) params.set("sport", sport);
    if (startTime) params.set("startTime", startTime);
    if (endTime) params.set("endTime", endTime);
    const url = `${BASE}/search?${params.toString()}`;
    return apiFetch(url);
  }

  async function getEquipments(facilityId) {
    return apiFetch(`${BASE}/facility/${facilityId}/equipments`);
  }

  async function createRental(payload) {
    return apiFetch(`${BASE}/rental`, {
      method: "POST",
      body: JSON.stringify(payload),
    });
  }

  async function updateRental(id, payload) {
    return apiFetch(`${BASE}/rental/${id}/update`, {
      method: "PUT",
      body: JSON.stringify(payload),
    });
  }

  async function cancelRental(id) {
    return apiFetch(`${BASE}/rental/${id}/cancel`, { method: "PUT" });
  }

  // UI render helpers
  function createFieldCard(field) {
    const div = document.createElement("div");
    div.className = "col";
    div.dataset.id = field.id ?? "";

    const card = document.createElement("div");
    card.className = "field-card card border-0 shadow-sm h-100";

    // Map sportType to icon
    const sportIcons = {
      'Football': 'sports_soccer',
      'Padel': 'sports_tennis',
      'Tennis': 'sports_tennis',
      'Basketball': 'sports_basketball',
      'Volleyball': 'sports_volleyball'
    };
    const icon = sportIcons[field.sportType] || 'sports';

    card.innerHTML = `
      <div class="field-image card-img-top position-relative">
        <i class="material-icons text-white opacity-50 icon-xlarge" style="font-size: 64px;">${icon}</i>
        <div class="position-absolute top-0 start-0 m-3 px-3 py-1 bg-white text-accent rounded-pill fs-7 fw-bold shadow-sm text-uppercase" style="font-size: 0.75rem; letter-spacing: 0.5px;">
          ${field.sportType || "Sport"}
        </div>
        <div class="position-absolute bottom-0 end-0 m-3 px-3 py-1 bg-accent text-white rounded-pill fw-bold shadow-sm">
           ${field.pricePerHour ? `€${field.pricePerHour}/h` : "Price N/A"}
        </div>
      </div>
      
      <button class="favorite-btn btn position-absolute top-0 end-0 m-2 rounded-circle shadow-sm" aria-label="Favoritar" style="z-index: 5;">
        <i class="material-icons">favorite_border</i>
      </button>

      <div class="card-body d-flex flex-column gap-2">
        <h5 class="field-name card-title fw-bold text-dark mb-1 text-truncate">${field.name || "Unnamed Facility"
      }</h5>
        
        <div class="field-location d-flex align-items-center gap-1 text-muted small mb-3">
          <i class="material-icons icon-small text-accent">location_on</i> 
          <span class="text-truncate">${field.city || field.address || "Location N/A"}</span>
        </div>

        <div class="mt-auto d-flex gap-2">
          <button class="btn btn-sm btn-outline-dark btn-equip flex-fill">Equipamentos</button>
          <button class="btn btn-sm btn-primary btn-rent flex-fill text-white">Reservar</button>
        </div>
      </div>
    `;

    // attach handlers
    const favoriteBtn = card.querySelector(".favorite-btn");
    favoriteBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      const icon = favoriteBtn.querySelector(".material-icons");
      favoriteBtn.classList.toggle("liked");
      icon.textContent = favoriteBtn.classList.contains("liked") ? "favorite" : "favorite_border";
    });

    card.querySelector(".btn-equip").addEventListener("click", async (e) => {
      e.stopPropagation();
      const fid = div.dataset.id;
      window.location.href = `equipments.html?facilityId=${fid}`;
    });
    card.querySelector(".btn-rent").addEventListener("click", async (e) => {
      e.stopPropagation();
      const fid = div.dataset.id;
      window.location.href = `field_detail.html?id=${fid}`;
    });

    // Click on card to view details
    card.addEventListener("click", (e) => {
      if (!e.target.closest('button')) {
        window.location.href = `field_detail.html?id=${field.id}`;
      }
    });

    div.appendChild(card);
    return div;
  }

  // Pagination state
  let allFacilities = [];
  let currentPage = 1;
  const itemsPerPage = 8; // 8 items per page (4x2 grid)

  function renderPagination() {
    const totalPages = Math.ceil(allFacilities.length / itemsPerPage);
    const paginationControls = document.getElementById('paginationControls');
    if (!paginationControls) return;

    // Ensure styles
    paginationControls.className = 'pagination';
    // Wrap in container if not already
    let wrapper = paginationControls.parentElement;
    if (!wrapper.classList.contains('pagination-container')) {
      wrapper.classList.add('pagination-container');
    }

    paginationControls.innerHTML = '';

    // Previous button
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPage === 1 ? 'disabled' : ''}`;
    prevLi.innerHTML = `<a class="page-link" href="#" aria-label="Previous"><span aria-hidden="true">&laquo;</span></a>`;
    prevLi.addEventListener('click', (e) => {
      e.preventDefault();
      if (currentPage > 1) {
        currentPage--;
        renderPage();
        renderPagination();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
    paginationControls.appendChild(prevLi);

    // Page numbers
    for (let i = 1; i <= totalPages; i++) {
      const li = document.createElement('li');
      li.className = `page-item ${i === currentPage ? 'active' : ''}`;
      li.innerHTML = `<a class="page-link" href="#">${i}</a>`;
      li.addEventListener('click', (e) => {
        e.preventDefault();
        currentPage = i;
        renderPage();
        renderPagination();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      });
      paginationControls.appendChild(li);
    }

    // Next button
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${currentPage === totalPages || totalPages === 0 ? 'disabled' : ''}`;
    nextLi.innerHTML = `<a class="page-link" href="#" aria-label="Next"><span aria-hidden="true">&raquo;</span></a>`;
    nextLi.addEventListener('click', (e) => {
      e.preventDefault();
      if (currentPage < totalPages) {
        currentPage++;
        renderPage();
        renderPagination();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
    paginationControls.appendChild(nextLi);
  }

  function renderPage() {
    const container = document.querySelector('#featured');
    if (!container) return;

    // Clear but preserve grid classes
    container.className = 'row row-cols-2 row-cols-md-4 g-3 g-md-4';
    container.innerHTML = '';

    if (allFacilities.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'col text-muted';
      empty.textContent = 'Nenhum resultado encontrado.';
      container.appendChild(empty);
      return;
    }

    const startIdx = (currentPage - 1) * itemsPerPage;
    const endIdx = startIdx + itemsPerPage;
    const pageData = allFacilities.slice(startIdx, endIdx);

    pageData.forEach(f => container.appendChild(createFieldCard(f)));
  }

  async function renderSearchResults(data = []) {
    allFacilities = Array.isArray(data) ? data : [];
    currentPage = 1;
    renderPage();
    renderPagination();
  }

  // Render nearby facilities in carousel
  function renderNearbyCarousel(data = []) {
    const container = document.querySelector('#nearbyCarousel .carousel-item .d-flex');
    if (!container) return;

    container.innerHTML = '';
    const facilities = Array.isArray(data) ? data : [];

    if (facilities.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'text-muted';
      empty.textContent = 'Nenhuma facilidade próxima encontrada.';
      container.appendChild(empty);
      return;
    }

    // For carousel, show max 3 items
    facilities.slice(0, 3).forEach(f => container.appendChild(createFieldCard(f)));
  }

  // Simple equipment panel (appends to body)
  async function handleShowEquipments(facilityId, field = {}) {
    const res = await getEquipments(facilityId);
    if (!res.ok) {
      showToast(`Erro ao obter equipamentos (${res.status})`);
      return;
    }
    const list = Array.isArray(res.body) ? res.body : [];
    // create panel
    let panel = document.getElementById("equip-panel");
    if (!panel) {
      panel = document.createElement("div");
      panel.id = "equip-panel";
      panel.className = "card position-fixed p-3";
      Object.assign(panel.style, {
        right: "20px",
        top: "80px",
        width: "320px",
        zIndex: 1200,
      });
      document.body.appendChild(panel);
    }
    panel.innerHTML = `<div class="d-flex justify-content-between align-items-center mb-2">
        <strong>Equipamentos — ${field.name || ""}</strong>
        <button id="close-equip" class="btn btn-sm btn-light">Fechar</button>
      </div>
      <div id="equip-list"></div>
    `;
    document.getElementById("close-equip").onclick = () => panel.remove();
    const ul = document.getElementById("equip-list");
    if (list.length === 0) {
      ul.innerHTML =
        '<div class="text-muted">Nenhum equipamento disponível.</div>';
      return;
    }
    ul.innerHTML = list
      .map(
        (eq) => `
      <div class="d-flex justify-content-between align-items-center py-1">
        <div>
          <div class="fw-bold">${eq.name}</div>
          <small class="text-muted">${eq.description || ""}</small>
          <small class="text-muted"> | Qty: ${eq.quantity || 0}</small>
        </div>
        <div class="text-end"><small>${eq.pricePerHour ? `€${eq.pricePerHour}` : ""
          }</small></div>
      </div>
    `
      )
      .join("");
  }

  // Quick rent prompt (very minimal)
  async function handleQuickRent(facilityId, field = {}) {
    // prompt user for start/end (ISO or simple)
    const start = prompt(
      "Data/hora início (YYYY-MM-DDTHH:MM), ex: 2025-11-27T19:00"
    );
    if (!start) return;
    const end = prompt(
      "Data/hora fim (YYYY-MM-DDTHH:MM), ex: 2025-11-27T21:00"
    );
    if (!end) return;
    const equipmentIdsRaw = prompt(
      "IDs de equipamentos separados por vírgula (opcional)"
    );
    const equipmentIds = equipmentIdsRaw
      ? equipmentIdsRaw
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean)
        .map(Number)
      : [];
    const payload = {
      userId: 1, // Demo user ID
      facilityId: Number(facilityId) || Number(field.id),
      startTime: start,
      endTime: end,
      equipmentIds,
    };
    const res = await createRental(payload);
    if (!res.ok) {
      showToast(`Erro ao criar reserva (${res.status})`);
      console.error(res);
      return;
    }
    showToast("Reserva criada com sucesso");
    console.log("createRental response", res.body);
  }

  // Setup search form binding
  function bindSearch() {
    const searchBtn =
      document.getElementById("searchBtn") ||
      document.querySelector(".search-bar button") ||
      document.querySelector(".search-btn");
    const input =
      document.getElementById("searchInput") ||
      document.querySelector(".search-bar input");
    const sport =
      document.getElementById("sportFilter") ||
      document.querySelector(".search-bar select");
    if (!searchBtn || !input) return;
    searchBtn.addEventListener("click", async (e) => {
      e.preventDefault();

      // Map frontend sport names to backend values
      const sportMap = {
        'futebol': 'Football',
        'padel': 'Padel',
        'tenis': 'Tennis',
        'basquete': 'Basketball',
        'volei': 'Volleyball',
        'badminton': 'Badminton'
      };

      const sportValue = sport?.value || undefined;
      const mappedSport = sportValue ? sportMap[sportValue.toLowerCase()] || sportValue : undefined;

      const params = {
        location: input.value || undefined,
        sport: mappedSport,
      };
      showToast("A pesquisar...");
      const res = await searchFacilities(params);
      if (!res.ok) {
        showToast(`Erro ao pesquisar (${res.status})`);
        return;
      }
      // expected body may be array
      const body = Array.isArray(res.body) ? res.body : res.body?.results || [];
      renderSearchResults(body, "#featured");
      if (body.length === 0) {
        showToast("Nenhum resultado encontrado");
      } else {
        showToast(`${body.length} resultado(s) encontrado(s)`);
      }
    });
  }

  // Add after bindSearch function
  async function loadFeatured() {
    console.log('Loading featured facilities...');
    const res = await searchFacilities({}); // Empty params for all
    console.log('Featured facilities response:', res);
    if (res.ok) {
      const body = Array.isArray(res.body) ? res.body : [];
      console.log('Featured facilities data:', body);
      renderSearchResults(body, "#featured");
    }
  }

  async function loadNearby() {
    console.log('Loading nearby facilities...');
    const res = await searchFacilities({ location: "Lisboa" }); // Default location
    console.log('Nearby facilities response:', res);
    if (res.ok) {
      const body = Array.isArray(res.body) ? res.body : [];
      console.log('Nearby facilities data:', body);
      renderNearbyCarousel(body);
    }
  }

  // Update DOMContentLoaded
  document.addEventListener("DOMContentLoaded", () => {
    console.log('DOM loaded, initializing app...');
    bindSearch();
    loadFeatured();
    loadNearby();
  });
})();