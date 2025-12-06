// =======================================
// OWNER EQUIPMENTS JS
// =======================================

// Temporarily hardcoded ownerId (trocar quando houver auth real)
const ownerId = 1;

// Elements
const equipmentGrid = document.getElementById("equipment-grid");
const noEquipmentDiv = document.getElementById("no-equipment");
const loadingDiv = document.getElementById("loading");
const btnOpenAddEquipment = document.getElementById("btn-open-add-equipment");
const facilityLabel = document.getElementById("facility-label");

// Modals
let addEquipmentModal;
let editEquipmentModal;

// Cache local dos equipamentos (por id)
const equipmentCache = new Map();

// Obter facilityId da query string
const urlParams = new URLSearchParams(window.location.search);
const facilityId = urlParams.get("facilityId");

// =======================================
// ON LOAD
// =======================================
document.addEventListener("DOMContentLoaded", () => {

    if (!facilityId) {
        alert("Nenhum campo selecionado.");
        window.location.href = "owner_dashboard.html";
        return;
    }

    // Set label simples
    facilityLabel.textContent = ` (Campo #${facilityId})`;

    // Inicializar modals
    addEquipmentModal = new bootstrap.Modal(document.getElementById("addEquipmentModal"));
    editEquipmentModal = new bootstrap.Modal(document.getElementById("editEquipmentModal"));

    // Botão "Adicionar Equipamento"
    btnOpenAddEquipment.addEventListener("click", () => {
        document.getElementById("addEquipmentForm").reset();
        addEquipmentModal.show();
    });

    // Botão confirmar adicionar
    document.getElementById("btnConfirmAddEquipment")
        .addEventListener("click", async () => {
            await createEquipment();
        });

    // Botão confirmar editar
    document.getElementById("btnConfirmEditEquipment")
        .addEventListener("click", async () => {
            await updateEquipment();
        });

    // Carregar equipamentos do campo
    loadEquipments();
});


// =======================================
// LOAD EQUIPMENTS
// =======================================
async function loadEquipments() {
    equipmentGrid.innerHTML = "";
    equipmentCache.clear();

    loadingDiv.style.display = "block";
    noEquipmentDiv.style.display = "none";

    try {
        const response = await fetch(`/api/owner/${ownerId}/facilities/${facilityId}/equipment`);
        if (!response.ok) {
            throw new Error("Erro ao carregar equipamentos.");
        }

        const equipments = await response.json();

        loadingDiv.style.display = "none";

        if (!equipments || equipments.length === 0) {
            noEquipmentDiv.style.display = "block";
            return;
        }

        noEquipmentDiv.style.display = "none";

        equipments.forEach(eq => {
            equipmentCache.set(eq.id, eq);
            equipmentGrid.appendChild(createEquipmentCard(eq));
        });

    } catch (error) {
        console.error(error);
        loadingDiv.style.display = "none";
        alert("Não foi possível carregar os equipamentos.");
    }
}


// =======================================
// CREATE EQUIPMENT CARD
// =======================================
function createEquipmentCard(eq) {
    const col = document.createElement("div");
    col.className = "col-md-6 col-lg-4";

    const unavailableClass = eq.status === "UNAVAILABLE" ? "unavailable" : "";

    col.innerHTML = `
        <div class="card equipment-card ${unavailableClass} shadow-sm border-0"
             style="border-radius: 16px; overflow: hidden;">

            <div class="card-body d-flex flex-column h-100">

                <div class="d-flex justify-content-between align-items-start mb-2">
                    <h5 class="fw-bold mb-0">${eq.name}</h5>
                    <span class="badge ${
                        eq.status === "AVAILABLE"
                            ? "bg-success"
                            : eq.status === "MAINTENANCE"
                                ? "bg-warning text-dark"
                                : "bg-secondary"
                    }">
                        ${statusLabel(eq.status)}
                    </span>
                </div>

                <p class="text-muted mb-1">
                    <i class="material-icons icon-small align-middle me-1">category</i>
                    <span>${eq.type || "-"}</span>
                </p>

                <p class="text-muted small mb-2">
                    ${eq.description || "Sem descrição"}
                </p>

                <div class="d-flex justify-content-between align-items-center mt-auto pt-3 border-top">
                    <div>
                        <div class="small text-muted">Quantidade</div>
                        <strong>${eq.quantity}</strong>
                    </div>
                    <div class="text-end">
                        <div class="small text-muted">Preço/hora</div>
                        <strong>${eq.pricePerHour ? `€${eq.pricePerHour.toFixed(2)}` : "Incluído"}</strong>
                    </div>
                </div>

                <div class="d-flex gap-2 mt-3">
                    <button class="btn btn-outline-dark btn-sm flex-fill"
                            style="border-radius: 50px;"
                            onclick="openEditEquipment(${eq.id})">
                        <i class="material-icons icon-small align-middle me-1">edit</i>
                        Editar
                    </button>
                </div>

            </div>
        </div>
    `;

    return col;
}


