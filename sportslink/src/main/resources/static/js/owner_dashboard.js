// ===============================
// OWNER DASHBOARD JS
// ===============================

// OwnerId passa a vir do JWT (inicialmente null)
let ownerId = null;

// Elements
const facilitiesGrid = document.getElementById("facilities-grid");
const noFacilitiesDiv = document.getElementById("no-facilities");
const btnAddFacility = document.getElementById("btn-open-add-facility");

// Helper para mensagens
function notify(message, kind = 'info') {
    if (typeof showBootstrapToast === 'function') {
        showBootstrapToast(message, kind);
    } else {
        alert(message);
    }
}

// ==================================
// LOAD OWNER + FACILITIES ON PAGE LOAD
// ==================================
document.addEventListener("DOMContentLoaded", async () => {
    // Verificar se há token
    const token = getToken();
    if (!token) {
        window.location.href = '/index.html';
        return;
    }

    // Buscar perfil para obter ownerId e validar role OWNER
    try {
        const res = await fetch('/api/auth/profile', {
            headers: authHeaders()
        });

        if (!res.ok) {
            if (res.status === 401) {
                // Token inválido/expirado
                logout();
                return;
            }
            throw new Error('Não foi possível carregar o perfil.');
        }

        const user = await res.json();
        const ownerDashboardContent = document.getElementById('owner-dashboard-content');
        const becomeOwnerSection = document.getElementById('become-owner-section');

        if (user.role !== 'OWNER') {
            // Se não for OWNER, mostrar secção de "Tornar-se Owner"
            if (becomeOwnerSection) becomeOwnerSection.style.display = 'block';
            if (ownerDashboardContent) ownerDashboardContent.style.display = 'none';
            return;
        }

        // Se for OWNER, mostrar dashboard
        if (becomeOwnerSection) becomeOwnerSection.style.display = 'none';
        if (ownerDashboardContent) ownerDashboardContent.style.display = 'block';

        ownerId = user.id;

    } catch (error) {
        console.error(error);
        notify("Erro ao carregar o perfil do proprietário.", 'danger');
        return;
    }

    // Só depois de ter ownerId é que carregamos as instalações
    await loadFacilities();
});


// ==================================
// PAGINATION STATE
// ==================================
let allFacilities = [];
let currentPage = 1;
const itemsPerPage = 6; // 6 items per page (3x2 grid)

// ==================================
// FETCH FACILITIES FROM BACKEND
// ==================================
async function loadFacilities() {
    if (!facilitiesGrid) return;

    // Clear current state
    allFacilities = [];
    currentPage = 1;
    facilitiesGrid.innerHTML = "";

    // Hide empty state
    if (noFacilitiesDiv) noFacilitiesDiv.style.display = "none";

    try {
        const response = await fetch(`/api/owner/${ownerId}/facilities`, {
            headers: authHeaders()
        });

        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return;
            }
            throw new Error("Erro ao carregar instalações.");
        }

        const facilities = await response.json();

        if (!facilities || facilities.length === 0) {
            if (noFacilitiesDiv) noFacilitiesDiv.style.display = "block";
            facilitiesGrid.style.display = "none";
            return;
        }

        // Store all facilities
        allFacilities = facilities;

        // Initial render
        renderPage();
        renderPagination();

        if (noFacilitiesDiv) noFacilitiesDiv.style.display = "none";
        facilitiesGrid.style.display = "flex";

    } catch (error) {
        console.error(error);
        notify("Não foi possível carregar os seus campos.", 'danger');
    }
}

function renderPage() {
    if (!facilitiesGrid) return;
    facilitiesGrid.innerHTML = "";

    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const pageItems = allFacilities.slice(startIndex, endIndex);

    pageItems.forEach(facility => {
        facilitiesGrid.appendChild(createFacilityCard(facility));
    });
}

