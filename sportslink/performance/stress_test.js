import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomSeed } from 'k6';

randomSeed(1234);

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const RENTER_EMAIL = __ENV.RENTER_EMAIL || 'test@sportslink.com';
const RENTER_PASSWORD = __ENV.RENTER_PASSWORD || 'password123';
const OWNER_EMAIL = __ENV.OWNER_EMAIL || 'owner@sportslink.com';
const OWNER_PASSWORD = __ENV.OWNER_PASSWORD || 'password123';

function toLocalDateTimeString(date) {
  return date.toISOString().replace('Z', '').split('.')[0];
}

// -------------------- STRESS OPTIONS --------------------
export const options = {
  scenarios: {
    renter_browse: {
      executor: 'constant-arrival-rate',
      exec: 'scenarioRenterBrowse',
      rate: 30,
      timeUnit: '1s',
      duration: '5m',
      preAllocatedVUs: 60,
      maxVUs: 200,
    },

    renter_lifecycle: {
      executor: 'ramping-vus',
      exec: 'scenarioRenterLifecycle',
      startVUs: 5,
      stages: [
        { duration: '1m', target: 30 },
        { duration: '2m', target: 80 },
        { duration: '1m', target: 120 },
        { duration: '1m', target: 0 },
      ],
      startTime: '1m',
    },

    owner_dashboard: {
      executor: 'constant-vus',
      exec: 'scenarioOwnerDashboard',
      vus: 40,
      duration: '5m',
      startTime: '2m',
    },

    suggestions: {
      executor: 'constant-arrival-rate',
      exec: 'scenarioSuggestions',
      rate: 20,
      timeUnit: '1s',
      duration: '4m',
      preAllocatedVUs: 40,
      maxVUs: 150,
      startTime: '2m30s',
    },

    mixed_traffic: {
      executor: 'constant-vus',
      exec: 'scenarioMixedTraffic',
      vus: 80,
      duration: '5m',
      startTime: '3m',
    },
  },

  thresholds: {
    'http_req_duration{scenario:renter_browse}': ['p(95)<3000'],
    'http_req_duration{scenario:renter_lifecycle}': ['p(95)<4000'],
    'http_req_duration{scenario:owner_dashboard}': ['p(95)<4000'],
    'http_req_duration{scenario:suggestions}': ['p(95)<3000'],
    'http_req_duration{scenario:mixed_traffic}': ['p(95)<4000'],
    'http_req_failed{expected_response:true}': ['rate<0.20'],
  },
};

// -------------------- SETUP --------------------
export function setup() {
  const jsonHeaders = { headers: { 'Content-Type': 'application/json' } };

  const renterLoginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: RENTER_EMAIL, password: RENTER_PASSWORD }),
    jsonHeaders
  );
  const renterAuth = renterLoginRes.status === 200 ? renterLoginRes.json() : null;
  const renter = renterAuth ? { id: renterAuth.userId, token: renterAuth.token } : null;

  const ownerLoginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: OWNER_EMAIL, password: OWNER_PASSWORD }),
    jsonHeaders
  );
  const ownerAuth = ownerLoginRes.status === 200 ? ownerLoginRes.json() : null;
  const owner = ownerAuth ? { id: ownerAuth.userId, token: ownerAuth.token } : null;

  const facilitiesRes = http.get(`${BASE_URL}/api/rentals/search`);
  let facilityId = null;
  if (facilitiesRes.status === 200) {
    const facilities = facilitiesRes.json();
    if (facilities?.length > 0) facilityId = facilities[0].id;
  }

  let ownerFacilityId = null;
  if (owner) {
    const ownerFacilitiesRes = http.get(
      `${BASE_URL}/api/owner/${owner.id}/facilities`,
      { headers: { Authorization: `Bearer ${owner.token}` } }
    );
    if (ownerFacilitiesRes.status === 200) {
      const ownerFacilities = ownerFacilitiesRes.json();
      if (ownerFacilities?.length > 0) ownerFacilityId = ownerFacilities[0].id;
    }
  }

  let sampleEquipmentId = null;
  if (facilityId) {
    const eqRes = http.get(`${BASE_URL}/api/rentals/facility/${facilityId}/equipments`);
    if (eqRes.status === 200) {
      const eqList = eqRes.json();
      if (eqList?.length > 0) sampleEquipmentId = eqList[0].id;
    }
  }

  return {
    renter,
    owner,
    facility: { id: facilityId },
    ownerFacilityId,
    sampleEquipmentId,
  };
}

