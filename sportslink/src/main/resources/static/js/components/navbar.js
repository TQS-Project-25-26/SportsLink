/**
 * Navbar Component
 * Standardizes the navigation bar across all pages.
 */

function renderNavbar() {
    const navbarPlaceholder = document.getElementById('navbar-placeholder');
    if (!navbarPlaceholder) return;

    // Determine current page for active state (optional but good for UX)
    const currentPath = window.location.pathname;

    // Helper to add 'active' class
    const isActive = (path) => currentPath.includes(path) ? 'active' : '';

    const navbarHtml = `
    <nav class="navbar navbar-expand-lg navbar-light sticky-top" style="background-color: white; z-index: 1000;">
        <div class="container-fluid">
            <a class="navbar-brand d-flex align-items-center" href="/pages/main_page_user.html">
                <i class="material-icons me-2 text-accent">sports_soccer</i>
                <span class="text-gradient fw-bold fs-4">SportsLink</span>
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link fw-medium ${isActive('main_page_user.html')}" href="/pages/main_page_user.html">Home</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link fw-medium ${isActive('myBookingsPage.html')}" href="/pages/myBookingsPage.html">My Rentals</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link fw-medium" href="/pages/main_page_user.html#favoritos">Favorites</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link fw-medium ${isActive('owner_dashboard.html')}" href="/pages/owner_dashboard.html" id="facilitiesNavLink">My Facilities</a>
                    </li>
                </ul>
                <div class="d-flex align-items-center">
                    <div class="dropdown">
                        <button id="profileBtn" class="profile-icon d-flex align-items-center justify-content-center text-white fw-bold dropdown-toggle"
                            data-bs-toggle="dropdown" aria-expanded="false">
                            U
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="profileBtn">
                            <li><a class="dropdown-item" href="/pages/profile_page.html" id="profileManage">Profile</a></li>
                            <li><a class="dropdown-item" href="/pages/settings.html">Settings</a></li>
                            <li><a class="dropdown-item" href="mailto:support@sportslink.com">Contact Support</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item text-danger" href="#" id="logoutBtnNav">Logout</a></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </nav>
    `;

    navbarPlaceholder.innerHTML = navbarHtml;

    // Attach Logout Event Listener
    const logoutBtn = document.getElementById('logoutBtnNav');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (typeof logout === 'function') {
                logout();
            } else {
                console.error('Logout function not found. Make sure auth.js is included.');
                // Fallback for safety
                localStorage.removeItem('token');
                localStorage.removeItem('role');
                window.location.href = '/index.html';
            }
        });
    }

    // Optional: Highlighting "My Facilities" behavior if necessary
    // Current requirement says it should just link to owner_dashboard.html, 
    // where the role check happens.
}

// Auto-render if DOM is ready, or wait for it
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', renderNavbar);
} else {
    renderNavbar();
}
