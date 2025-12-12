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

    let currentDate = new Date(); // Controls the visible month
    let selectedDate = null;      // Controls the selected booking date

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

            // Load all equipments for the facility
            const equipmentRes = await fetch(`/api/rentals/facility/${facilityId}/equipments`);
            allEquipments = await equipmentRes.json();

            // Pre-select equipments if passed in URL
            if (equipmentIds.length > 0) {
                const equipmentRes = await fetch(`/api/rentals/facility/${facilityId}/equipments`, { headers: headers });
                const allEquipments = await equipmentRes.json();
                selectedEquipments = allEquipments.filter(eq => equipmentIds.includes(eq.id));
            }

            updateUI();
            renderCalendar(); // Initial Calendar Render
            loadUserInfo(); // Fetch and pre-fill user details
        } catch (err) {
            console.error('Error loading data:', err);
            alert('Erro ao carregar dados');
        }
    }

    async function loadUserInfo() {
        try {
            const token = localStorage.getItem('token');
            if (!token) return;

            const res = await fetch('/api/auth/profile', {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (res.ok) {
                const user = await res.json();
                const nameInput = document.getElementById('user-name');
                const emailInput = document.getElementById('user-email');
                const phoneInput = document.getElementById('user-phone');

                if (nameInput && user.name) nameInput.value = user.name;
                if (emailInput && user.email) emailInput.value = user.email;
                if (phoneInput && user.phone) phoneInput.value = user.phone;
            }
        } catch (err) {
            console.warn('Failed to load user info for pre-fill:', err);
            // Non-critical, just leave fields empty
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

        renderAllEquipment();
        renderSportsCheckboxes(); // Ensure sports are rendered
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

            // Check if past date
            // Use a new date object for comparison to avoid mutating variables
            const checkDate = new Date(year, month, i);
            // Reset hours for accurate date comparison
            const todayReset = new Date();
            todayReset.setHours(0, 0, 0, 0);

            if (checkDate < todayReset) {
                dayEl.classList.add('disabled');
            } else {
                // Fix closure issue by using let/const in loop or passing explicit date
                dayEl.addEventListener('click', () => selectDate(new Date(year, month, i)));
            }

            // Check if today
            if (i === today.getDate() && month === today.getMonth() && year === today.getFullYear()) {
                dayEl.classList.add('today');
            }

            // Check if selected
            if (selectedDate &&
                i === selectedDate.getDate() &&
                month === selectedDate.getMonth() &&
                year === selectedDate.getFullYear()) {
                dayEl.classList.add('selected');
            }

            calendarGrid.appendChild(dayEl);
        }
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
        updateSummary();
    }

    // --- CALENDAR LOGIC END ---

    function renderAllEquipment() {
        const container = document.getElementById('all-equipment-list');

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
        const duration = parseFloat(document.getElementById('duration').value);

        if (startTime) {
            const [hoursStr, minutesStr] = startTime.split(':');
            const hours = parseInt(hoursStr);
            const minutes = parseInt(minutesStr);

            // Convert to minutes, add duration (in hours * 60), convert back
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

    // Event Listeners
    document.getElementById('duration').addEventListener('change', () => {
        updateEndTime();
    });

    document.getElementById('start-time').addEventListener('input', () => {
        updateEndTime();
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

    // Stripe payment integration
    let stripe = null;
    let elements = null;
    let paymentElement = null;
    let currentRentalId = null;

    // Initialize Stripe (lazy load)
    async function initStripe() {
        if (stripe) return;

        try {
            const configRes = await fetch('/api/payments/config');
            if (!configRes.ok) {
                throw new Error('Could not load Stripe configuration');
            }
            const config = await configRes.json();
            stripe = Stripe(config.publishableKey);
        } catch (err) {
            console.error('Failed to initialize Stripe:', err);
            throw err;
        }
    }

    // Fetch receipt URL and show View Receipt button
    async function fetchAndShowReceipt(rentalId) {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`/api/payments/status/${rentalId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const paymentStatus = await response.json();
                if (paymentStatus.receiptUrl) {
                    const receiptBtn = document.getElementById('view-receipt-btn');
                    receiptBtn.href = paymentStatus.receiptUrl;
                    receiptBtn.style.display = 'inline-flex';
                }
            }
        } catch (err) {
            console.warn('Could not fetch receipt URL:', err);
            // Non-critical, just don't show the button
        }
    }

    // Show payment modal and mount Stripe Elements
    async function showPaymentModal(rentalId, amount, email) {
        currentRentalId = rentalId;

        const paymentModal = new bootstrap.Modal(document.getElementById('paymentModal'));
        document.getElementById('payment-amount').textContent = `€${amount.toFixed(2)}`;
        document.getElementById('payment-message').style.display = 'none';
        document.getElementById('payment-loading').style.display = 'block';
        document.getElementById('payment-element').innerHTML = '';
        document.getElementById('submit-payment').disabled = true;

        paymentModal.show();

        try {
            await initStripe();

            // Create PaymentIntent on backend
            const token = localStorage.getItem('token');
            const response = await fetch(`/api/payments/create-intent/${rentalId}?email=${encodeURIComponent(email)}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || 'Failed to create payment intent');
            }

            const { clientSecret } = await response.json();

            // Mount Stripe Elements
            elements = stripe.elements({ clientSecret });
            paymentElement = elements.create('payment', {
                layout: 'tabs'
            });

            document.getElementById('payment-loading').style.display = 'none';
            paymentElement.mount('#payment-element');
            document.getElementById('submit-payment').disabled = false;

        } catch (err) {
            console.error('Payment initialization error:', err);
            document.getElementById('payment-loading').style.display = 'none';
            document.getElementById('payment-message').textContent = err.message;
            document.getElementById('payment-message').style.display = 'block';
        }
    }

    // Handle payment submission
    document.getElementById('submit-payment').addEventListener('click', async () => {
        if (!stripe || !elements) return;

        const submitBtn = document.getElementById('submit-payment');
        const messageDiv = document.getElementById('payment-message');

        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Processing...';
        messageDiv.style.display = 'none';

        try {
            const { error, paymentIntent } = await stripe.confirmPayment({
                elements,
                confirmParams: {
                    return_url: window.location.origin + '/pages/booking.html?success=true&rentalId=' + currentRentalId
                },
                redirect: 'if_required'
            });

            if (error) {
                messageDiv.textContent = error.message;
                messageDiv.style.display = 'block';
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="material-icons align-middle me-1" style="font-size: 18px;">lock</i> Pay Now';
            } else if (paymentIntent && paymentIntent.status === 'succeeded') {
                // Payment successful - close payment modal and show success
                bootstrap.Modal.getInstance(document.getElementById('paymentModal')).hide();

                document.getElementById('booking-id').textContent = currentRentalId;

                // Fetch receipt URL from payment status
                await fetchAndShowReceipt(currentRentalId);

                const successModal = new bootstrap.Modal(document.getElementById('successModal'));
                successModal.show();
            }
        } catch (err) {
            console.error('Payment error:', err);
            messageDiv.textContent = 'Payment failed. Please try again.';
            messageDiv.style.display = 'block';
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="material-icons align-middle me-1" style="font-size: 18px;">lock</i> Pay Now';
        }
    });

    // Handle cancel payment
    document.getElementById('cancel-payment').addEventListener('click', () => {
        // Rental was created but payment cancelled - user can pay later
        if (currentRentalId) {
            alert('Your booking has been created but is pending payment. You can complete payment from your rentals page.');
        }
    });

    // Booking confirmation - now creates rental then shows payment
    document.getElementById('btn-confirm-booking').addEventListener('click', async () => {
        const form = document.getElementById('booking-form');

        // Custom validation check
        const date = document.getElementById('booking-date').value;
        const startTime = document.getElementById('start-time').value;

        if (!date || !startTime) {
            alert("Please select a date and time.");
            return;
        }

        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        const email = document.getElementById('user-email').value;
        if (!email) {
            alert("Please enter your email for payment receipt.");
            return;
        }

        const endTime = document.getElementById('end-time').value;
        const startDateTime = `${date}T${startTime}:00`;
        const endDateTime = `${date}T${endTime}:00`;

        const bookingData = {
            userId: parseInt(localStorage.getItem('userId')),
            facilityId: parseInt(facilityId),
            startTime: startDateTime,
            endTime: endDateTime,
            equipmentIds: selectedEquipments.map(eq => eq.id)
        };

        try {
            const headers = { 'Content-Type': 'application/json' };
            const token = localStorage.getItem('token');
            if (token) headers['Authorization'] = `Bearer ${token}`;

            // Create rental first
            const response = await fetch('/api/rentals/rental', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(bookingData)
            });

            if (!response.ok) {
                const text = await response.text();
                try {
                    const error = JSON.parse(text);
                    throw new Error(error.message || 'Error creating booking');
                } catch (e) {
                    if (e instanceof SyntaxError) {
                        throw new Error(text || 'Error creating booking');
                    }
                    throw e;
                }
            }

            const result = await response.json();

            // Calculate total for payment
            const duration = parseFloat(document.getElementById('duration').value);
            const fieldCost = (facilityData?.pricePerHour || 0) * duration;
            const equipmentCost = selectedEquipments.reduce((sum, eq) => sum + (eq.pricePerHour || 0) * duration, 0);
            const totalAmount = fieldCost + equipmentCost;

            // Show payment modal
            await showPaymentModal(result.id, totalAmount, email);

        } catch (err) {
            alert(`Error: ${err.message}`);
            console.error('Booking error:', err);
        }
    });

    // Check for payment success return (reuse urlParams from line 2)
    if (urlParams.get('success') === 'true') {
        const rentalId = urlParams.get('rentalId');
        if (rentalId) {
            document.getElementById('booking-id').textContent = rentalId;
            // Fetch and show receipt button
            fetchAndShowReceipt(rentalId);
            const successModal = new bootstrap.Modal(document.getElementById('successModal'));
            successModal.show();
            // Clean URL
            window.history.replaceState({}, '', window.location.pathname);
        }
    }

    loadData();
})();

