import http from 'k6/http';
import { check, sleep } from 'k6';

const TRANSACTION_BASE = 'http://transaction-service:8080';
const LEDGER_BASE = 'http://ledger-service:8082';

export const options = {
  stages: [
    { duration: '20s', target: 10 },
    { duration: '40s', target: 20 },
    { duration: '1m', target: 35 },
    { duration: '1m', target: 35 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 50 },
    { duration: '40s', target: 20 },
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.35'],
  },
};

function asJson(res) {
  try {
    return res.json();
  } catch (e) {
    return null;
  }
}

function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

export default function () {
  const nome = `ResUser-${__VU}-${__ITER}-${Date.now()}`;

  // 1) cria usuário
  const criarUsuarioRes = http.post(
    `${TRANSACTION_BASE}/usuarios`,
    JSON.stringify({
      nome,
      saldoInicial: 1000.0,
    }),
    {
      headers: { 'Content-Type': 'application/json' },
      timeout: '5s',
    }
  );

  check(criarUsuarioRes, {
    'POST /usuarios -> 200 ou 201': (r) => r.status === 200 || r.status === 201,
  });

  const usuario = asJson(criarUsuarioRes);
  const usuarioId = usuario?.id;

  if (!usuarioId) {
    sleep(1);
    return;
  }

  // 2) cria transação
  const transacaoRes = http.post(
    `${TRANSACTION_BASE}/transacoes`,
    JSON.stringify({
      usuarioId: usuarioId,
      chavePixDestino: `destino-${__VU}-${__ITER}@teste.com`,
      valor: 15.0,
      descricao: `Teste resiliência ${__VU}-${__ITER}`,
    }),
    {
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': uuid(),
      },
      timeout: '5s',
    }
  );

  check(transacaoRes, {
    'POST /transacoes -> 200 ou 201': (r) => r.status === 200 || r.status === 201,
  });

  const transacao = asJson(transacaoRes);
  const transacaoId = transacao?.id;

  // 3) consulta saldo do usuário
  const saldoRes = http.get(`${TRANSACTION_BASE}/usuarios/${usuarioId}/saldo`, {
    timeout: '5s',
  });

  check(saldoRes, {
    'GET /usuarios/{id}/saldo -> 200': (r) => r.status === 200,
  });

  // 4) consulta transações do usuário
  const listarTransacoesRes = http.get(
    `${TRANSACTION_BASE}/usuarios/${usuarioId}/transacoes`,
    { timeout: '5s' }
  );

  check(listarTransacoesRes, {
    'GET /usuarios/{id}/transacoes -> 200': (r) => r.status === 200,
  });

  // 5) consulta transação por id
  if (transacaoId) {
    const buscarTransacaoRes = http.get(
      `${TRANSACTION_BASE}/transacoes/${transacaoId}`,
      { timeout: '5s' }
    );

    check(buscarTransacaoRes, {
      'GET /transacoes/{id} -> 200': (r) => r.status === 200,
    });
  }

  // 6) consulta ledger balance
  const balanceRes = http.get(
    `${LEDGER_BASE}/ledger/queries/balance/${usuarioId}`,
    { timeout: '3s' }
  );

  check(balanceRes, {
    'GET ledger balance -> 200/404/5xx aceitável em falha': (r) =>
      r.status === 200 || r.status === 404 || r.status >= 500 || r.error_code !== 0,
  });

  // 7) consulta ledger statement
  const statementRes = http.get(
    `${LEDGER_BASE}/ledger/queries/statement/${usuarioId}`,
    { timeout: '3s' }
  );

  check(statementRes, {
    'GET ledger statement -> 200/404/5xx aceitável em falha': (r) =>
      r.status === 200 || r.status === 404 || r.status >= 500 || r.error_code !== 0,
  });

  sleep(1);
}