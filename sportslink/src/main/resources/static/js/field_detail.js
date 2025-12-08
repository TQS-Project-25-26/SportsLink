(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const facilityId = urlParams.get('id');

    if (!facilityId) {
        window.location.href = '../pages/main_page_user.html';
        return;
    }

    let facilityData = null;

    const sportIcons = {
        'FOOTBALL': 'sports_soccer',
        'PADEL': 'sports_tennis',
        'TENNIS': 'sports_tennis',
        'BASKETBALL': 'sports_basketball',
        'VOLLEYBALL': 'sports_volleyball'
    };

    async function loadFacility() {
        try {
            const res = await fetch('/api/rentals/search', { headers: authHeaders() });
            const facilities = await res.json();
            facilityData = facilities.find(f => f.id == facilityId);

            if (!facilityData) {
                alert('Campo não encontrado');
                window.location.href = '../pages/main_page_user.html';
                return;
            }

            // Update page
            document.getElementById('field-name').textContent = facilityData.name;
            document.getElementById('field-location').textContent = facilityData.city;
            document.getElementById('field-description').textContent = facilityData.description || 'Sem descrição disponível';
            document.getElementById('field-hours').textContent =
                `${facilityData.openingTime || '08:00'} - ${facilityData.closingTime || '22:00'}`;
            document.getElementById('field-rating').textContent = facilityData.rating || '0.0';
            const sportsText = facilityData.sports && facilityData.sports.length > 0
                ? facilityData.sports.join(', ')
                : (facilityData.sportType || 'N/A');
            document.getElementById('field-sport').textContent = sportsText;
            document.getElementById('field-address').textContent = facilityData.address;
            document.getElementById('field-price').textContent = `€${facilityData.pricePerHour}/hora`;

            const primarySport = facilityData.sports && facilityData.sports.length > 0
                ? facilityData.sports[0]
                : facilityData.sportType;
            const icon = sportIcons[primarySport] || 'sports';
            document.getElementById('field-icon').textContent = icon;

            loadEquipmentSuggestions();
            loadEquipmentPreview();
        } catch (err) {
            console.error('Error loading facility:', err);
            alert('Erro ao carregar campo');
        }
    }

    async function loadEquipmentSuggestions() {
        if (typeof SuggestionsService === 'undefined' || !facilityData) {
            console.log('SuggestionsService not available');
            return;
        }

        try {
            const sport = facilityData.sports && facilityData.sports.length > 0
                ? facilityData.sports[0]
                : facilityData.sportType;
            const suggestions = await SuggestionsService.getEquipmentSuggestions(facilityId, sport);
            console.log('Equipment suggestions:', suggestions);

            const container = document.getElementById('equipment-suggestions');
            const noSuggestions = document.getElementById('no-suggestions');

            if (!container) return;

            container.innerHTML = '';

            if (suggestions.length === 0) {
                if (noSuggestions) noSuggestions.style.display = 'block';
                return;
            }

            if (noSuggestions) noSuggestions.style.display = 'none';

            // Show all suggestions
            suggestions.forEach(suggestion => {
                const card = SuggestionsService.createEquipmentSuggestionCard(suggestion);
                container.appendChild(card);
            });
        } catch (err) {
            console.error('Error loading equipment suggestions:', err);
        }
    }

    async function loadEquipmentPreview() {
        try {
            const res = await fetch(`/api/rentals/facility/${facilityId}/equipments`, { headers: authHeaders() });
            const equipments = await res.json();

            const container = document.getElementById('equipment-preview');
            container.innerHTML = '';

            if (equipments.length === 0) {
                document.getElementById('no-equipments').style.display = 'block';
                return;
            }

            // Show first 3 equipments
            equipments.slice(0, 3).forEach(eq => {
                const div = document.createElement('div');
                div.className = 'col-md-4';
                div.innerHTML = `
                    <div class="card border h-100 shadow-sm" style="border-radius: 16px;">
                        <div class="card-body">
                            <h6 class="fw-bold">${eq.name}</h6>
                            <p class="text-muted small mb-2 description-truncate">${eq.description || 'Sem descrição'}</p>
                            <div class="d-flex justify-content-between align-items-center mt-3">
                                <span class="badge ${eq.status === 'AVAILABLE' ? 'bg-success' : 'bg-secondary'}">${eq.status}</span>
                                <span class="fw-bold text-accent">€${eq.pricePerHour}/h</span>
                            </div>
                        </div>
                    </div>
                `;
                container.appendChild(div);
            });
        } catch (err) {
            console.error('Error loading equipments:', err);
        }
    }

    const btnBook = document.getElementById('btn-book');
    if (btnBook) {
        btnBook.addEventListener('click', () => {
            window.location.href = `booking.html?facilityId=${facilityId}`;
        });
    }

    const btnViewEquip = document.getElementById('btn-view-equipments');
    if (btnViewEquip) {
        btnViewEquip.addEventListener('click', () => {
            window.location.href = `equipments.html?facilityId=${facilityId}`;
        });
    }

    loadFacility();
})();
