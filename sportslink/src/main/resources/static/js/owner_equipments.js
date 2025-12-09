// =======================================
// OWNER EQUIPMENTS JS
// =======================================

// OwnerId passa a vir do JWT (inicialmente null)
let ownerId = null;

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

// Helper para mensagens
function notify(message, kind = 'info') {
    if (typeof showBootstrapToast === 'function') {
        showBootstrapToast(message, kind);
    } else {
        alert(message);
    }
}

// =======================================
// ON LOAD
// =======================================
document.addEventListener("DOMContentLoaded", async () => {

    if (!facilityId) {
        notify("Nenhum campo selecionado.", 'warning');
        window.location.href = "owner_dashboard.html";
        return;
    }

    // Verificar se há token
    const token = getToken();
    if (!token) {
        window.location.href = '/index.html';
        return;
    }

    // Carregar perfil para obter ownerId e validar role OWNER
    try {
        const res = await fetch('/api/auth/profile', {
            headers: authHeaders()
        });

        if (!res.ok) {
            if (res.status === 401) {
                logout();
                return;
            }
            throw new Error('Não foi possível carregar o perfil.');
        }

        const user = await res.json();
        if (user.role !== 'OWNER') {
            // Se não for OWNER, manda para a página principal de utilizador
            window.location.href = '/pages/main_page_user.html';
            return;
        }

        ownerId = user.id;
    } catch (error) {
        console.error(error);
        notify("Erro ao carregar o perfil do proprietário.", 'danger');
        return;
    }

    // Set label simples
    if (facilityLabel) {
        facilityLabel.textContent = ` (Campo #${facilityId})`;
    }

    // Inicializar modals
    addEquipmentModal = new bootstrap.Modal(document.getElementById("addEquipmentModal"));
    editEquipmentModal = new bootstrap.Modal(document.getElementById("editEquipmentModal"));

    // Botão "Adicionar Equipamento"
    if (btnOpenAddEquipment) {
        btnOpenAddEquipment.addEventListener("click", () => {
            const form = document.getElementById("addEquipmentForm");
            if (form) form.reset();
            addEquipmentModal.show();
        });
    }

    // Botão confirmar adicionar
    const btnConfirmAdd = document.getElementById("btnConfirmAddEquipment");
    if (btnConfirmAdd) {
        btnConfirmAdd.addEventListener("click", async () => {
            await createEquipment();
        });
    }

    // Botão confirmar editar
    const btnConfirmEdit = document.getElementById("btnConfirmEditEquipment");
    if (btnConfirmEdit) {
        btnConfirmEdit.addEventListener("click", async () => {
            await updateEquipment();
        });
    }

    // Carregar equipamentos do campo
    await loadEquipments();
});


// =======================================
// LOAD EQUIPMENTS
// =======================================
async function loadEquipments() {
    if (!equipmentGrid) return;

    equipmentGrid.innerHTML = "";
    equipmentCache.clear();

    if (loadingDiv) loadingDiv.style.display = "block";
    if (noEquipmentDiv) noEquipmentDiv.style.display = "none";

    try {
        const response = await fetch(`/api/owner/${ownerId}/facilities/${facilityId}/equipment`, {
            headers: authHeaders()
        });
        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return;
            }
            throw new Error("Erro ao carregar equipamentos.");
        }

        const equipments = await response.json();

        if (loadingDiv) loadingDiv.style.display = "none";

        if (!equipments || equipments.length === 0) {
            if (noEquipmentDiv) noEquipmentDiv.style.display = "block";
            return;
        }

        if (noEquipmentDiv) noEquipmentDiv.style.display = "none";

        equipments.forEach(eq => {
            equipmentCache.set(eq.id, eq);
            equipmentGrid.appendChild(createEquipmentCard(eq));
        });

    } catch (error) {
        console.error(error);
        if (loadingDiv) loadingDiv.style.display = "none";
        notify("Não foi possível carregar os equipamentos.", 'danger');
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
                    <span class="badge ${eq.status === "AVAILABLE"
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
                    <button class="btn bg-accent text-white btn-sm flex-fill"
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

    if (!ownerId) {
        notify("Utilizador não identificado como proprietário.", 'danger');
        return;
    }

    const name = document.getElementById("equipmentName").value.trim();
    const type = document.getElementById("equipmentType").value;
    const description = document.getElementById("equipmentDescription").value.trim();
    const quantity = parseInt(document.getElementById("equipmentQuantity").value, 10);
    const priceStr = document.getElementById("equipmentPrice").value;
    const status = document.getElementById("equipmentStatus").value;

    if (!name || !type || !quantity || !status) {
        notify("Por favor preencha os campos obrigatórios.", 'warning');
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
            headers: Object.assign({ "Content-Type": "application/json" }, authHeaders()),
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return;
            }
            throw new Error("Erro ao criar equipamento.");
        }

        addEquipmentModal.hide();
        const form = document.getElementById("addEquipmentForm");
        if (form) form.reset();

        await loadEquipments();

        notify("Equipamento criado com sucesso!", 'success');

    } catch (error) {
        console.error(error);
        notify("Não foi possível criar o equipamento.", 'danger');
    }
}


// =======================================
// OPEN EDIT EQUIPMENT MODAL
// =======================================
function openEditEquipment(equipmentId) {
    const eq = equipmentCache.get(equipmentId);
    if (!eq) {
        notify("Equipamento não encontrado.", 'warning');
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
    if (!ownerId) {
        notify("Utilizador não identificado como proprietário.", 'danger');
        return;
    }

    const equipmentId = document.getElementById("editEquipmentId").value;

    const name = document.getElementById("editEquipmentName").value.trim();
    const type = document.getElementById("editEquipmentType").value;
    const description = document.getElementById("editEquipmentDescription").value.trim();
    const quantity = parseInt(document.getElementById("editEquipmentQuantity").value, 10);
    const priceStr = document.getElementById("editEquipmentPrice").value;
    const status = document.getElementById("editEquipmentStatus").value;

    if (!name || !type || !quantity || !status) {
        notify("Por favor preencha os campos obrigatórios.", 'warning');
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
            headers: Object.assign({ "Content-Type": "application/json" }, authHeaders()),
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return;
            }
            throw new Error("Erro ao atualizar equipamento.");
        }

        editEquipmentModal.hide();

        await loadEquipments();

        notify("Equipamento atualizado com sucesso!", 'success');

    } catch (error) {
        console.error(error);
        notify("Não foi possível atualizar o equipamento.", 'danger');
    }
}
