package com.engss.transationService.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "ledger.exchange";

    // Routing keys que o transaction-service publica
    public static final String RK_SAQUE_INICIADO  = "ledger.saque.iniciado";
    public static final String RK_PIX_CONFIRMADO  = "ledger.pix.confirmado";
    public static final String RK_PIX_FALHOU      = "ledger.pix.falhou";
    public static final String RK_CREDITO_INICIAL = "ledger.credito.inicial";

    // Routing keys que o transaction-service consome (vindas do ledger)
    public static final String RK_LEDGER_DEBITED          = "ledger.debited";
    public static final String RK_LEDGER_DEBIT_CONFIRMED  = "ledger.debit.confirmed";
    public static final String RK_LEDGER_REVERSED         = "ledger.reversed";

    // Filas do transaction-service
    public static final String Q_LEDGER_DEBITED         = "transaction.ledger.debited";
    public static final String Q_LEDGER_DEBIT_CONFIRMED = "transaction.ledger.debit.confirmed";
    public static final String Q_LEDGER_REVERSED        = "transaction.ledger.reversed";

    @Bean
    public TopicExchange ledgerExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue queueLedgerDebited() {
        return QueueBuilder.durable(Q_LEDGER_DEBITED).build();
    }

    @Bean
    public Queue queueLedgerDebitConfirmed() {
        return QueueBuilder.durable(Q_LEDGER_DEBIT_CONFIRMED).build();
    }

    @Bean
    public Queue queueLedgerReversed() {
        return QueueBuilder.durable(Q_LEDGER_REVERSED).build();
    }

    @Bean
    public Binding bindingLedgerDebited() {
        return BindingBuilder.bind(queueLedgerDebited())
                .to(ledgerExchange()).with(RK_LEDGER_DEBITED);
    }

    @Bean
    public Binding bindingLedgerDebitConfirmed() {
        return BindingBuilder.bind(queueLedgerDebitConfirmed())
                .to(ledgerExchange()).with(RK_LEDGER_DEBIT_CONFIRMED);
    }

    @Bean
    public Binding bindingLedgerReversed() {
        return BindingBuilder.bind(queueLedgerReversed())
                .to(ledgerExchange()).with(RK_LEDGER_REVERSED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}