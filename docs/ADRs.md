# ADR-001: Arquitetura orientada a eventos com SAGA por coreografia

## Status
Aceito

## Contexto
O fluxo de saque da POC não termina em uma única chamada HTTP. Depois da criação da transação, ainda existem etapas independentes: bloqueio de saldo no ledger, envio ao provedor PIX, confirmação ou falha do PIX e atualização final do status da transação.

Além disso, o enunciado da POC 3 exige integração com PIX sandbox, idempotência e confiabilidade em um fluxo financeiro assíncrono, recomendando explicitamente padrões como SAGA, EDA e Retry/DLQ. 

## Decisão
Adotamos uma arquitetura orientada a eventos com SAGA por coreografia entre `transaction-service` e `ledger-service`, usando RabbitMQ como barramento de mensagens.

O fluxo principal ficou assim:

```text
POST /transacoes
  → transaction-service persiste transação PENDENTE
  → grava evento na outbox: SaqueIniciado
  → worker publica em RabbitMQ
  → ledger-service consome e tenta aplicar DEBIT_PENDING
     → se saldo suficiente: publica LedgerDebited
     → se saldo insuficiente: publica LedgerDebitDenied
  → transaction-service consome LedgerDebited e chama PixAdapter
     → se PIX confirmar: grava PixConfirmado na outbox
     → se PIX falhar: grava PixFalhou na outbox
  → ledger-service consome PixConfirmado ou PixFalhou
     → confirma o débito ou faz estorno
  → transaction-service consome evento final e atualiza a transação
```

## Alternativas consideradas

### Opção A - Fluxo totalmente síncrono entre serviços
O `transaction-service` chamaria o `ledger-service` e o provedor PIX dentro da mesma requisição.

- **Prós:** fluxo mais simples de entender.
- **Contras:** maior acoplamento temporal, maior latência para o cliente e maior risco de falhas parciais deixando a operação em estado inconsistente.

### Opção B - SAGA com orquestrador central
Um serviço dedicado controlaria todos os passos do saque.

- **Prós:** fluxo explícito em um ponto central.
- **Contras:** mais código de coordenação para uma POC pequena e criação de um ponto central extra de falha e manutenção.

### Opção C - SAGA por coreografia com eventos
Cada serviço reage aos eventos relevantes e executa apenas sua parte.

- **Prós:** melhor desacoplamento entre serviços e alinhamento natural com a mensageria já usada.
- **Contras:** fluxo distribuído fica mais difícil de rastrear sem observabilidade e correlation id.

## Consequências
### Positivas
- Desacoplamento temporal entre API, ledger e provedor PIX.
- Permite compensação explícita quando o PIX falha.
- Mantém o sistema alinhado a um cenário real de integrações assíncronas.
- Facilita evolução do fluxo sem transformar uma requisição HTTP em processo longo.

### Negativas
- O fluxo deixa de ser linear e fica mais difícil de depurar.
- Exige correlation id, observabilidade e tratamento cuidadoso de mensagens repetidas.
- O usuário não recebe o resultado final do saque imediatamente na mesma resposta HTTP.

---

# ADR-002: Ledger append-only com eventos imutáveis e projeção de saldo

## Status
Aceito

## Contexto
A POC precisa manter rastreabilidade das operações financeiras, suportar quarentena (`pending_balance`) e permitir auditoria do que aconteceu em cada etapa do saque. Apenas manter um campo `saldo` mutável não seria suficiente para explicar por que um valor foi bloqueado, confirmado, negado ou estornado.

No código atual, o ledger foi implementado com:
- tabela de eventos `ledger_events` como trilha imutável do que aconteceu;
- projeção `balance_view` com `available_balance` e `pending_balance`;
- tipos de evento como `CREDIT`, `DEBIT_PENDING`, `DEBIT_CONFIRMED`, `REVERSAL` e `DEBIT_DENIED`.

## Decisão
Adotamos um ledger append-only baseado em eventos imutáveis, com uma projeção de leitura separada para saldo.

Na prática:
- a escrita registra eventos no histórico do ledger;
- a leitura do saldo usa `balance_view`, atualizada a cada evento aplicado;
- o bloqueio de saldo para quarentena é representado por `DEBIT_PENDING`;
- a confirmação definitiva do saque usa `DEBIT_CONFIRMED`;
- falhas são compensadas com `REVERSAL`.

## Alternativas consideradas

### Opção A - Saldo apenas mutável por conta
Armazenar apenas o saldo final em uma tabela simples.

- **Prós:** implementação mais simples.
- **Contras:** pouca auditabilidade e baixa capacidade de explicar o histórico do fluxo.

### Opção B - Ledger append-only com projeção de saldo
Registrar cada transição relevante e manter uma visão materializada para leitura.

- **Prós:** rastreabilidade, auditoria e suporte natural à quarentena.
- **Contras:** maior complexidade de modelagem.

## Consequências
### Positivas
- Permite reconstruir o histórico financeiro da conta.
- Separa claramente saldo disponível e saldo em quarentena.
- Torna estornos e negativas explícitos no histórico.
- Melhora auditoria e explicabilidade do fluxo.

### Negativas
- Introduz sincronização entre escrita de eventos e projeção de leitura.
- Requer regras cuidadosas de idempotência na aplicação dos eventos.
- Esta POC modela o ledger por conta e por evento, não um razão contábil completo multi-conta; isso simplifica a entrega, mas reduz aderência a um double-entry financeiro completo.

---

