# ADR-001: Adoção de Event-Driven Architeture

## Status 
Aceito

## Contexto
O protocolo pix do Banco Central não retorna a confirmação de pagamento de forma síncrona. 
Ele retorna 202 Accepted e notifica o resultado via webhook, o que impede o fluxo de request/response tradicional.

Além disso o fluxo de saque envolve múltiplos passos independentes - débito no ledger, envio do PIX, confirmação,
cobrança de fee e liberação de quarentena - o que não podem ser executados em uma única transação síncrona.

## Alternativas Consideradas
### Opção A - Arquitetura Síncrona com Polling
O cliente dispara o saque e fica consultando status periodicamente até obter a confirmação.
- Contras: Em um sistema com múltiplos usuários simultâneos, cada etapa do fluxo gera novas requisições de polling,
  multiplicando o volume de camadas. Isso casusa Overhead crescente, degrada a performance e pode derrubar o sistema sob carga alta. Ainda é síncrono por natureza.
### Opção B - Event-Driven Architeture
A API recebe a requisição, persiste o evento e retorna 202.
Workers independentes processam  cada etapa e se comunicam via eventos de forma assíncrona.

## Consequências
### Positivas
- Respostas rápidas ao cliente - A API retorna 202 imediatamente sem aguardar o processo completo.
- Processamento Concorrente - Workers processam eventos de múltiplos usuários simultâneamente.
- Alinhamento natural com o protocolo PIX - que já é assíncrono por design.
### Negativas
- Maior complexidade operacional - O fluxo não é mais linear, exige observalidade e rastreamento de eventos.
- O cliente precisa ser notificado do resultado por webhook ou polling leve, não pela resposta direta.


# ADR-002: Adoção de Event Sourcing para o Ledger

## Status
Aceito

## Contexto
Em um sistema financeiro precisamos rastrear todas as operações que ocorreram para que o saldo atual de um usuário seja X.
Sem esse histórico, não é possível auditar entradas e saídas, nem identificar em qual etapa um fluxo multi-passo falhou.

Por exemplo, se o PIX foi confirmado mas a fee não foi cobrada, sem o histórico de eventos não há como saber onde o fluxo parou.

## Alternativas Consideradas

### Opção A — State-based
Tabela com saldo mutável, sobrescrito a cada transação.

- Contras: armazena apenas o resultado final. Não há
  histórico de operações, impossibilitando auditoria
  e rastreabilidade.

### Opção B — Append-only Ledger
Registra os lançamentos financeiros mas sem o conceito
de evento com estado e tipo definidos.

- Contras: é possível ver os lançamentos, mas sem o
  evento associado não conseguimos identificar em qual
  etapa do fluxo ocorreu uma falha.

### Opção C — Event Sourcing
Cada mudança de estado gera um evento imutável. O saldo
é derivado da sequência de eventos. Nenhum evento é
editado, apenas estornado.

## Decisão
Adotaremos Event Sourcing como modelo de persistência
do ledger. Cada etapa do fluxo de saque gerará um evento
imutável, permitindo reconstruir o estado completo em
qualquer ponto no tempo.

## Consequências
### Positivas
- Rastreabilidade completa — sabemos exatamente quais operações levaram ao saldo atual
- Identificação precisa de falhas — é possível ver em qual etapa do fluxo o processamento parou
- Auditoria natural — o histórico é imutável por design
### Negativas
- Query de saldo mais custosa — requer agregação dos eventos, mitigado com views materializadas via CQRS
- Maior volume de dados — cada transição de estado gera um novo registro