import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomUUID } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE = 'http://transaction-service:8080';

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
    http_req_failed: ['rate<0.15'],
  },
};

function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

function asJson(res) {
  try {
    return res.json();
  } catch (e) {
    return null;
  }
}

export default function () {
  const nome = `Usuario-${__VU}-${__ITER}-${Date.now()}`;
  const saldoInicial = 1000.0;

  // 1) POST /usuarios
  const criarUsuarioRes = http.post(
    `${BASE}/usuarios`,
    JSON.stringify({
      nome,
      saldoInicial,
    }),
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  check(criarUsuarioRes, {
    'POST /usuarios -> 200 ou 201': (r) => r.status === 200 || r.status === 201,
    'POST /usuarios -> retorna JSON': (r) => asJson(r) !== null,
  });

  const usuario = asJson(criarUsuarioRes);
  const usuarioId = usuario?.id;

  if (!usuarioId) {
    sleep(1);
    return;
  }

  // 2) GET /usuarios
  const listarUsuariosRes = http.get(`${BASE}/usuarios`);

  check(listarUsuariosRes, {
    'GET /usuarios -> 200': (r) => r.status === 200,
    'GET /usuarios -> array': (r) => Array.isArray(asJson(r)),
  });

  // 3) GET /usuarios/{id}/saldo
  const saldoRes = http.get(`${BASE}/usuarios/${usuarioId}/saldo`);

  check(saldoRes, {
    'GET /usuarios/{id}/saldo -> 200': (r) => r.status === 200,
    'GET /usuarios/{id}/saldo -> retorna JSON': (r) => asJson(r) !== null,
  });

  // 4) POST /transacoes
  const transacaoRes = http.post(
    `${BASE}/transacoes`,
    JSON.stringify({
      usuarioId: usuarioId, // UUID
      chavePixDestino: `destino-${__VU}-${__ITER}@teste.com`,
      valor: 25.0,
      descricao: `Pagamento teste ${__VU}-${__ITER}`,
    }),
    {
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': uuid(),
      },
    }
  );

  check(transacaoRes, {
    'POST /transacoes -> 200 ou 201': (r) => r.status === 200 || r.status === 201,
    'POST /transacoes -> retorna JSON': (r) => asJson(r) !== null,
  });

  const transacao = asJson(transacaoRes);
  const transacaoId = transacao?.id;

  // 5) GET /transacoes/{id}
  if (transacaoId) {
    const buscarTransacaoRes = http.get(`${BASE}/transacoes/${transacaoId}`);

    check(buscarTransacaoRes, {
      'GET /transacoes/{id} -> 200': (r) => r.status === 200,
      'GET /transacoes/{id} -> retorna JSON': (r) => asJson(r) !== null,
    });
  }

  // 6) GET /usuarios/{id}/transacoes
  const listarTransacoesRes = http.get(`${BASE}/usuarios/${usuarioId}/transacoes`);

  check(listarTransacoesRes, {
    'GET /usuarios/{id}/transacoes -> 200': (r) => r.status === 200,
    'GET /usuarios/{id}/transacoes -> array': (r) => Array.isArray(asJson(r)),
  });

  sleep(1);
}