# ADR-003: Transactional Outbox como mecanismo obrigatório de publicação

## Status
Aceito

## Contexto
Sem outbox, haveria risco de *dual write*: salvar estado no banco e falhar ao publicar no RabbitMQ, ou publicar a mensagem e falhar antes de persistir o estado. Esse problema é especialmente crítico no fluxo de saque, porque banco e mensageria precisam permanecer coerentes.

No estado atual do projeto, tanto `transaction-service` quanto `ledger-service` gravam eventos em tabelas de outbox e possuem workers agendados que publicam as mensagens pendentes.

## Decisão
Adotamos o padrão Transactional Outbox nos dois serviços.

A regra é:
1. a operação de negócio persiste seu estado e o registro de outbox na mesma transação local;
2. um worker assíncrono busca eventos pendentes;
3. o worker publica no RabbitMQ e marca o evento como publicado;
4. falhas ficam registradas para retry posterior.

## Alternativas consideradas

### Opção A - Publicação direta no RabbitMQ dentro do serviço

- **Prós:** menos código.
- **Contras:** mantém o problema de dual write.

### Opção B - Transactional Outbox

- **Prós:** aumenta a consistência entre banco e mensageria sem depender de transação distribuída.
- **Contras:** adiciona tabela, worker e monitoramento operacional.

## Consequências
### Positivas
- Reduz fortemente o risco de inconsistência entre persistência e publicação.
- Mantém o fluxo compatível com SAGA sem exigir 2PC.
- Permite retry controlado sobre eventos que falharam ao publicar.

### Negativas
- Introduz latência pequena entre persistência e publicação efetiva.
- Requer limpeza, monitoramento e métricas da outbox.
- Gera mais componentes para explicar no videocast e na documentação.

---

# ADR-004: Idempotência formal com chave do cliente, hash do payload e estado de processamento

## Status
Aceito

## Contexto
O escopo da POC exige que retry de saque não duplique crédito nem gere saques repetidos. Em integrações distribuídas, timeout do cliente, reenvio manual ou retry automático são cenários esperados.

A solução inicial baseada apenas em uma coluna `chave_idempotencia` na transação era simples, mas insuficiente para tratar concorrência e reuso indevido da mesma chave com payload diferente.

## Decisão
Adotamos uma camada formal de idempotência na tabela `idempotencia`, com:
- chave enviada pelo cliente no header `Idempotency-Key`;
- hash SHA-256 do payload canônico;
- estados `PROCESSANDO` e `CONCLUIDA`;
- vínculo da chave com a `transacao_pix` gerada.

Regras aplicadas:
- mesma chave + mesmo payload + transação concluída → retorna resultado anterior;
- mesma chave + payload diferente → erro;
- mesma chave ainda em processamento → erro de concorrência/processamento.

## Alternativas consideradas

### Opção A - Idempotência apenas por coluna única na transação

- **Prós:** simples de implementar.
- **Contras:** não modela bem requisição em processamento nem protege contra mesmo identificador com payload diferente.

### Opção B - Tabela dedicada de idempotência

- **Prós:** tratamento mais explícito e seguro para concorrência, replays e deduplicação.
- **Contras:** aumenta modelagem e número de passos do fluxo.

## Consequências
### Positivas
- Retry seguro para criação de transações.
- Melhor tratamento de concorrência entre requisições repetidas.
- Impede reuso indevido da mesma chave para outra operação.
- Fica fácil demonstrar o requisito de idempotência na apresentação.

### Negativas
- Mais estado para manter e testar.
- Requer definição clara do payload canônico para evitar falsos conflitos.
- A idempotência está forte na entrada HTTP, mas consumidores de mensageria ainda dependem das proteções por `correlationId` e `idempotencyKey` em cada serviço.

---

# ADR-005: Abstração do provedor PIX por adapter, com mock padrão e Asaas sandbox opcional

## Status
Aceito

## Contexto
O trabalho exige integração com PIX em sandbox, mas o projeto também precisa ser fácil de executar localmente para demonstração, testes e videocast. Depender sempre de um provedor externo tornaria a POC mais frágil e mais difícil de reproduzir.

No código atual existe a interface `PixAdapter`, com duas implementações:
- `MockPixAdapter`, habilitada por padrão;
- `AsaasPixAdapter`, habilitada quando `pix.provider=asaas`.

## Decisão
Adotamos o padrão Adapter para isolar a integração PIX do restante do fluxo de negócio.

O `transaction-service` conversa apenas com a interface `PixAdapter`. A escolha do provedor fica em configuração.

## Alternativas consideradas

### Opção A - Chamar diretamente a API do provedor dentro do serviço de negócio

- **Prós:** menos abstrações.
- **Contras:** forte acoplamento ao PSP e maior dificuldade para testes locais.

### Opção B - Abstração via adapter

- **Prós:** troca simples entre mock e sandbox real, melhor testabilidade e menor acoplamento.
- **Contras:** mais classes e necessidade de manter contrato comum entre implementações.

## Consequências
### Positivas
- Permite demo local estável com `mock`.
- Mantém caminho de integração com Asaas sandbox quando necessário.
- Reduz impacto de mudança futura de PSP.
- Facilita testes de sucesso, falha e atraso controlado.

### Negativas
- O mock não substitui todos os comportamentos reais de um PSP.
- Existe risco de divergência entre o fluxo mockado e o comportamento do provedor real.
- Webhooks e estados reais do PSP ainda precisam ser validados separadamente quando o adapter real estiver ativo.
