import http from 'k6/http';
import { sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 1,
  duration: '10s',
  // Sem thresholds – é mesmo só para ver status codes e garantir ligação
};

export default function () {
  const res = http.get(`${BASE_URL}/api/rentals/search`);

  console.log(`GET /api/rentals/search -> status ${res.status}`);

  // Se quiseres testar outro endpoint qualquer, descomenta:
  // const resSports = http.get(`${BASE_URL}/api/rentals/sports`);
  // console.log(`GET /api/rentals/sports -> status ${resSports.status}`);

  sleep(1);
}