function renderPagination() {
    const totalPages = Math.ceil(allFacilities.length / itemsPerPage);
    const paginationControls = document.getElementById('paginationControls');
    if (!paginationControls) return;

    paginationControls.innerHTML = '';

    // If only 1 page, maybe hide? But user asked for pagination. We keep it if > 1 page usually.
    if (totalPages <= 1) return;

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
    nextLi.className = `page-item ${currentPage === totalPages ? 'disabled' : ''}`;
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


// ==================================
// RENDER FACILITY CARD
// ==================================
function createFacilityCard(facility) {

    const col = document.createElement("div");
    col.className = "col-md-6 col-lg-4";

    const sportIcons = {
        'FOOTBALL': 'sports_soccer',
        'PADEL': 'sports_tennis',
        'TENNIS': 'sports_tennis',
        'BASKETBALL': 'sports_basketball',
        'VOLLEYBALL': 'sports_volleyball',
        'SWIMMING': 'pool'
    };

    // Determine main sport for icon
    let mainSport = 'SPORTS';
    if (Array.isArray(facility.sports) && facility.sports.length > 0) {
        mainSport = facility.sports[0];
    }
    const icon = sportIcons[mainSport] || 'sports';

    // Updated card content to support images with icon fallback
    let imageContent;
    if (facility.imageUrl) {
        imageContent = `<img src="${facility.imageUrl}" class="w-100 h-100 object-fit-cover" alt="${facility.name}" onerror="this.parentElement.innerHTML='<div class=\\'w-100 h-100 d-flex align-items-center justify-content-center bg-light\\'><i class=\\'material-icons text-muted opacity-50 icon-xlarge\\' style=\\'font-size: 64px;\\'>${icon}</i></div>'">`;
    } else {
        imageContent = `<div class="w-100 h-100 d-flex align-items-center justify-content-center bg-light">
                          <i class="material-icons text-muted opacity-50 icon-xlarge" style="font-size: 64px;">${icon}</i>
                        </div>`;
    }

    const sportsList = Array.isArray(facility.sports) ? facility.sports.join(", ") : "";

    col.innerHTML = `
        <div class="card shadow-sm p-0 border-0 h-100" style="border-radius: 16px; overflow: hidden;">
            <div style="height: 180px; overflow: hidden; background-color: #f0f0f0;">
                ${imageContent}
            </div>
            <div class="card-body p-4">

                <h4 class="fw-bold mb-2">${facility.name}</h4>

                <p class="text-muted mb-1">
                    <i class="material-icons text-accent align-middle me-1">location_on</i>
                    ${facility.city}
                </p>

                <p class="text-muted mb-2">
                    <i class="material-icons text-accent align-middle me-1">sports</i>
                    ${sportsList}
                </p>

                <div class="d-flex gap-2 mt-3 flex-wrap">

                    <button class="btn bg-accent text-white fw-bold px-3"
                            style="border-radius: 50px;"
                            onclick="openEquipmentPage(${facility.id})">
                        <i class="material-icons align-middle me-1">construction</i>
                        Equipamentos
                    </button>

                </div>

            </div>
        </div>
    `;

    return col;
}


// ==================================
// NAVIGATE TO EQUIPMENT PAGE
// ==================================
function openEquipmentPage(facilityId) {
    window.location.href = `owner_equipments.html?facilityId=${facilityId}`;
}


// ==================================
// OPEN ADD FACILITY MODAL
// ==================================
if (btnAddFacility) {
    btnAddFacility.addEventListener("click", () => {
        const modalElement = document.getElementById("addFacilityModal");
        if (!modalElement) return;
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
    });
}


// ==================================
// CREATE FACILITY
// ==================================
const btnConfirmAddFacility = document.getElementById("btnConfirmAddFacility");
if (btnConfirmAddFacility) {
    btnConfirmAddFacility.addEventListener("click", async () => {
        await createFacility();
    });
}

async function createFacility() {
    if (!ownerId) {
        notify("Utilizador não identificado como proprietário.", 'danger');
        return;
    }

    const name = document.getElementById("facilityName").value.trim();
    const city = document.getElementById("facilityCity").value.trim();
    const address = document.getElementById("facilityAddress").value.trim();
    const description = document.getElementById("facilityDescription").value.trim();
    const price = parseFloat(document.getElementById("facilityPrice").value);
    const opening = document.getElementById("facilityOpening").value;
    const closing = document.getElementById("facilityClosing").value;
    const imageInput = document.getElementById("facilityImage");
    const imageFile = imageInput.files[0];

    const selectedSports = Array.from(document.querySelectorAll(".facility-sport-check:checked"))
        .map(cb => cb.value);

    // Simple validation
    if (!name || !city || !address || !price || !opening || !closing || selectedSports.length === 0) {
        notify("Por favor preencha todos os campos obrigatórios.", 'warning');
        return;
    }

    const facilityData = {
        name,
        city,
        address,
        description,
        pricePerHour: price,
        openingTime: opening,
        closingTime: closing,
        sports: selectedSports
    };

    const formData = new FormData();
    formData.append('facility', new Blob([JSON.stringify(facilityData)], { type: 'application/json' }));
    if (imageFile) {
        formData.append('image', imageFile);
    }

    try {
        // Headers: Auth only. Content-Type is set automatically by browser with boundary.
        const headers = authHeaders();
        delete headers['Content-Type']; // Ensure we don't send application/json

        const response = await fetch(`/api/owner/${ownerId}/facilities`, {
            method: "POST",
            headers: headers,
            body: formData
        });

        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return;
            }
            throw new Error("Erro ao criar campo");
        }

        const modalElement = document.getElementById("addFacilityModal");
        if (modalElement) {
            const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
            modal.hide();
        }

        const form = document.getElementById("addFacilityForm");
        if (form) form.reset();

        await loadFacilities();

        notify("Campo criado com sucesso!", 'success');

    } catch (error) {
        console.error(error);
        notify("Não foi possível criar o campo.", 'danger');
    }
}
