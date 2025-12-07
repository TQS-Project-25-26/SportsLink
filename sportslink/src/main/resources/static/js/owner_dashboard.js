// ===============================
// OWNER DASHBOARD JS
// ===============================

// OwnerId passa a vir do JWT (inicialmente null)
let ownerId = null;
let editingFacilityId = null; // Track if we are editing or creating

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

        // Check for URL parameters to auto-open modals
        const urlParams = new URLSearchParams(window.location.search);
        const action = urlParams.get('action');
        const facilityIdParam = urlParams.get('facilityId');

        if (action && facilityIdParam) {
            const fId = parseInt(facilityIdParam);
            if (action === 'edit') {
                openEditModal(fId);
            } else if (action === 'equipment') {
                openEquipmentPage(fId);
            }

            // Clean URL
            const url = new URL(window.location);
            url.searchParams.delete('action');
            url.searchParams.delete('facilityId');
            window.history.replaceState({}, document.title, url);
        }

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
        <div class="card shadow-sm p-0 border-0 h-100" style="border-radius: 16px; overflow: hidden; position: relative;">
            
            <!-- OVERLAY ACTIONS -->
            <div class="position-absolute top-0 end-0 p-3 d-flex gap-2" style="z-index: 10;">
                <button class="btn shadow-sm d-flex align-items-center justify-content-center"
                        style="width: 40px; height: 40px; padding: 0;"
                        onclick="openEditModal(${facility.id})"
                        title="Edit Facility">
                    <i class="material-icons text-white" style="font-size: 1.2rem;">edit</i>
                </button>
                <button class="btn shadow-sm d-flex align-items-center justify-content-center"
                        style="width: 40px; height: 40px; padding: 0;"
                        onclick="deleteFacility(${facility.id})"
                        title="Delete Facility">
                    <i class="material-icons text-white" style="font-size: 1.2rem;">delete</i>
                </button>
            </div>

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

                <div class="d-flex gap-2 mt-3 flex-wrap align-items-center">
                    
                    <button class="btn bg-accent text-white fw-bold px-3"
                            style="border-radius: 50px;"
                            onclick="openEquipmentPage(${facility.id})">
                        <i class="material-icons align-middle me-1">construction</i>
                        Equipments
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
// ==================================
// OPEN ADD FACILITY MODAL
// ==================================
if (btnAddFacility) {
    btnAddFacility.addEventListener("click", () => {
        openAddModal();
    });
}

function openAddModal() {
    editingFacilityId = null; // Reset edit mode

    // Clear form
    const form = document.getElementById("addFacilityForm");
    if (form) form.reset();

    // Update title and button
    document.getElementById("addFacilityLabel").innerHTML = `
        <i class="material-icons me-2 text-accent">add_circle</i>
        Add New Facility
    `;
    document.getElementById("btnConfirmAddFacility").innerText = "Save Facility";

    // Show image input (allowed for creation)
    const imageInput = document.getElementById("facilityImage");
    if (imageInput && imageInput.parentElement) imageInput.parentElement.style.display = "block";

    const modalElement = document.getElementById("addFacilityModal");
    if (!modalElement) return;
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}

function openEditModal(facilityId) {
    const facility = allFacilities.find(f => f.id === facilityId);
    if (!facility) return;

    editingFacilityId = facilityId;

    // Populate form
    document.getElementById("facilityName").value = facility.name;
    document.getElementById("facilityCity").value = facility.city;
    document.getElementById("facilityAddress").value = facility.address;
    document.getElementById("facilityDescription").value = facility.description || "";
    document.getElementById("facilityPrice").value = facility.pricePerHour;
    document.getElementById("facilityOpening").value = facility.openingTime;
    document.getElementById("facilityClosing").value = facility.closingTime;

    // Set sports
    document.querySelectorAll(".facility-sport-check").forEach(cb => {
        cb.checked = facility.sports && facility.sports.includes(cb.value);
    });

    // Update title and button
    document.getElementById("addFacilityLabel").innerHTML = `
        <i class="material-icons me-2 text-accent">edit</i>
        Edit Facility
    `;
    document.getElementById("btnConfirmAddFacility").innerText = "Update Facility";

    // Hide image input (not supported for update via JSON)
    const imageInput = document.getElementById("facilityImage");
    if (imageInput && imageInput.parentElement) imageInput.parentElement.style.display = "none";

    const modalElement = document.getElementById("addFacilityModal");
    if (!modalElement) return;
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}


// ==================================
// CREATE FACILITY
// ==================================
// ==================================
// CREATE OR UPDATE FACILITY
// ==================================
const btnConfirmAddFacility = document.getElementById("btnConfirmAddFacility");
if (btnConfirmAddFacility) {
    btnConfirmAddFacility.addEventListener("click", async () => {
        await saveFacility();
    });
}

async function saveFacility() {
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

    const selectedSports = Array.from(document.querySelectorAll(".facility-sport-check:checked"))
        .map(cb => cb.value);

    // Simple validation
    if (!name || !city || !address || isNaN(price) || !opening || !closing || selectedSports.length === 0) {
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

    try {
        let response;

        if (editingFacilityId) {
            // UDPATE (PUT)
            response = await fetch(`/api/owner/${ownerId}/facilities/${editingFacilityId}`, {
                method: "PUT",
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${getToken()}`
                },
                body: JSON.stringify(facilityData)
            });
        } else {
            // CREATE (POST with FormData)
            const imageInput = document.getElementById("facilityImage");
            const imageFile = imageInput.files[0];

            const formData = new FormData();
            formData.append('facility', new Blob([JSON.stringify(facilityData)], { type: 'application/json' }));
            if (imageFile) {
                formData.append('image', imageFile);
            }

            const headers = authHeaders();
            delete headers['Content-Type']; // Let browser set boundary

            response = await fetch(`/api/owner/${ownerId}/facilities`, {
                method: "POST",
                headers: headers,
                body: formData
            });
        }

        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return;
            }
            throw new Error("Erro ao salvar campo");
        }

        const modalElement = document.getElementById("addFacilityModal");
        if (modalElement) {
            const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
            modal.hide();
        }

        const form = document.getElementById("addFacilityForm");
        if (form) form.reset();

        await loadFacilities();

        notify(editingFacilityId ? "Campo atualizado com sucesso!" : "Campo criado com sucesso!", 'success');
        editingFacilityId = null; // Reset

    } catch (error) {
        console.error(error);
        notify("Não foi possível salvar o campo.", 'danger');
    }
}

// Old createFacility function removed as it is merged into saveFacility
function unused_createFacility() {
    // Placeholder to match previous structure if needed for tools, but I replaced the whole block.
}

// ==================================
// DELETE FACILITY
// ==================================
async function deleteFacility(facilityId) {
    if (!confirm("Are you sure you want to delete this facility? This action cannot be undone immediately.")) {
        return;
    }

    try {
        const response = await fetch(`/api/owner/${ownerId}/facilities/${facilityId}`, {
            method: "DELETE",
            headers: authHeaders()
        });

        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return;
            }
            throw new Error("Erro ao eliminar campo");
        }

        notify("Campo eliminado com sucesso.", 'success');
        await loadFacilities();

    } catch (error) {
        console.error(error);
        notify("Não foi possível eliminar o campo.", 'danger');
    }
}