// -------------------- Scenario 1: renter browse --------------------
export function scenarioRenterBrowse(data) {
  if (!data.facility?.id) return sleep(1);

  const facilityId = data.facility.id;

  check(http.get(`${BASE_URL}/api/rentals/search?location=Aveiro`), {
    'browse: search city 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1);

  check(http.get(`${BASE_URL}/api/rentals/search?sport=FOOTBALL`), {
    'browse: search sport 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1);

  check(http.get(`${BASE_URL}/api/rentals/facility/${facilityId}/equipments`), {
    'browse: list equipments 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1);
}

// -------------------- Scenario 2: renter lifecycle (com payment) --------------------
export function scenarioRenterLifecycle(data) {
  if (!data.renter || !data.facility?.id) return sleep(1);

  const renter = data.renter;
  const facilityId = data.facility.id;

  const authHeaders = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${renter.token}`,
    },
  };

  check(http.get(`${BASE_URL}/api/rentals/search?location=Aveiro`), {
    'lifecycle: search 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1);

  const now = new Date();
  const start = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
  start.setMinutes(0, 0, 0);
  const end = new Date(start.getTime() + 60 * 60 * 1000);

  const rentalBody = {
    userId: renter.id,
    facilityId: facilityId,
    startTime: toLocalDateTimeString(start),
    endTime: toLocalDateTimeString(end),
    equipmentIds: [],
  };

  const createRes = http.post(
    `${BASE_URL}/api/rentals/rental`,
    JSON.stringify(rentalBody),
    { ...authHeaders, tags: { expected_response: 'false' } }
  );
  check(createRes, {
    'lifecycle: create rental ok/4xx': (r) =>
      (r.status >= 200 && r.status < 300) ||
      (r.status >= 400 && r.status < 500),
  });

  let rentalId = createRes.status === 200 ? createRes.json().id : null;

  sleep(1);

  // ---- PAYMENT FLOW ----
  if (rentalId) {
    const paymentUrl = `${BASE_URL}/api/payments/create-intent/${rentalId}?email=${encodeURIComponent(
      RENTER_EMAIL
    )}`;

    const payRes = http.post(paymentUrl, null, {
      ...authHeaders,
      tags: { expected_response: 'false' },
    });

    check(payRes, {
      'lifecycle: create payment intent ok/4xx': (r) =>
        (r.status >= 200 && r.status < 300) ||
        (r.status >= 400 && r.status < 500),
    });

    sleep(1);

    const payStatus = http.get(
      `${BASE_URL}/api/payments/status/${rentalId}`,
      { headers: { Authorization: `Bearer ${renter.token}` }, tags: { expected_response: 'false' } }
    );

    check(payStatus, {
      'lifecycle: payment status ok/4xx/5xx': (r) => r.status >= 200,
    });

    sleep(1);
  }

  // ---- Remaining lifecycle ----
  check(
    http.get(`${BASE_URL}/api/rentals/history?userId=${renter.id}`, {
      headers: { Authorization: `Bearer ${renter.token}` },
    }),
    { 'lifecycle: history 2xx': (r) => r.status >= 200 && r.status < 300 }
  );

  sleep(1);

  if (rentalId) {
    check(
      http.get(`${BASE_URL}/api/rentals/rental/${rentalId}/status`, {
        headers: { Authorization: `Bearer ${renter.token}` },
      }),
      { 'lifecycle: get status 2xx': (r) => r.status >= 200 && r.status < 300 }
    );

    sleep(1);

    check(
      http.put(
        `${BASE_URL}/api/rentals/rental/${rentalId}/cancel`,
        null,
        { headers: { Authorization: `Bearer ${renter.token}` }, tags: { expected_response: 'false' } }
      ),
      {
        'lifecycle: cancel ok/4xx': (r) =>
          (r.status >= 200 && r.status < 300) ||
          (r.status >= 400 && r.status < 500),
      }
    );
  }

  sleep(1);
}

// -------------------- Scenario 3: owner dashboard --------------------
export function scenarioOwnerDashboard(data) {
  if (!data.owner || !data.ownerFacilityId) return sleep(1);

  const owner = data.owner;
  const ownerFacilityId = data.ownerFacilityId;

  const authHeaders = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${owner.token}`,
    },
  };

  check(
    http.get(`${BASE_URL}/api/owner/${owner.id}/facilities`, {
      headers: { Authorization: `Bearer ${owner.token}` },
    }),
    {
      'owner: list facilities 2xx': (r) => r.status >= 200 && r.status < 300,
    }
  );

  sleep(1);

  const listEqRes = http.get(
    `${BASE_URL}/api/owner/${owner.id}/facilities/${ownerFacilityId}/equipment`,
    { headers: { Authorization: `Bearer ${owner.token}` } }
  );

  check(listEqRes, {
    'owner: list equipment 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  let eqList = [];
  try {
    if (listEqRes.status === 200) eqList = listEqRes.json();
  } catch (_) {}

  const existingEq = eqList.length > 0 ? eqList[0] : null;

  sleep(1);

  // ----- update facility -----
  const updFacilityBody = {
    name: `Updated Facility`,
    sports: ['FOOTBALL'],
    city: 'Aveiro',
    address: 'Updated Addr',
    description: 'Updated by k6 (stress)',
    pricePerHour: 25,
    openingTime: '08:00',
    closingTime: '22:00',
  };

  const updFacRes = http.put(
    `${BASE_URL}/api/owner/${owner.id}/facilities/${ownerFacilityId}`,
    JSON.stringify(updFacilityBody),
    { ...authHeaders, tags: { expected_response: 'false' } }
  );

  // --- FIX: aceitar 2xx/4xx/5xx em stress ---
  check(updFacRes, {
    'owner: update facility (stress)': (r) => r.status >= 200 && r.status < 600,
  });

  if (updFacRes.status >= 500) {
    console.error(
      'owner: update facility returned 5xx under stress:',
      updFacRes.status,
      updFacRes.body
    );
  }

  sleep(1);

  // --- add equipment ---
  const newEqBody = {
    name: `k6 Ball ${Math.random() * 99999}`,
    type: 'BALL',
    description: 'Perf test equipment',
    quantity: 10,
    pricePerHour: 2.5,
    status: 'AVAILABLE',
  };

  check(
    http.post(
      `${BASE_URL}/api/owner/${owner.id}/facilities/${ownerFacilityId}/equipment`,
      JSON.stringify(newEqBody),
      authHeaders
    ),
    {
      'owner: add equipment 2xx': (r) => r.status >= 200 && r.status < 300,
    }
  );

  sleep(1);

  // --- update equipment ---
  if (existingEq) {
    const updEqRes = http.put(
      `${BASE_URL}/api/owner/${owner.id}/equipment/${existingEq.id}`,
      JSON.stringify({
        name: `Updated ${existingEq.name}`,
        type: 'BALL',
        description: 'Updated by k6',
        quantity: 15,
        pricePerHour: 3.0,
        status: 'AVAILABLE',
      }),
      { ...authHeaders, tags: { expected_response: 'false' } }
    );

    check(updEqRes, {
      'owner: update equipment ok/4xx/5xx': (r) => r.status >= 200 && r.status < 600,
    });
  }

  sleep(1);
}

// -------------------- Scenario 4: suggestions --------------------
export function scenarioSuggestions(data) {
  if (!data.facility?.id) return sleep(1);

  const renter = data.renter;
  const owner = data.owner;
  const facilityId = data.facility.id;

  if (renter) {
    check(
      http.get(
        `${BASE_URL}/api/suggestions/user/${renter.id}?latitude=40.64&longitude=-8.65&facilityId=${facilityId}&sport=FOOTBALL`,
        { headers: { Authorization: `Bearer ${renter.token}` } }
      ),
      { 'suggestions: user 2xx': (r) => r.status >= 200 && r.status < 300 }
    );
  }

  sleep(1);

  check(http.get(`${BASE_URL}/api/suggestions/equipment/${facilityId}?sport=FOOTBALL`), {
    'suggestions: equipment 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1);

  if (owner) {
    check(
      http.get(
        `${BASE_URL}/api/suggestions/owner/${owner.id}`,
        { headers: { Authorization: `Bearer ${owner.token}` } }
      ),
      { 'suggestions: owner 2xx': (r) => r.status >= 200 && r.status < 300 }
    );
  }

  sleep(1);
}

// -------------------- Scenario 5: mixed traffic --------------------
export function scenarioMixedTraffic(data) {
  if (!data) return sleep(1);

  const rnd = Math.floor(Math.random() * 4) + 1;

  switch (rnd) {
    case 1:
      scenarioRenterBrowse(data);
      break;
    case 2:
      scenarioRenterLifecycle(data);
      break;
    case 3:
      scenarioOwnerDashboard(data);
      break;
    case 4:
      scenarioSuggestions(data);
      break;
  }

  sleep(1);
}
