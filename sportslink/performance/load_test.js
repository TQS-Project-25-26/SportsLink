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

export const options = {
  scenarios: {
    renter_browse: {
      executor: 'constant-arrival-rate',
      exec: 'scenarioRenterBrowse',
      rate: 5,
      timeUnit: '1s',
      duration: '2m',
      preAllocatedVUs: 10,
      maxVUs: 30,
    },
    renter_lifecycle: {
      executor: 'ramping-vus',
      exec: 'scenarioRenterLifecycle',
      startVUs: 1,
      stages: [
        { duration: '30s', target: 5 },
        { duration: '1m30s', target: 10 },
        { duration: '30s', target: 0 },
      ],
      startTime: '30s',
    },
    owner_dashboard: {
      executor: 'constant-vus',
      exec: 'scenarioOwnerDashboard',
      vus: 3,
      duration: '3m',
      startTime: '1m',
    },
    suggestions: {
      executor: 'constant-arrival-rate',
      exec: 'scenarioSuggestions',
      rate: 3,
      timeUnit: '1s',
      duration: '2m',
      preAllocatedVUs: 6,
      maxVUs: 15,
      startTime: '90s',
    },
    mixed_traffic: {
      executor: 'constant-vus',
      exec: 'scenarioMixedTraffic',
      vus: 10,
      duration: '3m',
      startTime: '2m',
    },
  },
  thresholds: {
    'http_req_duration{scenario:renter_browse}': ['p(95)<800'],
    'http_req_duration{scenario:renter_lifecycle}': ['p(95)<1500'],
    'http_req_duration{scenario:owner_dashboard}': ['p(95)<1500'],
    'http_req_duration{scenario:suggestions}': ['p(95)<1000'],
    'http_req_duration{scenario:mixed_traffic}': ['p(95)<1500'],
    'http_req_failed{expected_response:true}': ['rate<0.05'],
  },
};

