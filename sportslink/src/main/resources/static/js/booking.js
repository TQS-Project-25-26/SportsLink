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

    const sportIcons = {
        'Football': 'sports_soccer',
        'Padel': 'sports_tennis',
        'Tennis': 'sports_tennis',
        'Basketball': 'sports_basketball',
        'Volleyball': 'sports_volleyball'
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

            // Load equipments if any selected
            if (equipmentIds.length > 0) {
                const equipmentRes = await fetch(`/api/rentals/facility/${facilityId}/equipments`);
                const allEquipments = await equipmentRes.json();
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

        // Render selected equipments
        renderEquipments();
        updateSummary();
    }

    function renderEquipments() {
        const container = document.getElementById('selected-equipment-list');

        if (selectedEquipments.length === 0) {
            container.innerHTML = '<p class="text-muted text-center py-3">Nenhum equipamento selecionado</p>';
            return;
        }

        container.innerHTML = selectedEquipments.map(eq => `
            <div class="d-flex justify-content-between align-items-center py-2 border-bottom">
                <div>
                    <strong>${eq.name}</strong>
                    <p class="text-muted small mb-0">${eq.description || ''}</p>
                </div>
                <span class="fw-bold text-accent">€${eq.pricePerHour}/h</span>
            </div>
        `).join('');
    }

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

        // Update summary display
        if (date) {
            const dateObj = new Date(date + 'T00:00:00');
            document.getElementById('summary-date').textContent =
                dateObj.toLocaleDateString('pt-PT', { day: '2-digit', month: 'short', year: 'numeric' });
        }

        if (startTime && endTime) {
            document.getElementById('summary-time').textContent = `${startTime} - ${endTime}`;
        }

        document.getElementById('summary-duration').textContent = `${duration}h`;

        // Calculate costs
        const fieldCost = (facilityData?.pricePerHour || 0) * duration;
        document.getElementById('field-cost').textContent = `€${fieldCost}`;

        const equipmentCostsDiv = document.getElementById('equipment-costs');
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

    document.getElementById('add-more-equipment').addEventListener('click', (e) => {
        e.preventDefault();
        const currentIds = selectedEquipments.map(eq => eq.id).join(',');
        window.location.href = `equipments.html?facilityId=${facilityId}`;
    });

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
            userId: 1, // Demo user
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
                const error = await response.json();
                throw new Error(error.message || 'Erro ao criar reserva');
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
