(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const facilityId = urlParams.get('facilityId');
    const equipmentIdsParam = urlParams.get('equipmentIds');
    const equipmentIds = equipmentIdsParam ? equipmentIdsParam.split(',').map(Number) : [];

    if (!facilityId) {
        window.location.href = '../pages/main_page_user.html';
        return;
    }

    let facilityData = null;
    let selectedEquipments = [];
    let suggestedEquipments = [];
    let selectedSports = [];
    let allEquipments = [];

    const sportIcons = {
        'FOOTBALL': 'sports_soccer',
        'PADEL': 'sports_tennis',
        'TENNIS': 'sports_tennis',
        'BASKETBALL': 'sports_basketball',
        'VOLLEYBALL': 'sports_volleyball',
        'SWIMMING': 'pool'
    };

    const sportNames = {
        'FOOTBALL': 'Futebol',
        'PADEL': 'Padel',
        'TENNIS': 'Ténis',
        'BASKETBALL': 'Basquetebol',
        'VOLLEYBALL': 'Voleibol',
        'SWIMMING': 'Natação'
    };

    async function loadData() {
        try {
            // Load facility
            const facilityRes = await fetch('/api/rentals/search');
            const facilities = await facilityRes.json();
            facilityData = facilities.find(f => f.id == facilityId);

            if (!facilityData) {
                alert('Campo não encontrado');
                window.location.href = '../pages/main_page_user.html';
                return;
            }

            // Load all equipments for the facility
            const equipmentRes = await fetch(`/api/rentals/facility/${facilityId}/equipments`);
            allEquipments = await equipmentRes.json();

            // Pre-select equipments if passed in URL
            if (equipmentIds.length > 0) {
                selectedEquipments = allEquipments.filter(eq => equipmentIds.includes(eq.id));
            }

            updateUI();
        } catch (err) {
            console.error('Error loading data:', err);
            alert('Erro ao carregar dados');
        }
    }

    function updateUI() {
        // Update breadcrumb and summary
        const backBtn = document.getElementById('back-btn');
        if (backBtn) backBtn.href = `field_detail.html?id=${facilityId}`;

        document.getElementById('summary-field-name').textContent = facilityData.name;
        document.getElementById('summary-location').textContent = `${facilityData.city} - ${facilityData.address}`;

        const icon = sportIcons[facilityData.sportType] || 'sports';
        document.getElementById('summary-icon').textContent = icon;

        // Set default date (today)
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);
        document.getElementById('booking-date').valueAsDate = tomorrow;
        document.getElementById('booking-date').min = tomorrow.toISOString().split('T')[0];

        // Set default time
        document.getElementById('start-time').value = '10:00';
        updateEndTime();

        // Render sports checkboxes
        renderSportsCheckboxes();

        // Render equipment suggestions and all equipment
        renderEquipmentSuggestions();
        renderAllEquipment();
        updateSummary();
    }

    function renderSportsCheckboxes() {
        const container = document.getElementById('sports-checkboxes');

        if (!facilityData || !facilityData.sports || facilityData.sports.length === 0) {
            container.innerHTML = '<p class="text-muted mb-0">Nenhum desporto disponível</p>';
            return;
        }

        container.innerHTML = facilityData.sports.map(sport => {
            const sportName = sportNames[sport] || sport;
            const icon = sportIcons[sport] || 'sports';
            return `
                <div class="form-check mb-2">
                    <input class="form-check-input sport-checkbox" type="checkbox" value="${sport}" id="sport-${sport}">
                    <label class="form-check-label d-flex align-items-center" for="sport-${sport}">
                        <i class="material-icons me-2 text-accent">${icon}</i>
                        <span>${sportName}</span>
                    </label>
                </div>
            `;
        }).join('');

        // Add event listeners
        document.querySelectorAll('.sport-checkbox').forEach(checkbox => {
            checkbox.addEventListener('change', handleSportSelection);
        });

        // Auto-select if only one sport
        if (facilityData.sports.length === 1) {
            const singleSport = facilityData.sports[0];
            const checkbox = document.getElementById(`sport-${singleSport}`);
            if (checkbox) {
                checkbox.checked = true;
                handleSportSelection();
            }
        }
    }

    function handleSportSelection() {
        selectedSports = Array.from(document.querySelectorAll('.sport-checkbox:checked'))
            .map(cb => cb.value);

        // Update badge
        const badge = document.getElementById('sports-selected-badge');
        if (selectedSports.length === 0) {
            badge.textContent = 'Selecione um desporto';
            badge.className = 'badge bg-warning text-dark';
        } else {
            badge.textContent = `${selectedSports.length} desporto(s) selecionado(s)`;
            badge.className = 'badge bg-success';
        }

        loadEquipmentSuggestions();
    }

    async function loadEquipmentSuggestions() {
        if (selectedSports.length === 0) {
            suggestedEquipments = [];
            renderEquipmentSuggestions();
            return;
        }

        try {
            const suggestions = [];
            for (const sport of selectedSports) {
                const res = await fetch(`/api/suggestions/equipment/${facilityId}?sport=${sport}`);
                if (res.ok) {
                    const sportSuggestions = await res.json();
                    suggestions.push(...sportSuggestions);
                }
            }

            const uniqueSuggestions = suggestions.reduce((acc, curr) => {
                if (!acc.find(s => s.equipmentId === curr.equipmentId)) {
                    acc.push(curr);
                }
                return acc;
            }, []);

            uniqueSuggestions.sort((a, b) => b.score - a.score);

            suggestedEquipments = uniqueSuggestions
                .map(suggestion => {
                    const equipment = allEquipments.find(eq => eq.id === suggestion.equipmentId);
                    return equipment ? { ...equipment, suggestion } : null;
                })
                .filter(eq => eq !== null);

            renderEquipmentSuggestions();
            // Also re-render all equipment to potentially update button states if needed (though duplicated)
            renderAllEquipment();
        } catch (err) {
            console.error('Error loading equipment suggestions:', err);
        }
    }

    function renderEquipmentSuggestions() {
        const container = document.getElementById('suggested-equipment-list');

        if (selectedSports.length === 0) {
            container.innerHTML = '<p class="text-muted text-center py-3">Selecione um desporto para ver equipamentos sugeridos</p>';
            return;
        }

        if (suggestedEquipments.length === 0) {
            container.innerHTML = '<p class="text-muted text-center py-3">Nenhum equipamento sugerido encontrado</p>';
            return;
        }

        container.innerHTML = suggestedEquipments.map(eq => {
            const isSelected = selectedEquipments.some(s => s.id === eq.id);
            const reason = eq.suggestion ? eq.suggestion.reason : '';
            const score = eq.suggestion ? Math.round(eq.suggestion.score) : 0;

            return `
                <div class="border rounded p-3 mb-2 bg-white ${isSelected ? 'border-success' : ''}">
                    <div class="d-flex justify-content-between align-items-start">
                        <div class="flex-grow-1">
                            <div class="d-flex align-items-center mb-1">
                                <strong class="me-2">${eq.name}</strong>
                                ${score > 80 ? '<span class="badge bg-success">Altamente Recomendado</span>' : ''}
                                ${isSelected ? '<span class="badge bg-primary ms-2"><i class="material-icons" style="font-size:12px">check</i> Selecionado</span>' : ''}
                            </div>
                            <p class="text-muted small mb-1">${eq.description || ''}</p>
                            ${reason ? `<p class="text-info small mb-1"><i class="material-icons" style="font-size: 14px;">info</i> ${reason}</p>` : ''}
                            <div class="small text-muted">
                                <span>Tipo: ${eq.type}</span> • 
                                <span>Disponível: ${eq.quantity} unidades</span>
                            </div>
                        </div>
                        <div class="text-end ms-3">
                            <div class="fw-bold text-accent">€${eq.pricePerHour}/h</div>
                            ${isSelected
                    ? `<button class="btn btn-sm btn-outline-danger mt-2" onclick="removeEquipment(${eq.id})">
                                     <i class="material-icons" style="font-size: 16px;">remove</i> Remover
                                   </button>`
                    : `<button class="btn btn-sm btn-success mt-2" onclick="addEquipment(${eq.id})">
                                     <i class="material-icons" style="font-size: 16px;">add</i> Adicionar
                                   </button>`
                }
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }

    function renderAllEquipment() {
        const container = document.getElementById('all-equipment-list');
        if (!container) return;

        if (allEquipments.length === 0) {
            container.innerHTML = '<p class="text-muted text-center py-3">Nenhum equipamento disponível</p>';
            return;
        }

        // To avoid duplication, we COULD filter out items that are already in suggestions.
        // BUT the user asked for "All Equipment", so we show everything.
        // We just need to make sure the state (selected/not) is consistent.

        container.innerHTML = allEquipments.map(eq => {
            const isSelected = selectedEquipments.some(s => s.id === eq.id);
            // Check if this item is also in suggestions to maybe highlight it or just leave as is. 
            // We'll treat it as a standard item list.

            return `
                <div class="border rounded p-3 mb-2 bg-white ${isSelected ? 'border-success' : ''}">
                    <div class="d-flex justify-content-between align-items-start">
                        <div class="flex-grow-1">
                            <div class="d-flex align-items-center mb-1">
                                <strong class="me-2">${eq.name}</strong>
                                ${isSelected ? '<span class="badge bg-primary ms-2"><i class="material-icons" style="font-size:12px">check</i> Selecionado</span>' : ''}
                            </div>
                            <p class="text-muted small mb-1">${eq.description || ''}</p>
                            <div class="small text-muted">
                                <span>Tipo: ${eq.type}</span> • 
                                <span>Disponível: ${eq.quantity} unidades</span>
                            </div>
                        </div>
                        <div class="text-end ms-3">
                            <div class="fw-bold text-accent">€${eq.pricePerHour}/h</div>
                            ${isSelected
                    ? `<button class="btn btn-sm btn-outline-danger mt-2" onclick="removeEquipment(${eq.id})">
                                     <i class="material-icons" style="font-size: 16px;">remove</i> Remover
                                   </button>`
                    : `<button class="btn btn-sm btn-success mt-2" onclick="addEquipment(${eq.id})">
                                     <i class="material-icons" style="font-size: 16px;">add</i> Adicionar
                                   </button>`
                }
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }

    window.addEquipment = function (equipmentId) {
        const eq = allEquipments.find(e => e.id === equipmentId);
        if (eq && !selectedEquipments.find(s => s.id === equipmentId)) {
            selectedEquipments.push(eq);
            // Refresh BOTH lists to sync button states
            renderEquipmentSuggestions();
            renderAllEquipment();
            updateSummary();
        }
    };

    window.removeEquipment = function (equipmentId) {
        selectedEquipments = selectedEquipments.filter(eq => eq.id !== equipmentId);
        // Refresh BOTH lists to sync button states
        renderEquipmentSuggestions();
        renderAllEquipment();
        updateSummary();
    };


    function updateEndTime() {
        const startTime = document.getElementById('start-time').value;
        const duration = parseInt(document.getElementById('duration').value);

        if (startTime) {
            const [hours, minutes] = startTime.split(':').map(Number);
            const endHours = (hours + duration) % 24;
            const endTime = `${String(endHours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
            document.getElementById('end-time').value = endTime;
            updateSummary();
        }
    }

    function updateSummary() {
        const date = document.getElementById('booking-date').value;
        const startTime = document.getElementById('start-time').value;
        const endTime = document.getElementById('end-time').value;
        const duration = parseInt(document.getElementById('duration').value);

        if (date) {
            const dateObj = new Date(date + 'T00:00:00');
            document.getElementById('summary-date').textContent =
                dateObj.toLocaleDateString('pt-PT', { day: '2-digit', month: 'short', year: 'numeric' });
        }

        if (startTime && endTime) {
            document.getElementById('summary-time').textContent = `${startTime} - ${endTime}`;
        }

        document.getElementById('summary-duration').textContent = `${duration}h`;

        const fieldCost = (facilityData?.pricePerHour || 0) * duration;
        document.getElementById('field-cost').textContent = `€${fieldCost}`;

        const equipmentCostsDiv = document.getElementById('equipment-costs');
        selectedEquipments.sort((a, b) => a.name.localeCompare(b.name));

        let equipmentTotal = 0;

        if (selectedEquipments.length > 0) {
            equipmentCostsDiv.innerHTML = selectedEquipments.map(eq => {
                const cost = eq.pricePerHour * duration;
                equipmentTotal += cost;
                return `
                    <div class="d-flex justify-content-between mb-2 small">
                        <span>${eq.name}:</span>
                        <span>€${cost}</span>
                    </div>
                `;
            }).join('');
        } else {
            equipmentCostsDiv.innerHTML = '';
        }

        const total = fieldCost + equipmentTotal;
        document.getElementById('total-cost').textContent = `€${total}`;
    }

    document.getElementById('start-time').addEventListener('change', updateEndTime);
    document.getElementById('duration').addEventListener('change', () => {
        updateEndTime();
        updateSummary();
    });
    document.getElementById('booking-date').addEventListener('change', updateSummary);

    document.getElementById('btn-confirm-booking').addEventListener('click', async () => {
        const form = document.getElementById('booking-form');

        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        const date = document.getElementById('booking-date').value;
        const startTime = document.getElementById('start-time').value;
        const endTime = document.getElementById('end-time').value;

        const startDateTime = `${date}T${startTime}:00`;
        const endDateTime = `${date}T${endTime}:00`;

        const bookingData = {
            userId: 1,
            facilityId: parseInt(facilityId),
            startTime: startDateTime,
            endTime: endDateTime,
            equipmentIds: selectedEquipments.map(eq => eq.id)
        };

        try {
            const response = await fetch('/api/rentals/rental', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(bookingData)
            });

            if (!response.ok) {
                const text = await response.text();
                try {
                    const error = JSON.parse(text);
                    throw new Error(error.message || 'Erro ao criar reserva');
                } catch (e) {
                    if (e instanceof SyntaxError) {
                        throw new Error(text || 'Erro ao criar reserva (Resposta inválida do servidor)');
                    }
                    throw e;
                }
            }

            const result = await response.json();
            document.getElementById('booking-id').textContent = result.id;

            const modal = new bootstrap.Modal(document.getElementById('successModal'));
            modal.show();
        } catch (err) {
            alert(`Erro: ${err.message}`);
            console.error('Booking error:', err);
        }
    });

    loadData();
})();
