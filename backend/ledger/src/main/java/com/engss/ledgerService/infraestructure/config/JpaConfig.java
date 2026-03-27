package com.ledgerService.ledger.infraestructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.engss.ledgerService.domain.repository",
        repositoryImplementationPostfix = "Impl")
public class JpaConfig {
    // Spring Boot autoconfigura DataSource e EntityManagerFactory via application.yml
    // Esta classe existe para tornar explícito o pacote dos repositories
    // e habilitar o gerenciamento de transações
}