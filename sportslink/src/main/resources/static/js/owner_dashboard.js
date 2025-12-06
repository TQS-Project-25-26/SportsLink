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
        if (user.role !== 'OWNER') {
            // Se não for OWNER, mandar de volta para a página principal de utilizador
            window.location.href = '/pages/main_page_user.html';
            return;
        }

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
// FETCH FACILITIES FROM BACKEND
// ==================================
async function loadFacilities() {
    if (!facilitiesGrid) return;

    facilitiesGrid.innerHTML = "";

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

        if (noFacilitiesDiv) noFacilitiesDiv.style.display = "none";
        facilitiesGrid.style.display = "flex";

        facilities.forEach(facility => {
            facilitiesGrid.appendChild(createFacilityCard(facility));
        });

    } catch (error) {
        console.error(error);
        notify("Não foi possível carregar os seus campos.", 'danger');
    }
}


// ==================================
// RENDER FACILITY CARD
// ==================================
function createFacilityCard(facility) {

    const col = document.createElement("div");
    col.className = "col-md-6 col-lg-4";

    const sportsList = Array.isArray(facility.sports) ? facility.sports.join(", ") : "";

    col.innerHTML = `
        <div class="card shadow-sm p-0 border-0" style="border-radius: 16px; overflow: hidden;">
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

    const selectedSports = Array.from(document.getElementById("facilitySports").selectedOptions)
        .map(opt => opt.value);

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

    try {
        const response = await fetch(`/api/owner/${ownerId}/facilities`, {
            method: "POST",
            headers: Object.assign({ "Content-Type": "application/json" }, authHeaders()),
            body: JSON.stringify(facilityData)
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
