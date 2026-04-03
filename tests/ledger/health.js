import http from 'k6/http';
import { check, sleep } from 'k6';

const TRANSACTION_BASE = 'http://transaction-service:8080';
const LEDGER_BASE = 'http://ledger-service:8082';

export const options = {
  stages: [
    { duration: '20s', target: 10 },
    { duration: '40s', target: 20 },
    { duration: '40s', target: 35 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 50 },
    { duration: '40s', target: 70 },
    { duration: '1m', target: 70 },
    { duration: '40s', target: 30 },
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<1500'],
    http_req_failed: ['rate<0.20'],
  },
};

function asJson(res) {
  try {
    return res.json();
  } catch (e) {
    return null;
  }
}

export default function () {
  const nome = `LedgerUser-${__VU}-${__ITER}-${Date.now()}`;

  // cria usuário para obter UUID válido
  const criarUsuarioRes = http.post(
    `${TRANSACTION_BASE}/usuarios`,
    JSON.stringify({
      nome,
      saldoInicial: 500.0,
    }),
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  check(criarUsuarioRes, {
    'ledger setup -> POST /usuarios 200 ou 201': (r) => r.status === 200 || r.status === 201,
  });

  const usuario = asJson(criarUsuarioRes);
  const accountId = usuario?.id;

  if (!accountId) {
    sleep(1);
    return;
  }

  const balanceRes = http.get(`${LEDGER_BASE}/ledger/queries/balance/${accountId}`);

  check(balanceRes, {
    'GET /ledger/queries/balance/{accountId} -> 200 ou 404': (r) =>
      r.status === 200 || r.status === 404,
  });

  const statementRes = http.get(`${LEDGER_BASE}/ledger/queries/statement/${accountId}`);

  check(statementRes, {
    'GET /ledger/queries/statement/{accountId} -> 200 ou 404': (r) =>
      r.status === 200 || r.status === 404,
  });

  sleep(1);
}