// -------------------- SETUP --------------------
export function setup() {
  const jsonHeaders = { headers: { 'Content-Type': 'application/json' } };

  const renterLoginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: RENTER_EMAIL, password: RENTER_PASSWORD }),
    jsonHeaders,
  );
  if (renterLoginRes.status !== 200) {
    console.error('LOGIN RENTER FAILED', renterLoginRes.status, renterLoginRes.body);
  }
  const renterAuth =
    renterLoginRes.status === 200 ? renterLoginRes.json() : null;
  const renter = renterAuth
    ? { id: renterAuth.userId, token: renterAuth.token }
    : null;

  const ownerLoginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: OWNER_EMAIL, password: OWNER_PASSWORD }),
    jsonHeaders,
  );
  if (ownerLoginRes.status !== 200) {
    console.error('LOGIN OWNER FAILED', ownerLoginRes.status, ownerLoginRes.body);
  }
  const ownerAuth =
    ownerLoginRes.status === 200 ? ownerLoginRes.json() : null;
  const owner = ownerAuth
    ? { id: ownerAuth.userId, token: ownerAuth.token }
    : null;

  const facilitiesRes = http.get(`${BASE_URL}/api/rentals/search`);
  if (facilitiesRes.status !== 200) {
    console.error(
      'FACILITY SEARCH IN SETUP FAILED',
      facilitiesRes.status,
      facilitiesRes.body,
    );
  }

  let facilityId = null;
  if (facilitiesRes.status === 200) {
    const facilities = facilitiesRes.json();
    if (facilities && facilities.length > 0) {
      facilityId = facilities[0].id;
    }
  }

  let ownerFacilityId = null;
  if (owner && owner.id) {
    const ownerFacilitiesRes = http.get(
      `${BASE_URL}/api/owner/${owner.id}/facilities`,
      { headers: { Authorization: `Bearer ${owner.token}` } },
    );
    if (ownerFacilitiesRes.status === 200) {
      const ownerFacilities = ownerFacilitiesRes.json();
      if (ownerFacilities && ownerFacilities.length > 0) {
        ownerFacilityId = ownerFacilities[0].id;
      }
    } else {
      console.error(
        'OWNER FACILITIES IN SETUP FAILED',
        ownerFacilitiesRes.status,
        ownerFacilitiesRes.body,
      );
    }
  }

  let sampleEquipmentId = null;
  if (facilityId) {
    const eqRes = http.get(
      `${BASE_URL}/api/rentals/facility/${facilityId}/equipments`,
    );
    if (eqRes.status === 200) {
      const eqList = eqRes.json();
      if (eqList && eqList.length > 0) {
        sampleEquipmentId = eqList[0].id;
      }
    }
  }

  console.log(
    `SETUP SUMMARY: renter=${renter ? renter.id : 'null'}, owner=${
      owner ? owner.id : 'null'
    }, facilityId=${facilityId}, ownerFacilityId=${ownerFacilityId}, sampleEquipmentId=${sampleEquipmentId}`,
  );

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
  if (!data || !data.facility || !data.facility.id) {
    sleep(1);
    return;
  }

  const facilityId = data.facility.id;

  const resSearchCity = http.get(
    `${BASE_URL}/api/rentals/search?location=Aveiro`,
  );
  check(resSearchCity, {
    'browse: search by city 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1);

  const resSearchSport = http.get(
    `${BASE_URL}/api/rentals/search?sport=FOOTBALL`,
  );
  check(resSearchSport, {
    'browse: search by sport 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1);

  const resEquipments = http.get(
    `${BASE_URL}/api/rentals/facility/${facilityId}/equipments`,
  );
  check(resEquipments, {
    'browse: list equipments 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1);
}

// -------------------- Scenario 2: renter lifecycle (com payment) --------------------
export function scenarioRenterLifecycle(data) {
  if (!data || !data.renter || !data.renter.id || !data.facility || !data.facility.id) {
    sleep(1);
    return;
  }

  const renter = data.renter;
  const facilityId = data.facility.id;

  const authHeaders = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${renter.token}`,
    },
  };

  const resSearch = http.get(`${BASE_URL}/api/rentals/search?location=Aveiro`);
  check(resSearch, {
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
    Object.assign({}, authHeaders, { tags: { expected_response: 'false' } }),
  );
  check(createRes, {
    'lifecycle: create rental 200/4xx expected': (r) =>
      r.status === 200 || (r.status >= 400 && r.status < 500),
  });

  let rentalId = null;
  if (createRes.status === 200) {
    rentalId = createRes.json().id;
  }

  sleep(1);

  // Fluxo payment
  if (rentalId) {
    // criar PaymentIntent
    const paymentUrl = `${BASE_URL}/api/payments/create-intent/${rentalId}?email=${encodeURIComponent(
      RENTER_EMAIL,
    )}`;

    const createPaymentRes = http.post(
      paymentUrl,
      null,
      Object.assign({}, authHeaders, { tags: { expected_response: 'false' } }),
    );

    check(createPaymentRes, {
      'lifecycle: create payment intent 200/4xx expected': (r) =>
        r.status === 200 || (r.status >= 400 && r.status < 500),
    });

    sleep(1);

    // consultar estado do pagamento
    const statusPayRes = http.get(
      `${BASE_URL}/api/payments/status/${rentalId}`,
      Object.assign(
        {},
        { headers: { Authorization: `Bearer ${renter.token}` } },
        { tags: { expected_response: 'false' } },
      ),
    );

    check(statusPayRes, {
      'lifecycle: payment status 2xx/4xx expected': (r) =>
        (r.status >= 200 && r.status < 300) ||
        (r.status >= 400 && r.status < 500),
    });

    sleep(1);
  }

  // restante lifecycle
  const historyRes = http.get(
    `${BASE_URL}/api/rentals/history?userId=${renter.id}`,
    { headers: { Authorization: `Bearer ${renter.token}` } },
  );
  check(historyRes, {
    'lifecycle: history 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1);

  if (rentalId) {
    const statusRes = http.get(
      `${BASE_URL}/api/rentals/rental/${rentalId}/status`,
      { headers: { Authorization: `Bearer ${renter.token}` } },
    );
    check(statusRes, {
      'lifecycle: get status 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    sleep(1);

    const cancelRes = http.put(
      `${BASE_URL}/api/rentals/rental/${rentalId}/cancel`,
      null,
      Object.assign(
        {},
        { headers: { Authorization: `Bearer ${renter.token}` } },
        { tags: { expected_response: 'false' } },
      ),
    );
    check(cancelRes, {
      'lifecycle: cancel 2xx/4xx': (r) =>
        (r.status >= 200 && r.status < 300) ||
        (r.status >= 400 && r.status < 500),
    });
  }

  sleep(1);
}

// -------------------- Scenario 3: owner dashboard --------------------
export function scenarioOwnerDashboard(data) {
  try {
    if (!data) {
      sleep(1);
      return;
    }
    if (!data.owner || !data.owner.id || !data.ownerFacilityId) {
      sleep(1);
      return;
    }

    const owner = data.owner;
    const ownerFacilityId = data.ownerFacilityId;

    const authHeaders = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${owner.token}`,
      },
    };

    const facilitiesRes = http.get(
      `${BASE_URL}/api/owner/${owner.id}/facilities`,
      { headers: { Authorization: `Bearer ${owner.token}` } },
    );
    check(facilitiesRes, {
      'owner: list facilities 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    let facility = null;
    if (facilitiesRes.status === 200) {
      const facilities = facilitiesRes.json();
      if (facilities && facilities.length > 0) {
        facility = facilities[0];
      }
    }

    sleep(1);

    const listEqRes = http.get(
      `${BASE_URL}/api/owner/${owner.id}/facilities/${ownerFacilityId}/equipment`,
      { headers: { Authorization: `Bearer ${owner.token}` } },
    );
    check(listEqRes, {
      'owner: list equipment 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    let eqList = [];
    if (listEqRes.status === 200) {
      try {
        eqList = listEqRes.json();
      } catch (e) {
        console.error('owner: error parsing equipment list JSON', e);
      }
    }
    const existingEq = eqList && eqList.length > 0 ? eqList[0] : null;

    sleep(1);

    if (facility) {
      const updateFacilityBody = {
        name: facility.name || 'Updated Facility',
        sports:
          facility.sports && facility.sports.length > 0
            ? facility.sports
            : ['FOOTBALL'],
        city: facility.city || 'Aveiro',
        address: facility.address || 'Updated Address',
        description: (facility.description || '') + ' (updated by k6)',
        pricePerHour: (facility.pricePerHour || 20) + 1,
        openingTime: facility.openingTime || '08:00',
        closingTime: facility.closingTime || '22:00',
      };

      const updFacRes = http.put(
        `${BASE_URL}/api/owner/${owner.id}/facilities/${facility.id}`,
        JSON.stringify(updateFacilityBody),
        Object.assign({}, authHeaders, { tags: { expected_response: 'false' } }),
      );
      check(updFacRes, {
        'owner: update facility 2xx/4xx': (r) =>
          (r.status >= 200 && r.status < 300) ||
          (r.status >= 400 && r.status < 500),
      });
    }

    sleep(1);

    const newEqBody = {
      name: `k6 Ball ${Math.floor(Math.random() * 100000) + 1}`,
      type: 'BALL',
      description: 'Perf test equipment',
      quantity: 10,
      pricePerHour: 2.5,
      status: 'AVAILABLE',
    };

    const addEqRes = http.post(
      `${BASE_URL}/api/owner/${owner.id}/facilities/${ownerFacilityId}/equipment`,
      JSON.stringify(newEqBody),
      authHeaders,
    );
    check(addEqRes, {
      'owner: add equipment 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    let newEqId = null;
    if (addEqRes.status === 200) {
      newEqId = addEqRes.json().id;
    }

    sleep(1);

    const targetEqId = newEqId || (existingEq ? existingEq.id : null);
    if (targetEqId) {
      const updateEqBody = {
        name: existingEq ? 'Updated ' + existingEq.name : 'Updated k6 Equipment',
        type: 'BALL',
        description: 'Updated by k6',
        quantity: 15,
        pricePerHour: 3.0,
        status: 'AVAILABLE',
      };

      const updEqRes = http.put(
        `${BASE_URL}/api/owner/${owner.id}/equipment/${targetEqId}`,
        JSON.stringify(updateEqBody),
        Object.assign({}, authHeaders, { tags: { expected_response: 'false' } }),
      );
      check(updEqRes, {
        'owner: update equipment 2xx/4xx': (r) =>
          (r.status >= 200 && r.status < 300) ||
          (r.status >= 400 && r.status < 500),
      });
    }

    sleep(1);
  } catch (e) {
    console.error('scenarioOwnerDashboard error caught:', String(e));
    sleep(1);
  }
}

// -------------------- Scenario 4: suggestions --------------------
export function scenarioSuggestions(data) {
  if (!data || !data.facility || !data.facility.id) {
    sleep(1);
    return;
  }

  const renter = data.renter;
  const owner = data.owner;
  const facilityId = data.facility.id;

  if (renter && renter.token) {
    const resUserSuggestions = http.get(
      `${BASE_URL}/api/suggestions/user/${renter.id}?latitude=40.64&longitude=-8.65&facilityId=${facilityId}&sport=FOOTBALL`,
      { headers: { Authorization: `Bearer ${renter.token}` } },
    );
    check(resUserSuggestions, {
      'suggestions: user 2xx': (r) => r.status >= 200 && r.status < 300,
    });
  }

  sleep(1);

  const resEqSuggestions = http.get(
    `${BASE_URL}/api/suggestions/equipment/${facilityId}?sport=FOOTBALL`,
  );
  check(resEqSuggestions, {
    'suggestions: equipment 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1);

  if (owner && owner.token) {
    const resOwnerSuggestions = http.get(
      `${BASE_URL}/api/suggestions/owner/${owner.id}`,
      { headers: { Authorization: `Bearer ${owner.token}` } },
    );
    check(resOwnerSuggestions, {
      'suggestions: owner 2xx': (r) => r.status >= 200 && r.status < 300,
    });
  }

  sleep(1);
}

// -------------------- Scenario 5: mixed traffic --------------------
export function scenarioMixedTraffic(data) {
  if (!data) {
    sleep(1);
    return;
  }

  const rnd = Math.floor(Math.random() * 4) + 1; // 1 a 4

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
    default:
      scenarioRenterBrowse(data);
  }

  sleep(1);
}