function statusLabel(status) {
    switch (status) {
        case "AVAILABLE": return "Disponível";
        case "MAINTENANCE": return "Em manutenção";
        case "UNAVAILABLE": return "Indisponível";
        default: return status;
    }
}


// =======================================
// CREATE EQUIPMENT
// =======================================
async function createEquipment() {

    const name = document.getElementById("equipmentName").value.trim();
    const type = document.getElementById("equipmentType").value;
    const description = document.getElementById("equipmentDescription").value.trim();
    const quantity = parseInt(document.getElementById("equipmentQuantity").value, 10);
    const priceStr = document.getElementById("equipmentPrice").value;
    const status = document.getElementById("equipmentStatus").value;

    if (!name || !type || !quantity || !status) {
        alert("Por favor preencha os campos obrigatórios.");
        return;
    }

    const pricePerHour = priceStr ? parseFloat(priceStr) : null;

    const payload = {
        name,
        type,
        description,
        quantity,
        pricePerHour,
        status
    };

    try {
        const response = await fetch(`/api/owner/${ownerId}/facilities/${facilityId}/equipment`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error("Erro ao criar equipamento.");
        }

        addEquipmentModal.hide();
        document.getElementById("addEquipmentForm").reset();

        await loadEquipments();

        alert("Equipamento criado com sucesso!");

    } catch (error) {
        console.error(error);
        alert("Não foi possível criar o equipamento.");
    }
}


// =======================================
// OPEN EDIT EQUIPMENT MODAL
// =======================================
function openEditEquipment(equipmentId) {
    const eq = equipmentCache.get(equipmentId);
    if (!eq) {
        alert("Equipamento não encontrado.");
        return;
    }

    document.getElementById("editEquipmentId").value = eq.id;
    document.getElementById("editEquipmentName").value = eq.name || "";
    document.getElementById("editEquipmentType").value = eq.type || "";
    document.getElementById("editEquipmentDescription").value = eq.description || "";
    document.getElementById("editEquipmentQuantity").value = eq.quantity || 1;
    document.getElementById("editEquipmentPrice").value = eq.pricePerHour || "";
    document.getElementById("editEquipmentStatus").value = eq.status || "AVAILABLE";

    editEquipmentModal.show();
}


// =======================================
// UPDATE EQUIPMENT
// =======================================
async function updateEquipment() {
    const equipmentId = document.getElementById("editEquipmentId").value;

    const name = document.getElementById("editEquipmentName").value.trim();
    const type = document.getElementById("editEquipmentType").value;
    const description = document.getElementById("editEquipmentDescription").value.trim();
    const quantity = parseInt(document.getElementById("editEquipmentQuantity").value, 10);
    const priceStr = document.getElementById("editEquipmentPrice").value;
    const status = document.getElementById("editEquipmentStatus").value;

    if (!name || !type || !quantity || !status) {
        alert("Por favor preencha os campos obrigatórios.");
        return;
    }

    const pricePerHour = priceStr ? parseFloat(priceStr) : null;

    const payload = {
        name,
        type,
        description,
        quantity,
        pricePerHour,
        status
    };

    try {
        const response = await fetch(`/api/owner/${ownerId}/equipment/${equipmentId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error("Erro ao atualizar equipamento.");
        }

        editEquipmentModal.hide();

        await loadEquipments();

        alert("Equipamento atualizado com sucesso!");

    } catch (error) {
        console.error(error);
        alert("Não foi possível atualizar o equipamento.");
    }
}
