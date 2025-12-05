// ===============================
// OWNER DASHBOARD JS
// ===============================

// Temporary ownerId
const ownerId = 1;

// Elements
const facilitiesGrid = document.getElementById("facilities-grid");
const noFacilitiesDiv = document.getElementById("no-facilities");
const btnAddFacility = document.getElementById("btn-open-add-facility");


// ==================================
// LOAD FACILITIES ON PAGE LOAD
// ==================================
document.addEventListener("DOMContentLoaded", () => {
    loadFacilities();
});


// ==================================
// FETCH FACILITIES FROM BACKEND
// ==================================
async function loadFacilities() {
    facilitiesGrid.innerHTML = "";

    try {
        const response = await fetch(`/api/owner/${ownerId}/facilities`);
        if (!response.ok) throw new Error("Erro ao carregar instalações.");

        const facilities = await response.json();

        if (facilities.length === 0) {
            noFacilitiesDiv.style.display = "block";
            facilitiesGrid.style.display = "none";
            return;
        }

        noFacilitiesDiv.style.display = "none";
        facilitiesGrid.style.display = "flex";

        facilities.forEach(facility => {
            facilitiesGrid.appendChild(createFacilityCard(facility));
        });

    } catch (error) {
        console.error(error);
        alert("Não foi possível carregar os seus campos.");
    }
}


// ==================================
// RENDER FACILITY CARD
// ==================================
function createFacilityCard(facility) {

    const col = document.createElement("div");
    col.className = "col-md-6 col-lg-4";

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
                    ${facility.sports.join(", ")}
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
btnAddFacility.addEventListener("click", () => {
    const modal = new bootstrap.Modal(document.getElementById("addFacilityModal"));
    modal.show();
});


// ==================================
// CREATE FACILITY
// ==================================
document.getElementById("btnConfirmAddFacility").addEventListener("click", async () => {
    await createFacility();
});

async function createFacility() {

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
        alert("Por favor preencha todos os campos obrigatórios.");
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
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(facilityData)
        });

        if (!response.ok) throw new Error("Erro ao criar campo");

        const modalElement = document.getElementById("addFacilityModal");
        const modal = bootstrap.Modal.getInstance(modalElement);
        modal.hide();

        document.getElementById("addFacilityForm").reset();

        await loadFacilities();

        alert("Campo criado com sucesso!");

    } catch (error) {
        console.error(error);
        alert("Não foi possível criar o campo.");
    }
}
