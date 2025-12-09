/**
 * Suggestions Service - Intelligent Engine Integration
 * Provides personalized facility and equipment recommendations
 */

const SuggestionsService = (() => {
  const BASE_URL = '/api/suggestions';

  /**
   * Get authenticated user ID from localStorage
   * @returns {number|null} User ID or null if not authenticated
   */
  function getAuthenticatedUserId() {
    const userId = localStorage.getItem('userId');
    return userId ? parseInt(userId, 10) : null;
  }

  /**
   * Fetch personalized facility suggestions for a user
   * @param {number} userId - User ID (optional, defaults to authenticated user)
   * @param {Object} location - Optional {latitude, longitude}
   * @returns {Promise<Array>} Facility suggestions
   */
  async function getFacilitySuggestions(userId = null, location = null) {
    const effectiveUserId = userId || getAuthenticatedUserId();
    if (!effectiveUserId) {
      console.log('No authenticated user, skipping facility suggestions');
      return [];
    }
    try {
      const params = new URLSearchParams();
      if (location?.latitude && location?.longitude) {
        params.set('latitude', location.latitude);
        params.set('longitude', location.longitude);
      }

      const url = `${BASE_URL}/facilities/${effectiveUserId}${params.toString() ? '?' + params.toString() : ''}`;
      const response = await fetch(url, {
        headers: typeof authHeaders === 'function' ? authHeaders() : {}
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch suggestions: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching facility suggestions:', error);
      return [];
    }
  }

  /**
   * Fetch equipment suggestions for a facility and sport
   * @param {number} facilityId - Facility ID
   * @param {string} sport - Sport name (FOOTBALL, BASKETBALL, etc.)
   * @returns {Promise<Array>} Equipment suggestions
   */
  async function getEquipmentSuggestions(facilityId, sport) {
    try {
      if (!facilityId || !sport) {
        throw new Error('Facility ID and sport are required');
      }

      const params = new URLSearchParams({ sport: sport.toUpperCase() });
      const url = `${BASE_URL}/equipment/${facilityId}?${params.toString()}`;
      const response = await fetch(url);

      if (!response.ok) {
        throw new Error(`Failed to fetch equipment suggestions: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching equipment suggestions:', error);
      return [];
    }
  }

  /**
   * Fetch combined user suggestions (facilities + equipment)
   * @param {number} userId - User ID
   * @param {Object} options - { latitude, longitude, facilityId, sport }
   * @returns {Promise<Object>} Combined suggestions
   */
  async function getUserSuggestions(userId = null, options = {}) {
    const effectiveUserId = userId || getAuthenticatedUserId();
    if (!effectiveUserId) {
      console.log('No authenticated user, skipping user suggestions');
      return { facilitySuggestions: [], equipmentSuggestions: [] };
    }

    try {
      const params = new URLSearchParams();

      if (options.latitude && options.longitude) {
        params.set('latitude', options.latitude);
        params.set('longitude', options.longitude);
      }

      if (options.facilityId) {
        params.set('facilityId', options.facilityId);
      }

      if (options.sport) {
        params.set('sport', options.sport.toUpperCase());
      }

      const url = `${BASE_URL}/user/${effectiveUserId}${params.toString() ? '?' + params.toString() : ''}`;
      const response = await fetch(url, {
        headers: typeof authHeaders === 'function' ? authHeaders() : {}
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch user suggestions: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching user suggestions:', error);
      return {
        facilitySuggestions: [],
        equipmentSuggestions: []
      };
    }
  }

  /**
   * Create a facility card element from suggestion data
   * @param {Object} suggestion - Facility suggestion
   * @returns {HTMLElement} Card element
   */
  function createSuggestionCard(suggestion) {
    const div = document.createElement('div');
    div.className = 'col';
    div.dataset.id = suggestion.facilityId;

    const card = document.createElement('div');
    card.className = 'field-card card border-0 shadow-sm h-100';

    // Add suggestion badge
    const badge = suggestion.score >= 80
      ? '<span class="badge bg-success position-absolute top-0 start-0 m-2">Top Match!</span>'
      : suggestion.score >= 60
        ? '<span class="badge bg-info position-absolute top-0 start-0 m-2">Recommended</span>'
        : '';

    // Distance info
    const distanceInfo = suggestion.distanceKm
      ? `<div class="text-muted small mb-2">
           <i class="material-icons icon-small">place</i>
           ${suggestion.distanceKm < 1
        ? `${Math.round(suggestion.distanceKm * 1000)}m away`
        : `${suggestion.distanceKm.toFixed(1)}km away`}
         </div>`
      : '';

    // Map sports to icon (handle both sports array and sportType for compatibility)
    const sportIcons = {
      'FOOTBALL': 'sports_soccer',
      'PADEL': 'sports_tennis',
      'TENNIS': 'sports_tennis',
      'BASKETBALL': 'sports_basketball',
      'VOLLEYBALL': 'sports_volleyball',
      'SWIMMING': 'pool'
    };

    // Determine sport icon (check sports array or sportType, default to generic sport icon)
    let sport = 'SPORTS';
    if (Array.isArray(suggestion.sports) && suggestion.sports.length > 0) {
      sport = suggestion.sports[0];
    } else if (suggestion.sportType) {
      sport = suggestion.sportType;
    }
    const icon = sportIcons[sport] || 'sports';

    // Generate image content with fallback
    let imageContent;
    if (suggestion.imageUrl) {
      imageContent = `<img src="${suggestion.imageUrl}" class="w-100 h-100 object-fit-cover" alt="${suggestion.name}" onerror="this.parentElement.innerHTML='<div class=\\'w-100 h-100 d-flex align-items-center justify-content-center bg-light\\'><i class=\\'material-icons text-muted opacity-50 icon-xlarge\\' style=\\'font-size: 64px;\\'>${icon}</i></div>'">`;
    } else {
      imageContent = `<div class="w-100 h-100 d-flex align-items-center justify-content-center bg-light">
                          <i class="material-icons text-muted opacity-50 icon-xlarge" style="font-size: 64px;">${icon}</i>
                        </div>`;
    }

    card.innerHTML = `
      ${badge}
      <div class="field-image card-img-top position-relative" style="height: 200px; overflow: hidden;">
        ${imageContent}
      </div>
      <div class="card-body">
        <h5 class="card-title fw-bold text-accent-dark">${suggestion.name}</h5>
        ${distanceInfo}
        <div class="field-location d-flex align-items-center gap-1 text-muted small mb-2">
          <i class="material-icons icon-small">location_on</i>
          ${suggestion.city}
        </div>
        <div class="suggestion-reason text-primary small mb-3">
          <i class="material-icons icon-small">lightbulb</i>
          ${suggestion.reason}
        </div>
        <div class="d-flex justify-content-between align-items-center pt-3 border-top">
          <div class="fw-bold text-accent-dark">€${suggestion.pricePerHour}/hour</div>
          <div class="text-warning">
            ${'★'.repeat(Math.floor(suggestion.rating || 0))}
            ${(suggestion.rating || 0).toFixed(1)}
          </div>
        </div>
      </div>
    `;

    // Click to view details
    card.addEventListener('click', () => {
      window.location.href = `field_detail.html?id=${suggestion.facilityId}`;
    });

    div.appendChild(card);
    return div;
  }

  /**
   * Create equipment suggestion card
   * @param {Object} equipment - Equipment suggestion
   * @returns {HTMLElement} Card element
   */
  function createEquipmentSuggestionCard(equipment) {
    const div = document.createElement('div');
    div.className = 'col-md-6 col-lg-4';

    const isRecommended = equipment.score >= 80;
    const badgeClass = isRecommended ? 'bg-success' : 'bg-info';
    const badgeText = isRecommended ? 'Highly Recommended' : 'Suggested';

    div.innerHTML = `
      <div class="card border h-100 shadow-sm" style="border-radius: 16px;">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <h6 class="fw-bold mb-0">${equipment.name}</h6>
            <span class="badge ${badgeClass}">${badgeText}</span>
          </div>
          <p class="text-muted small mb-1">${equipment.type}</p>
          <p class="text-primary small mb-3">
            <i class="material-icons icon-small">stars</i>
            ${equipment.reason}
          </p>
          <div class="d-flex justify-content-between align-items-center">
            <span class="text-muted small">Qty: ${equipment.quantity}</span>
            <span class="fw-bold text-accent">€${equipment.pricePerHour}/h</span>
          </div>
        </div>
      </div>
    `;

    return div;
  }

  // Public API
  return {
    getFacilitySuggestions,
    getEquipmentSuggestions,
    getUserSuggestions,
    createSuggestionCard,
    createEquipmentSuggestionCard
  };
})();

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = SuggestionsService;
}
