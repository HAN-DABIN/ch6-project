import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const SCENARIO = __ENV.K6_SCENARIO || 'mixed';

const scenarioOptions = {
  smoke: {
    executor: 'constant-vus',
    vus: 1,
    duration: '30s',
    exec: 'smokeRead',
  },
  menus: {
    executor: 'constant-vus',
    vus: Number(__ENV.K6_VUS || 10),
    duration: __ENV.K6_DURATION || '1m',
    exec: 'menusOnly',
  },
  popular: {
    executor: 'constant-vus',
    vus: Number(__ENV.K6_VUS || 10),
    duration: __ENV.K6_DURATION || '1m',
    exec: 'popularOnly',
  },
  mixed: {
    executor: 'constant-vus',
    vus: Number(__ENV.K6_VUS || 10),
    duration: __ENV.K6_DURATION || '1m',
    exec: 'mixedRead',
  },
};

export const options = {
  scenarios: {
    [SCENARIO]: scenarioOptions[SCENARIO] || scenarioOptions.mixed,
  },
  thresholds: thresholdsFor(SCENARIO),
};

export function smokeRead() {
  requestMenus();
  requestPopularMenus();
  sleep(1);
}

export function menusOnly() {
  requestMenus();
  sleep(1);
}

export function popularOnly() {
  requestPopularMenus();
  sleep(1);
}

export function mixedRead() {
  if (Math.random() < 0.7) {
    requestMenus();
  } else {
    requestPopularMenus();
  }

  sleep(1);
}

function requestMenus() {
  const response = http.get(`${BASE_URL}/api/menus`, {
    tags: { api: 'menus' },
  });

  check(response, {
    'menus status is 200': (res) => res.status === 200,
    'menus common status is 200': (res) => jsonValue(res, 'status') === 200,
    'menus message matches': (res) => jsonValue(res, 'message') === '메뉴 조회 성공',
    'menus data is array': (res) => Array.isArray(jsonValue(res, 'data')),
  });
}

function requestPopularMenus() {
  const response = http.get(`${BASE_URL}/api/menus/popular`, {
    tags: { api: 'popular-menus' },
  });

  check(response, {
    'popular menus status is 200': (res) => res.status === 200,
    'popular menus common status is 200': (res) => jsonValue(res, 'status') === 200,
    'popular menus message matches': (res) => jsonValue(res, 'message') === '인기 메뉴 조회 성공',
    'popular menus data is array': (res) => Array.isArray(jsonValue(res, 'data')),
  });
}

function jsonValue(response, field) {
  try {
    return response.json(field);
  } catch (e) {
    return undefined;
  }
}

function thresholdsFor(scenario) {
  const thresholds = {
    checks: ['rate>0.99'],
    http_req_failed: ['rate<0.01'],
  };

  if (scenario === 'menus') {
    thresholds['http_req_failed{api:menus}'] = ['rate<0.01'];
    thresholds['http_req_duration{api:menus}'] = ['p(95)<1000'];
    return thresholds;
  }

  if (scenario === 'popular') {
    thresholds['http_req_failed{api:popular-menus}'] = ['rate<0.01'];
    thresholds['http_req_duration{api:popular-menus}'] = ['p(95)<1000'];
    return thresholds;
  }

  thresholds['http_req_failed{api:menus}'] = ['rate<0.01'];
  thresholds['http_req_failed{api:popular-menus}'] = ['rate<0.01'];
  thresholds['http_req_duration{api:menus}'] = ['p(95)<1000'];
  thresholds['http_req_duration{api:popular-menus}'] = ['p(95)<1000'];
  return thresholds;
}
