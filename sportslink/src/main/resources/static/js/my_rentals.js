/**
 * My Rentals Page Logic
 * Handles fetching, displaying, and managing user rentals.
 */

document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');

    if (!token || !userId) {
        window.location.href = '/index.html';
        return;
    }

    // Initialize tabs
    const tabEls = document.querySelectorAll('button[data-bs-toggle="tab"]');
    tabEls.forEach(tabEl => {
        tabEl.addEventListener('shown.bs.tab', (event) => {
            // Optional: refresh data on tab switch if needed
        });
    });

    // Load rentals
    await loadUserRentals(userId, token);
});

async function loadUserRentals(userId, token) {
    const activeList = document.getElementById('active-rentals-list');
    const pastList = document.getElementById('past-rentals-list');

    // Show loading
    activeList.innerHTML = '<div class="loading-container"><div class="spinner-border text-accent" role="status"></div></div>';
    pastList.innerHTML = '<div class="loading-container"><div class="spinner-border text-accent" role="status"></div></div>';

    try {
        const response = await fetch(`/api/rentals/history?userId=${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch rentals');
        }

        const rentals = await response.json();

        // Split into active (future) and past
        const now = new Date();
        const activeRentals = rentals.filter(r => new Date(r.endTime) > now);
        const pastRentals = rentals.filter(r => new Date(r.endTime) <= now);

        // Sort active: soonest first
        activeRentals.sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

        // Sort past: most recent first
        pastRentals.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));

        renderRentals(activeRentals, activeList, true);
        renderRentals(pastRentals, pastList, false);

    } catch (error) {
        console.error('Error loading rentals:', error);
        const errorHtml = '<div class="alert alert-danger">Failed to load rentals. Please try again later.</div>';
        activeList.innerHTML = errorHtml;
        pastList.innerHTML = errorHtml;
    }
}

function renderRentals(rentals, container, isActive) {
    if (rentals.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="material-icons">event_busy</i>
                <p class="text-muted fw-medium mb-0">No rentals found.</p>
                ${isActive ? '<a href="/pages/main_page_user.html" class="btn btn-outline-primary mt-3">Book a Field</a>' : ''}
            </div>
        `;
        return;
    }

    container.innerHTML = '';
    const row = document.createElement('div');
    row.className = 'row g-4';

    rentals.forEach(rental => {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';

        const startTime = new Date(rental.startTime);
        const endTime = new Date(rental.endTime);

        const dateStr = startTime.toLocaleDateString('en-GB', {
            weekday: 'short', year: 'numeric', month: 'short', day: 'numeric'
        });

        const timeStr = `${formatTime(startTime)} - ${formatTime(endTime)}`;

        const statusBadge = getStatusBadge(rental.status, isActive);

        col.innerHTML = `
            <div class="card h-100 rental-card shadow-sm" onclick="openRentalDetails(${rental.id})">
                <div class="card-body p-4">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        ${statusBadge}
                        <small class="text-muted fw-semibold">#${rental.id}</small>
                    </div>
                    
                    <h5 class="facility-name mb-2">Facility #${rental.facilityId}</h5> <!-- In real app, we'd need facility name included in DTO -->
                    
                    <div class="d-flex align-items-center mb-2 text-muted">
                        <i class="material-icons fs-5 me-2">calendar_today</i>
                        <span class="rental-date">${dateStr}</span>
                    </div>
                    
                    <div class="d-flex align-items-center mb-3 text-muted">
                        <i class="material-icons fs-5 me-2">schedule</i>
                        <span class="rental-time">${timeStr}</span>
                    </div>

                    ${rental.equipments && rental.equipments.length > 0 ? `
                    <div class="d-flex align-items-center text-muted small">
                        <i class="material-icons fs-6 me-2">sports_tennis</i>
                        <span>${rental.equipments.length} items rented</span>
                    </div>
                    ` : ''}
                </div>
                <div class="card-footer bg-transparent border-top-0 pb-4 px-4 pt-0">
                    <button class="btn btn-outline-secondary btn-sm w-100 rounded-pill">View Details</button>
                </div>
            </div>
        `;
        row.appendChild(col);
    });

    container.appendChild(row);
}

function getStatusBadge(status, isActive) {
    if (status === 'CANCELLED') {
        return '<span class="badge badge-cancelled">Cancelled</span>';
    }
    if (!isActive) {
        return '<span class="badge badge-past">Completed</span>';
    }
    return '<span class="badge badge-confirmed">Confirmed</span>';
}

function formatTime(date) {
    return date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
}

async function openRentalDetails(rentalId) {
    // Redirect to the new edit/details page
    window.location.href = `/pages/edit_booking.html?id=${rentalId}`;
}

// End of file
