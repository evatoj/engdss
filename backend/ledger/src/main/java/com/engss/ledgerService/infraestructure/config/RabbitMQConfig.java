package com.engss.ledgerService.infraestructure.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "ledger.exchange";

    public static final String RK_LEDGER_DEBITED          = "ledger.debited";
    public static final String RK_LEDGER_DEBIT_CONFIRMED  = "ledger.debit.confirmed";
    public static final String RK_LEDGER_REVERSED         = "ledger.reversed";

    // Filas que o ledger consome
    public static final String Q_SAQUE_INICIADO   = "ledger.saque.iniciado";
    public static final String Q_PIX_CONFIRMADO   = "ledger.pix.confirmado";
    public static final String Q_PIX_FALHOU       = "ledger.pix.falhou";
    public static final String Q_CREDITO_INICIAL  = "ledger.credito.inicial";

    @Bean
    public TopicExchange ledgerExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue queueSaqueIniciado() {
        return QueueBuilder.durable(Q_SAQUE_INICIADO).build();
    }

    @Bean
    public Queue queuePixConfirmado() {
        return QueueBuilder.durable(Q_PIX_CONFIRMADO).build();
    }

    @Bean
    public Queue queuePixFalhou() {
        return QueueBuilder.durable(Q_PIX_FALHOU).build();
    }

    @Bean
    public Queue queueCreditoInicial() {
        return QueueBuilder.durable(Q_CREDITO_INICIAL).build();
    }

    @Bean
    public Binding bindingSaqueIniciado() {
        return BindingBuilder.bind(queueSaqueIniciado())
                .to(ledgerExchange()).with(Q_SAQUE_INICIADO);
    }

    @Bean
    public Binding bindingPixConfirmado() {
        return BindingBuilder.bind(queuePixConfirmado())
                .to(ledgerExchange()).with(Q_PIX_CONFIRMADO);
    }

    @Bean
    public Binding bindingPixFalhou() {
        return BindingBuilder.bind(queuePixFalhou())
                .to(ledgerExchange()).with(Q_PIX_FALHOU);
    }

    @Bean
    public Binding bindingCreditoInicial() {
        return BindingBuilder.bind(queueCreditoInicial())
                .to(ledgerExchange()).with(Q_CREDITO_INICIAL);
    }
}