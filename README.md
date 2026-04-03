# Engenharia de Sistemas Distribuídos

## Documentação

1. [Diagramas C4 (níveis 1 e 2)](/docs/Diagramas%20C4.md)
1. [ADRs](/docs/ADRs.md)
1. [Padrões adotados](/docs/Padroes_adotados.md)
1. [Stack tecnológico](/docs/Stack.md)
1. [Plano de Testes](/PLANO%20DE%20TESTES.pdf)

* Arquivo em doc do Plano de Testes: https://docs.google.com/document/d/1EiM8JaLrdN9V0i3dz0MKFG-QKvbfKT6_Dd8FVpe4jnU/edit?usp=sharing

## POC 3 — Ledger + PIX Sandbox com Idempotência

Implementar um ledger de dupla entrada (double-entry) integrado com a API PIX em sandbox garantindo idempotência e quarentena financeira

## Como executar o projeto

### 1. Subir o SigNoz (Observabilidade)

Antes de iniciar a aplicação, é necessário subir o ambiente de observabilidade.

```bash
cd observabilidade/deploy/docker
docker compose build
docker compose up -d
```

Após subir os containers, acesse:

[http://localhost:3301](http://localhost:3301)

Na primeira execução, será necessário realizar o cadastro inicial no SigNoz.

**Você pode usar informações ficitícias pois é algo que está funcionando localmente.**

Esse passo é obrigatório apenas na primeira vez.

---

### 2. Subir a POC (Ledger + PIX)

Após o SigNoz estar rodando, volte para a raiz do projeto e execute:

```bash
./start-all.sh
```

Esse script irá iniciar todos os serviços da aplicação.

---

### 3. Execuções futuras

Depois da configuração inicial do SigNoz, você pode iniciar a aplicação diretamente com:

```bash
./start-all.sh
```

Não é necessário repetir o setup do SigNoz.

---

### Observação

Certifique-se de que o SigNoz esteja rodando antes de iniciar a POC.

## Grupo Responsável e Responsabilidades:

* Henrique Fabrício de Souza Bandeira (Trade-offs, stack tecnológico, Setup inicial do Docker)
* João Victor da Silva Cirilo (Níveis 1 e 2 de Diagrama C4, Back-End)
* João Victor Oliveira de Lima (Nível 1 de Diagrama C4, Back-End e Banco de Dados)
* Luiz Henrique dos Santos Souza (Níveis 1 e 2 de Diagrama C4, Front-End do Projeto, Signoz e configuração de informações para o OtelCollector; Testes de Carga, Resiliência e Integração)
* Matheus Yago Lima de Freitas (ADRs, padrões arquiteturais e justificativa)

## Testes:

### Carga:

* Rode no terminal (ou se estiver no Windows, rode no Git Bash) o ./run-tests.sh (Não se esqueça de antes dar permissão através de "chmod +x run-tests.sh"
  
    HTTP
    http_req_duration..............: avg=29.08ms min=0s     med=6.98ms max=13.33s p(90)=44.07ms p(95)=85.32ms
      { expected_response:true }...: avg=30.44ms min=1.17ms med=8.52ms max=13.33s p(90)=47.57ms p(95)=92.11ms
    http_req_failed................: 42.11% 15188 out of 36060
    http_reqs......................: 36060  94.712836/s

    EXECUTION
    iteration_duration.............: avg=1.62s   min=1s     med=1.04s  max=31.01s p(90)=2.56s   p(95)=3.07s  
    iterations.....................: 9965   26.173417/s
    vus............................: 2      min=1              max=70
    vus_max........................: 70     min=70             max=70

    NETWORK
    data_received..................: 14 MB  38 kB/s
    data_sent......................: 5.3 MB 14 kB/s




running (6m20.7s), 00/70 VUs, 9965 complete and 0 interrupted iterations
default ✓ [ 100% ] 00/70 VUs  6m20s
time="2026-04-02T23:04:18Z" level=error msg="thresholds on metrics 'http_req_failed' have been crossed"

### Resiliência

* Rode no terminal (ou se estiver no Windows, rode no Git Bash) o ./run-resilience-full.sh (Não se esqueça de antes dar permissão através de "chmod +x run-resilience-full.sh"

█ THRESHOLDS 

    http_req_duration
    ✓ 'p(95)<2000' p(95)=190.75ms

    http_req_failed
    ✓ 'rate<0.35' rate=32.42%


  █ TOTAL RESULTS 

    checks_total.......: 47909  133.063212/s
    checks_succeeded...: 83.27% 39896 out of 47909
    checks_failed......: 16.72% 8013 out of 47909

    ✗ POST /usuarios -> 200 ou 201
      ↳  84% — ✓ 6669 / ✗ 1251
    ✗ POST /transacoes -> 200 ou 201
      ↳  99% — ✓ 6644 / ✗ 25
    ✗ GET /usuarios/{id}/saldo -> 200
      ↳  0% — ✓ 0 / ✗ 6669
    ✗ GET /usuarios/{id}/transacoes -> 200
      ↳  99% — ✓ 6625 / ✗ 44
    ✗ GET /transacoes/{id} -> 200
      ↳  99% — ✓ 6620 / ✗ 24
    ✓ GET ledger balance -> 200/404/5xx aceitável em falha
    ✓ GET ledger statement -> 200/404/5xx aceitável em falha

    HTTP
    http_req_duration..............: avg=59.12ms min=0s     med=19.64ms max=5s     p(90)=113.42ms p(95)=190.75ms
      { expected_response:true }...: avg=62.17ms min=1.13ms med=22.03ms max=4.62s  p(90)=119.02ms p(95)=208.43ms
    http_req_failed................: 32.42% 15534 out of 47909
    http_reqs......................: 47909  133.063212/s

    EXECUTION
    iteration_duration.............: avg=1.46s   min=1s     med=1.16s   max=16.94s p(90)=2s       p(95)=2.9s    
    iterations.....................: 7920   21.997133/s
    vus............................: 2      min=1              max=50
    vus_max........................: 50     min=50             max=50

    NETWORK
    data_received..................: 20 MB  54 kB/s
    data_sent......................: 7.8 MB 22 kB/s

