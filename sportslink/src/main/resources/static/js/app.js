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
      const res = await fetch(path, options);
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
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
  }

  async function updateRental(id, payload) {
    return apiFetch(`${BASE}/rental/${id}/update`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
  }

  async function cancelRental(id) {
    return apiFetch(`${BASE}/rental/${id}/cancel`, { method: "PUT" });
  }

  // UI render helpers
  function createFieldCard(field) {
    const div = document.createElement("div");
    div.className = "field-card card border-0 shadow-sm";
    div.style.minWidth = "300px";
    div.dataset.id = field.id ?? "";
    div.innerHTML = `
      <div class="field-image card-img-top d-flex align-items-center justify-content-center bg-gradient-orange">
        <i class="material-icons text-accent icon-large">sports_soccer</i>
      </div>
      <button class="favorite-btn btn position-absolute top-0 end-0 m-2 rounded-circle shadow-sm" aria-label="Favoritar">
        <i class="material-icons">favorite_border</i>
      </button>
      <div class="card-body">
        <div class="field-sport text-uppercase fw-bold text-accent small">${(
          field.sport || ""
        ).toString()}</div>
        <h5 class="field-name card-title fw-bold text-accent-dark">${
          field.name || field.title || "Unnamed"
        }</h5>
        <div class="field-location d-flex align-items-center gap-1 text-muted small">
          <i class="material-icons icon-small">location_on</i> ${
            field.location || field.city || ""
          }
        </div>
        <div class="field-details d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
          <div class="field-price fw-bold text-accent-dark">${
            field.price ? `€${field.price}/hora` : ""
          }</div>
          <div class="d-flex gap-2">
            <button class="btn btn-sm btn-outline-primary btn-equip">Equipamentos</button>
            <button class="btn btn-sm btn-primary btn-rent">Reservar</button>
          </div>
        </div>
      </div>
    `;
    // attach handlers
    div.querySelector(".btn-equip").addEventListener("click", async (e) => {
      e.stopPropagation();
      const fid = div.dataset.id;
      await handleShowEquipments(fid, field);
    });
    div.querySelector(".btn-rent").addEventListener("click", async (e) => {
      e.stopPropagation();
      const fid = div.dataset.id;
      await handleQuickRent(fid, field);
    });
    return div;
  }

  async function renderSearchResults(data = [], containerSelector = '#featured') {
    const container = document.querySelector(containerSelector) || document.querySelector('.carousel');
    if (!container) return;
    container.innerHTML = '';
    if (!Array.isArray(data) || data.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'text-muted';
      empty.textContent = 'Nenhum resultado encontrado.';
      container.appendChild(empty);
      return;
    }
    data.forEach(f => container.appendChild(createFieldCard(f)));
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
        </div>
        <div class="text-end"><small>${
          eq.price ? `€${eq.price}` : ""
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
      const params = {
        location: input.value || undefined,
        sport: sport?.value || undefined,
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
    });
  }

  // Add after bindSearch function
  async function loadFeatured() {
    const res = await searchFacilities({}); // Empty params for all
    if (res.ok) {
      const body = Array.isArray(res.body) ? res.body : [];
      renderSearchResults(body, "#featuredCarousel");
    }
  }

  async function loadNearby() {
    const res = await searchFacilities({ location: "Lisboa" }); // Default location
    if (res.ok) {
      const body = Array.isArray(res.body) ? res.body : [];
      renderSearchResults(body, "#nearbyCarousel");
    }
  }

  // Update DOMContentLoaded
  document.addEventListener("DOMContentLoaded", () => {
    bindSearch();
    loadFeatured();
    loadNearby();
  });
})();