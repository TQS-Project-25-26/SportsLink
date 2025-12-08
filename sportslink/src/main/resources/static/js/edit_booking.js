(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const rentalId = urlParams.get('id');

    if (!rentalId) {
        alert('Rental ID missing');
        window.location.href = '../pages/myBookingsPage.html';
        return;
    }

    let rentalData = null;
    let facilityData = null;
    let selectedEquipments = [];
    let allEquipments = [];

    // Constants
    const monthNames = ["Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"];
    let currentDate = new Date();
    let selectedDate = null;

    async function loadData() {
        try {
            const token = localStorage.getItem('token');
            const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

            // 1. Fetch Rental Details
            const rentalRes = await fetch(`/api/rentals/rental/${rentalId}/status`, { headers });
            if (!rentalRes.ok) throw new Error('Failed to load rental details');
            rentalData = await rentalRes.json();

            document.getElementById('booking-id-display').textContent = `#${rentalData.id}`;

            // 2. Fetch Facility Details (using search ensuring we find it)
            // Note: Ideally we should have a getFacilityById endpoint
            const facilityRes = await fetch('/api/rentals/search', { headers: headers }); // Reuse search query if filters allow, otherwise fetch all
            const facilities = await facilityRes.json();
            facilityData = facilities.find(f => f.id == rentalData.facilityId);

            if (!facilityData) {
                throw new Error('Facility not found');
            }

            // 3. Fetch Equipments
            const equipmentRes = await fetch(`/api/rentals/facility/${facilityData.id}/equipments`, { headers });
            allEquipments = await equipmentRes.json();

            // 4. Pre-fill Data
            prefillForm();

            // 5. Render UI
            updateUI();
            renderCalendar();

        } catch (err) {
            console.error('Error loading data:', err);
            alert('Error loading booking data: ' + err.message);
            window.location.href = '../pages/myBookingsPage.html';
        }
    }

    function prefillForm() {
        // Set min date for date picker
        const today = new Date();
        const yyyyToday = today.getFullYear();
        const mmToday = String(today.getMonth() + 1).padStart(2, '0');
        const ddToday = String(today.getDate()).padStart(2, '0');
        document.getElementById('booking-date').min = `${yyyyToday}-${mmToday}-${ddToday}`;

        const start = new Date(rentalData.startTime);
        const end = new Date(rentalData.endTime);

        // Date
        const yyyy = start.getFullYear();
        const mm = String(start.getMonth() + 1).padStart(2, '0');
        const dd = String(start.getDate()).padStart(2, '0');
        const dateStr = `${yyyy}-${mm}-${dd}`;

        document.getElementById('booking-date').value = dateStr;
        selectedDate = start;


        // Time
        const startHours = String(start.getHours()).padStart(2, '0');
        const startMinutes = String(start.getMinutes()).padStart(2, '0');
        document.getElementById('start-time').value = `${startHours}:${startMinutes}`;

        // Duration
        const durationMs = end - start;
        const durationHours = durationMs / (1000 * 60 * 60);

        const durationSelect = document.getElementById('duration');
        // Check if exact match exists, if not maybe add it? For now assume standard blocks.
        if ([1, 1.5, 2, 2.5, 3, 4].includes(durationHours)) {
            durationSelect.value = durationHours;
        } else {
            // Select closest or default? 
            durationSelect.value = durationHours; // Try setting it
        }

        updateEndTime();

        // Equipments
        // Rental data gives us plain names currently: "equipments": ["Racket", "Ball"]
        // IMPORTANT: The DTO returns NAMES only (List<String>), not IDs!
        // We need to map names back to IDs from allEquipments.
        // This is a limitation of the current DTO.
        // We will try our best to match.
        if (rentalData.equipments) {
            selectedEquipments = [];
            rentalData.equipments.forEach(name => {
                // Find an equipment with this name that is NOT yet selected (if duplicates allowed? currently unique names likely)
                // Assuming unique names per type/item for now or just generic mapping
                // If we have multiple items with same name, we pick one.
                // NOTE: Logic in backend assigns specific items. 
                // Front-end matches by ID. 
                // We really need IDs in DTO. 
                // Checking AuthResponseDTO edit history... we only added userId.
                // Checking RentalResponseDTO...

                // Workaround: Map by name. 
                const eq = allEquipments.find(e => e.name === name); // First match
                if (eq) {
                    selectedEquipments.push(eq);
                }
            });
        }
    }

    // --- UI RENDERING (Simplified from booking.js) ---

    function updateUI() {
        document.getElementById('summary-field-name').textContent = facilityData.name;
        document.getElementById('summary-location').textContent = `${facilityData.city} - ${facilityData.address}`;
        document.getElementById('field-cost').textContent = `€${facilityData.pricePerHour}/h`;

        renderAllEquipment();
        updateSummary();
    }

    function renderCalendar() {
        // ... reusing logic? For now simplified or copy paste essential parts.
        // Copied strictly necessary parts for date selection.
        const year = currentDate.getFullYear();
        const month = currentDate.getMonth();

        document.getElementById('current-month-year').textContent = `${monthNames[month]} ${year}`;

        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();

        let startDay = firstDay.getDay() - 1;
        if (startDay === -1) startDay = 6;

        const calendarGrid = document.getElementById('calendar-days');
        calendarGrid.innerHTML = '';

        const weekDays = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'];
        weekDays.forEach(day => {
            const header = document.createElement('div');
            header.className = 'calendar-day-header';
            header.textContent = day;
            calendarGrid.appendChild(header);
        });

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

            const checkDate = new Date(year, month, i);
            const todayReset = new Date();
            todayReset.setHours(0, 0, 0, 0);

            if (checkDate < todayReset) {
                dayEl.classList.add('disabled');
            } else {
                dayEl.addEventListener('click', () => selectDate(new Date(year, month, i)));
            }

            if (selectedDate &&
                i === selectedDate.getDate() &&
                month === selectedDate.getMonth() &&
                year === selectedDate.getFullYear()) {
                dayEl.classList.add('selected');
            }

            calendarGrid.appendChild(dayEl);
        }
    }

    function selectDate(date) {
        selectedDate = date;
        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, '0');
        const dd = String(date.getDate()).padStart(2, '0');
        document.getElementById('booking-date').value = `${yyyy}-${mm}-${dd}`;
        renderCalendar();
        updateSummary();
    }

    // --- EQUIPMENT ---

    function renderAllEquipment() {
        const container = document.getElementById('all-equipment-list');

        if (allEquipments.length === 0) {
            container.innerHTML = '<p class="text-muted text-center py-3">No equipment available</p>';
            return;
        }

        container.innerHTML = allEquipments.map(eq => {
            // Count how many of this type are selected
            // But current logic treats selectedEquipments as a list of OBJECTS
            // If I selected 2 Rackets, do I have 2 objects? 
            // In booking.js: "selectedEquipments.push(eq)" -> pushes the reference.
            // If I want 2, I push it twice? 
            // booking.js logic seems to allow only 1 of each ID? 
            // "if (!selectedEquipments.find(s => s.id === equipmentId))" -> Yes, only unique items.
            // So we strictly follow that.

            const isSelected = selectedEquipments.some(s => s.id === eq.id);

            return `
                <div class="border rounded p-3 mb-2 bg-white ${isSelected ? 'border-success' : ''}">
                    <div class="d-flex justify-content-between align-items-start">
                        <div class="flex-grow-1">
                            <strong class="me-2">${eq.name}</strong>
                             ${isSelected ? '<span class="badge bg-primary ms-2"><i class="material-icons" style="font-size:12px">check</i> Selected</span>' : ''}
                            <div class="small text-muted">
                                <span>Type: ${eq.type}</span> • 
                                <span>Avail: ${eq.quantity}</span>
                            </div>
                        </div>
                        <div class="text-end ms-3">
                            <div class="fw-bold text-accent">€${eq.pricePerHour}/h</div>
                            ${isSelected
                    ? `<button class="btn btn-sm btn-outline-danger mt-2" onclick="removeEquipment(${eq.id})">Remove</button>`
                    : `<button class="btn btn-sm btn-success mt-2" onclick="addEquipment(${eq.id})">Add</button>`
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
            renderAllEquipment();
            updateSummary();
        }
    };

    window.removeEquipment = function (equipmentId) {
        selectedEquipments = selectedEquipments.filter(eq => eq.id !== equipmentId);
        renderAllEquipment();
        updateSummary();
    };

    // --- TIME & SUMMARY ---

    function updateEndTime() {
        const startTime = document.getElementById('start-time').value;
        const duration = parseFloat(document.getElementById('duration').value);

        if (startTime) {
            const [hoursStr, minutesStr] = startTime.split(':');
            const hours = parseInt(hoursStr);
            const minutes = parseInt(minutesStr);

            const totalStartMinutes = hours * 60 + minutes;
            const totalEndMinutes = totalStartMinutes + (duration * 60);

            const endHours = Math.floor(totalEndMinutes / 60) % 24;
            const endMinutes = totalEndMinutes % 60;

            const endTime = `${String(endHours).padStart(2, '0')}:${String(endMinutes).padStart(2, '0')}`;
            document.getElementById('end-time').value = endTime;
            updateSummary();
        }
    }

    function updateSummary() {
        const date = document.getElementById('booking-date').value;
        const startTime = document.getElementById('start-time').value;
        const endTime = document.getElementById('end-time').value;
        const duration = parseFloat(document.getElementById('duration').value);

        document.getElementById('summary-date').textContent = date ? date : '-';
        document.getElementById('summary-time').textContent = (startTime && endTime) ? `${startTime} - ${endTime}` : '-';
        document.getElementById('summary-duration').textContent = `${duration}h`;

        const fieldCost = (facilityData?.pricePerHour || 0) * duration;
        document.getElementById('field-cost').textContent = `€${fieldCost.toFixed(2)}`;

        const equipmentCostsDiv = document.getElementById('equipment-costs');
        let equipmentTotal = 0;

        if (selectedEquipments.length > 0) {
            equipmentCostsDiv.innerHTML = selectedEquipments.map(eq => {
                const cost = eq.pricePerHour * duration;
                equipmentTotal += cost;
                return `
                    <div class="d-flex justify-content-between mb-2 small">
                        <span>${eq.name}:</span>
                        <span>€${cost.toFixed(2)}</span>
                    </div>
                `;
            }).join('');
        } else {
            equipmentCostsDiv.innerHTML = '';
        }

        const total = fieldCost + equipmentTotal;
        document.getElementById('total-cost').textContent = `€${total.toFixed(2)}`;
    }

    // --- EVENTS ---

    document.getElementById('duration').addEventListener('change', updateEndTime);
    document.getElementById('start-time').addEventListener('input', updateEndTime);
    document.getElementById('booking-date').addEventListener('change', (e) => {
        const parts = e.target.value.split('-');
        selectedDate = new Date(parts[0], parts[1] - 1, parts[2]);
        renderCalendar();
        updateSummary();
    });

    document.getElementById('prev-month').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() - 1);
        renderCalendar();
    });

    document.getElementById('next-month').addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() + 1);
        renderCalendar();
    });

    // UPDATE BUTTON
    document.getElementById('btn-update-booking').addEventListener('click', async () => {
        hideError();
        const date = document.getElementById('booking-date').value;
        const startTime = document.getElementById('start-time').value;
        const endTime = document.getElementById('end-time').value;

        if (!date || !startTime) {
            showError("Please select date and time.");
            return;
        }

        const startDateTime = `${date}T${startTime}:00`;
        const endDateTime = `${date}T${endTime}:00`;

        const updateData = {
            userId: parseInt(localStorage.getItem('userId')), // Keep owner
            facilityId: facilityData.id,
            startTime: startDateTime,
            endTime: endDateTime,
            equipmentIds: selectedEquipments.map(eq => eq.id)
        };

        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`/api/rentals/rental/${rentalId}/update`, {
                method: 'PUT', // Controller uses PUT
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(updateData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to update rental');
            }

            const modalEl = document.getElementById('successModal');
            modalEl.querySelector('h3').textContent = 'Booking Updated!';
            modalEl.querySelector('p').textContent = 'Your booking has been successfully updated.';

            const modal = new bootstrap.Modal(modalEl);
            modal.show();

        } catch (err) {
            console.error(err);
            showError(err.message);
        }
    });

    // CANCEL BUTTON
    document.getElementById('btn-cancel-booking').addEventListener('click', async () => {
        if (!confirm('Are you sure you want to cancel this booking?')) return;
        hideError();

        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`/api/rentals/rental/${rentalId}/cancel`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const modalEl = document.getElementById('successModal');
                // Customize modal for cancel
                modalEl.querySelector('i').className = 'material-icons text-danger';
                modalEl.querySelector('i').textContent = 'cancel'; // Not check_circle
                modalEl.querySelector('h3').textContent = 'Booking Cancelled';
                modalEl.querySelector('p').textContent = 'Your booking has been cancelled successfully.';

                const modal = new bootstrap.Modal(modalEl);
                modal.show();
            } else {
                const errorData = await response.json();
                showError(errorData.message || 'Failed to cancel rental.');
            }
        } catch (e) {
            console.error(e);
            showError('Error cancelling rental: ' + e.message);
        }
    });

    function showError(message) {
        const alertEl = document.getElementById('error-alert');
        const msgEl = document.getElementById('error-message');
        msgEl.textContent = message;
        alertEl.classList.remove('d-none');
        // Scroll to error
        alertEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function hideError() {
        document.getElementById('error-alert').classList.add('d-none');
    }

    // START
    loadData();
})();
