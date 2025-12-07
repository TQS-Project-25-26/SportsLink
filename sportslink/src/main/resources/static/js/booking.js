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

    let currentDate = new Date(); // Controls the visible month
    let selectedDate = null;      // Controls the selected booking date
    let selectedSlot = null;      // Controls the selected time slot

    const monthNames = ["Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"];

    async function loadData() {
        try {
            const token = localStorage.getItem('token');
            const headers = {};
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            // Load facility
            const facilityRes = await fetch('/api/rentals/search', { headers: headers }); // GET with headers
            window.debugStatus = 'FETCHED';
            const facilities = await facilityRes.json();
            window.debugStatus = 'PARSED';
            facilityData = facilities.find(f => f.id == facilityId);
            window.debugStatus = 'FOUND: ' + (facilityData ? 'YES' : 'NO');


            if (!facilityData) {
                alert('Campo não encontrado');
                window.location.href = '../pages/main_page_user.html';
                return;
            }

            // Load equipments if any selected
            if (equipmentIds.length > 0) {
                const equipmentRes = await fetch(`/api/rentals/facility/${facilityId}/equipments`, { headers: headers });
                const allEquipments = await equipmentRes.json();
                selectedEquipments = allEquipments.filter(eq => equipmentIds.includes(eq.id));
            }

            updateUI();
            renderCalendar(); // Initial Calendar Render
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
        document.getElementById('field-cost').textContent = `€${facilityData.pricePerHour}/h`;

        const icon = sportIcons[facilityData.sportType] || 'sports';
        const iconElem = document.getElementById('summary-icon');
        const iconContainer = document.getElementById('summary-icon-container');
        const imgElem = document.getElementById('summary-image');

        if (facilityData.imageUrl) {
            imgElem.src = facilityData.imageUrl;
            imgElem.style.display = 'block';
            if (iconContainer) iconContainer.style.display = 'none';

            imgElem.onerror = () => {
                imgElem.style.display = 'none';
                if (iconContainer) iconContainer.style.display = 'flex';
                if (iconElem) iconElem.textContent = icon;
            };
        } else {
            imgElem.style.display = 'none';
            if (iconContainer) iconContainer.style.display = 'flex';
            if (iconElem) iconElem.textContent = icon;
        }

        renderEquipments();
        updateSummary();
    }

    // --- CALENDAR LOGIC START ---

    function renderCalendar() {
        const year = currentDate.getFullYear();
        const month = currentDate.getMonth();

        document.getElementById('current-month-year').textContent = `${monthNames[month]} ${year}`;

        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();

        // Adjust for Monday start (0=Sun, 1=Mon...). Standard JS getDay() 0 is Sun.
        // We want 0=Mon, 6=Sun.
        let startDay = firstDay.getDay() - 1;
        if (startDay === -1) startDay = 6;

        const calendarGrid = document.getElementById('calendar-days');
        calendarGrid.innerHTML = '';

        // Header days
        const weekDays = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'];
        weekDays.forEach(day => {
            const header = document.createElement('div');
            header.className = 'calendar-day-header';
            header.textContent = day;
            calendarGrid.appendChild(header);
        });

        // Empty slots for previous month
        for (let i = 0; i < startDay; i++) {
            const empty = document.createElement('div');
            calendarGrid.appendChild(empty);
        }

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        for (let i = 1; i <= daysInMonth; i++) {
            const dayEl = document.createElement('div');
            dayEl.className = 'calendar-day';
            dayEl.textContent = i;

            const thisDate = new Date(year, month, i);

            // Check if past date
            if (thisDate < today) {
                dayEl.classList.add('disabled');
            } else {
                dayEl.addEventListener('click', () => selectDate(thisDate));
            }

            // Check if today
            if (thisDate.getTime() === today.getTime()) {
                dayEl.classList.add('today');
            }

            // Check if selected
            if (selectedDate && thisDate.getTime() === selectedDate.getTime()) {
                dayEl.classList.add('selected');
            }

            calendarGrid.appendChild(dayEl);
        }
    }

    function selectDate(date) {
        selectedDate = date;
        selectedSlot = null; // Reset slot

        // Update hidden input
        // Format YYYY-MM-DD
        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, '0');
        const dd = String(date.getDate()).padStart(2, '0');
        document.getElementById('booking-date').value = `${yyyy}-${mm}-${dd}`;

        // Re-render calendar to show Selection state
        renderCalendar();

        // Render slots
        renderSlots(date);
        updateSummary();
    }

    // Mock Availability Logic
    function getMockSlots(date) {
        const slots = [];

        let startHour = 9;
        let endHour = 22;

        if (facilityData && facilityData.openingTime && facilityData.closingTime) {
            startHour = parseInt(facilityData.openingTime.split(':')[0]);
            endHour = parseInt(facilityData.closingTime.split(':')[0]);
        }

        // Use date string as seed for deterministic "randomness" so it doesn't change on click
        const seed = date.getDate() + date.getMonth();

        for (let h = startHour; h < endHour; h++) {
            // Mock some booked slots
            // Simple hash logic
            // Make fewer slots booked for testing purposes (e.g. only every 10th slot)
            const isBooked = (seed + h) % 10 === 0;

            const timeString = `${String(h).padStart(2, '0')}:00`;
            slots.push({
                time: timeString,
                available: !isBooked
            });
        }
        return slots;
    }

    function renderSlots(date) {
        const container = document.getElementById('slots-container');
        const grid = document.getElementById('slots-grid');
        const msg = document.getElementById('no-slots-msg');

        container.style.display = 'block';
        grid.innerHTML = '';
        msg.style.display = 'none';

        const slots = getMockSlots(date);
        const availableSlots = slots.filter(s => s.available);

        if (availableSlots.length === 0) {
            msg.style.display = 'block';
            return;
        }

        slots.forEach(slot => {
            const btn = document.createElement('div');
            btn.className = `time-slot ${slot.available ? '' : 'disabled'}`;
            btn.textContent = slot.time;

            if (slot.available) {
                if (selectedSlot === slot.time) {
                    btn.classList.add('selected');
                }
                btn.addEventListener('click', () => selectSlot(slot.time));
            }

            grid.appendChild(btn);
        });
    }

    function selectSlot(time) {
        selectedSlot = time;
        document.getElementById('start-time').value = time;

        // Update hidden inputs for End Time manually since we removed the listener
        updateEndTime(); // Will calculate end time based on duration

        // Re-render to show selection
        renderSlots(selectedDate);
    }

    // --- CALENDAR LOGIC END ---

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
        } else {
            document.getElementById('summary-date').textContent = '-';
        }

        if (startTime && endTime) {
            document.getElementById('summary-time').textContent = `${startTime} - ${endTime}`;
        } else {
            document.getElementById('summary-time').textContent = '-';
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

    // Event Listeners
    document.getElementById('duration').addEventListener('change', () => {
        updateEndTime(); // Recalculate end time and summary
    });

    // Calendar Navigation
    document.getElementById('prev-month').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() - 1);
        renderCalendar();
    });

    document.getElementById('next-month').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() + 1);
        renderCalendar();
    });


    document.getElementById('add-more-equipment').addEventListener('click', (e) => {
        e.preventDefault();
        const currentIds = selectedEquipments.map(eq => eq.id).join(',');
        window.location.href = `equipments.html?facilityId=${facilityId}`;
    });

    document.getElementById('btn-confirm-booking').addEventListener('click', async () => {
        const form = document.getElementById('booking-form');

        // Custom validation check
        const date = document.getElementById('booking-date').value;
        const startTime = document.getElementById('start-time').value;

        if (!date || !startTime) {
            alert("Por favor, selecione uma data e um horário.");
            return;
        }

        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

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
            const headers = { 'Content-Type': 'application/json' };
            const token = localStorage.getItem('token');
            if (token) headers['Authorization'] = `Bearer ${token}`;

            const response = await fetch('/api/rentals/rental', {
                method: 'POST',
                headers: headers,
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
