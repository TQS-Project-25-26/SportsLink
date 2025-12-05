(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const facilityId = urlParams.get('facilityId');

    if (!facilityId) {
        window.location.href = '../pages/main_page_user.html';
        return;
    }

    let allEquipments = [];
    let selectedEquipments = new Set();

    async function loadEquipments() {
        try {
            document.getElementById('loading').style.display = 'block';
            const res = await fetch(`/api/rentals/facility/${facilityId}/equipments`);

            if (!res.ok) {
                throw new Error('Failed to load equipments');
            }

            allEquipments = await res.json();
            document.getElementById('loading').style.display = 'none';

            renderEquipments();
            setupNavigation();
        } catch (err) {
            console.error('Error loading equipments:', err);
            document.getElementById('loading').innerHTML =
                '<p class="text-danger">Erro ao carregar equipamentos</p>';
        }
    }

    async function setupNavigation() {
        try {
            // Update Back Button
            const backBtn = document.getElementById('back-btn');
            backBtn.href = `field_detail.html?id=${facilityId}`;

        } catch (err) {
            console.error('Error setting up navigation:', err);
        }
    }

    function renderEquipments() {
        const container = document.getElementById('equipment-grid');
        container.innerHTML = '';

        const filterAvailable = document.getElementById('filter-available').checked;
        const filterUnavailable = document.getElementById('filter-unavailable').checked;
        const filterType = document.getElementById('filter-type').value;

        let filtered = allEquipments.filter(eq => {
            const statusMatch = (filterAvailable && eq.status === 'AVAILABLE') ||
                (filterUnavailable && eq.status !== 'AVAILABLE');
            const typeMatch = !filterType || eq.type === filterType;
            return statusMatch && typeMatch;
        });

        document.getElementById('equipment-count').textContent = `${filtered.length} equipamento(s)`;

        if (filtered.length === 0) {
            document.getElementById('no-results').style.display = 'block';
            return;
        }

        document.getElementById('no-results').style.display = 'none';

        filtered.forEach(eq => {
            const div = document.createElement('div');
            div.className = 'col-md-6 col-lg-4';

            const isAvailable = eq.status === 'AVAILABLE';
            const isSelected = selectedEquipments.has(eq.id);

            div.innerHTML = `
                <div class="card equipment-card ${!isAvailable ? 'unavailable' : ''} ${isSelected ? 'selected' : ''}" 
                     data-id="${eq.id}" data-available="${isAvailable}">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <h6 class="fw-bold mb-0">${eq.name}</h6>
                            ${isSelected ? '<i class="material-icons text-accent">check_circle</i>' : ''}
                        </div>
                        <p class="text-muted small mb-2">${eq.description || 'Sem descrição'}</p>
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="badge ${isAvailable ? 'bg-success' : 'bg-secondary'}">${eq.status}</span>
                            <span class="text-muted small">Tipo: ${eq.type}</span>
                        </div>
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="small">Quantidade: ${eq.quantity}</span>
                            <span class="fw-bold text-accent">€${eq.pricePerHour}/hora</span>
                        </div>
                    </div>
                </div>
            `;

            const card = div.querySelector('.equipment-card');
            if (isAvailable) {
                card.addEventListener('click', () => toggleSelection(eq));
            }

            container.appendChild(div);
        });
    }

    function toggleSelection(equipment) {
        if (selectedEquipments.has(equipment.id)) {
            selectedEquipments.delete(equipment.id);
        } else {
            selectedEquipments.add(equipment.id);
        }
        updateSelection();
        renderEquipments();
    }

    function updateSelection() {
        const count = selectedEquipments.size;
        const continueBtn = document.getElementById('continue-btn');
        const summary = document.getElementById('selection-summary');

        if (count > 0) {
            continueBtn.style.display = 'block';
            summary.style.display = 'block';
            document.getElementById('selected-count').textContent = count;

            const selectedList = document.getElementById('selected-list');
            const totalElement = document.getElementById('selected-total');

            selectedList.innerHTML = '';
            let total = 0;

            selectedEquipments.forEach(id => {
                const eq = allEquipments.find(e => e.id === id);
                if (eq) {
                    total += eq.pricePerHour;
                    const item = document.createElement('div');
                    item.className = 'small mb-1';
                    item.innerHTML = `${eq.name} - €${eq.pricePerHour}`;
                    selectedList.appendChild(item);
                }
            });

            totalElement.textContent = `€${total}/hora`;
        } else {
            continueBtn.style.display = 'none';
            summary.style.display = 'none';
        }
    }

    document.getElementById('continue-btn').addEventListener('click', () => {
        const equipmentIds = Array.from(selectedEquipments).join(',');
        window.location.href = `booking.html?facilityId=${facilityId}&equipmentIds=${equipmentIds}`;
    });

    document.getElementById('filter-available').addEventListener('change', renderEquipments);
    document.getElementById('filter-unavailable').addEventListener('change', renderEquipments);
    document.getElementById('filter-type').addEventListener('change', renderEquipments);

    document.getElementById('btn-clear-filters').addEventListener('click', () => {
        document.getElementById('filter-available').checked = true;
        document.getElementById('filter-unavailable').checked = false;
        document.getElementById('filter-type').value = '';
        renderEquipments();
    });

    loadEquipments();
})();
