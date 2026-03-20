# Stack: Java + Spring Boot

A escolha de Java + Spring Boot foi feita porque a POC exige um nível alto de confiabilidade, consistência e controle transacional, típico de sistemas financeiros. Como o projeto envolve um ledger de dupla entrada, idempotência e integração assíncrona com o PIX, é fundamental garantir que operações não sejam duplicadas, perdidas ou deixem o sistema em estado inconsistente.

O Java oferece tipagem forte e melhor controle de concorrência, o que ajuda a evitar erros em regras críticas de negócio. Já o Spring Boot facilita a implementação de transações ACID, essenciais para garantir que débitos e créditos ocorram de forma atômica e segura.

Além disso, a stack tem forte suporte para os padrões adotados no projeto, como Event Sourcing, SAGA, EDA e CQRS, permitindo estruturar fluxos assíncronos com rastreabilidade e compensações bem definidas. Também integra bem com mensageria e padrões como Transactional Outbox, garantindo consistência entre banco e eventos.

Em resumo, Java + Spring Boot foi escolhido por oferecer uma base mais robusta e confiável para implementar um sistema que simula operações financeiras reais, onde erros de consistência não são aceitáveis.