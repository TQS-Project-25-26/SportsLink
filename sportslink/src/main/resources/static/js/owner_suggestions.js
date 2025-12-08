document.addEventListener('DOMContentLoaded', async () => {
    const suggestionsGrid = document.getElementById('suggestions-grid');
    const noSuggestionsDiv = document.getElementById('no-suggestions');

    // Check token existence
    const token = getToken();
    if (!token) {
        window.location.href = '/index.html';
        return;
    }

    try {
        // 1. Get Owner ID from profile
        const user = await fetchUserProfile();
        if (!user || user.role !== 'OWNER') {
            alert("Access denied. Owners only.");
            window.location.href = '/index.html';
            return;
        }

        const ownerId = user.id;

        // 2. Load suggestions
        await loadSuggestions(ownerId);

    } catch (error) {
        console.error('Error initializing:', error);
        suggestionsGrid.innerHTML = `
            <div class="col-12 text-center text-danger">
                <p>Error loading suggestions: ${error.message}</p>
            </div>
        `;
    }

    async function fetchUserProfile() {
        const res = await fetch('/api/auth/profile', {
            headers: authHeaders()
        });

        if (!res.ok) {
            if (res.status === 401) {
                logout();
                return null;
            }
            throw new Error('Failed to load profile');
        }
        return await res.json();
    }

    async function loadSuggestions(ownerId) {
        const response = await fetch(`/api/suggestions/owner/${ownerId}`, {
            headers: authHeaders()
        });

        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return;
            }
            throw new Error('Failed to fetch suggestions');
        }

        const suggestions = await response.json();

        suggestionsGrid.innerHTML = '';

        if (!suggestions || suggestions.length === 0) {
            noSuggestionsDiv.style.display = 'block';
            return;
        } else {
            noSuggestionsDiv.style.display = 'none';
        }

        suggestions.forEach(suggestion => {
            const card = createSuggestionCard(suggestion);
            suggestionsGrid.appendChild(card);
        });
    }

    function createSuggestionCard(suggestion) {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';

        // Map priority to colors
        let priorityColor = 'secondary';
        if (suggestion.priority === 'HIGH') priorityColor = 'danger';
        if (suggestion.priority === 'MEDIUM') priorityColor = 'warning';
        if (suggestion.priority === 'LOW') priorityColor = 'info';

        // Map type to icon
        let icon = 'lightbulb';
        if (suggestion.type === 'ADD_EQUIPMENT') icon = 'inventory_2';
        if (suggestion.type === 'MAINTENANCE') icon = 'build';
        if (suggestion.type === 'LOWER_PRICE') icon = 'sell';

        const revenueBadge = suggestion.potentialRevenue
            ? `<div class="mt-2 text-success fw-bold"><i class="material-icons" style="font-size: 1rem; vertical-align: text-top;">monetization_on</i> Potential Revenue: +€${suggestion.potentialRevenue.toFixed(2)}</div>`
            : '';

        col.innerHTML = `
            <div class="card h-100 shadow-sm border-0 suggestion-card" style="border-radius: 15px; transition: transform 0.2s;">
                <div class="card-body p-4">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <div class="d-flex align-items-center gap-2">
                            <span class="badge bg-${priorityColor} bg-opacity-10 text-${priorityColor} px-3 py-2 rounded-pill">
                                ${suggestion.priority} PRIORITY
                            </span>
                        </div>
                        <i class="material-icons text-muted opacity-25" style="font-size: 2.5rem;">${icon}</i>
                    </div>

                    <h5 class="card-title fw-bold mb-1">${suggestion.title}</h5>
                    <h6 class="text-accent mb-3" style="font-size: 0.9rem;">${suggestion.facilityName}</h6>
                    
                    <p class="card-text text-muted mb-4">
                        ${suggestion.description}
                    </p>

                    ${revenueBadge}
                    
                    <div class="mt-4 pt-3 border-top d-flex justify-content-end">
                        <button class="btn btn-sm btn-outline-primary rounded-pill px-3" onclick="handleAction('${suggestion.type}', ${suggestion.facilityId})">
                            Take Action
                        </button>
                    </div>
                </div>
            </div>
        `;

        return col;
    }

    window.handleAction = function (type, facilityId) {
        // Redirect to dashboard with query parameters to open the specific modal
        if (type === 'ADD_EQUIPMENT') {
            window.location.href = `owner_dashboard.html?action=equipment&facilityId=${facilityId}`;
        } else {
            // Default to edit modal for other maintenance/price suggestions
            window.location.href = `owner_dashboard.html?action=edit&facilityId=${facilityId}`;
        }
    };
});
