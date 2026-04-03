# Engenharia de Sistemas Distribuídos

## Documentação

1. [Diagramas C4 (níveis 1 e 2)](/docs/Diagramas%20C4.md)
1. [ADRs](/docs/ADRs.md)
1. [Padrões adotados](/docs/Padroes_adotados.md)
1. [Stack tecnológico](/docs/Stack.md)
1. [Plano de Testes](/PLANO%20DE%20TESTES.pdf)
1. [Video em Drive](https://drive.google.com/drive/folders/1PvVSw3cMuZPtojuWsWrEQAXPS1PFURLm?usp=sharing)

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

### Resiliência

* Rode no terminal (ou se estiver no Windows, rode no Git Bash) o ./run-resilience-full.sh (Não se esqueça de antes dar permissão através de "chmod +x run-resilience-full.sh"


