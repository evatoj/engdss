package com.engss.transaction.infraestructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbit.exchange}")
    private String exchangeName;

    @Value("${app.rabbit.queue.ledger-debited}")
    private String queueLedgerDebited;

    @Value("${app.rabbit.queue.ledger-debit-confirmed}")
    private String queueLedgerDebitConfirmed;

    @Value("${app.rabbit.queue.ledger-reversed}")
    private String queueLedgerReversed;

    @Value("${app.rabbit.queue.ledger-debit-denied}")
    private String queueLedgerDebitDenied;

    @Value("${app.rabbit.routing-key.ledger-debited}")
    private String routingKeyLedgerDebited;

    @Value("${app.rabbit.routing-key.ledger-debit-confirmed}")
    private String routingKeyLedgerDebitConfirmed;

    @Value("${app.rabbit.routing-key.ledger-reversed}")
    private String routingKeyLedgerReversed;

    @Value("${app.rabbit.routing-key.ledger-debit-denied}")
    private String routingKeyLedgerDebitDenied;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public TopicExchange ledgerExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue ledgerDebitedQueue() {
        return QueueBuilder.durable(queueLedgerDebited).build();
    }

    @Bean
    public Queue ledgerDebitConfirmedQueue() {
        return QueueBuilder.durable(queueLedgerDebitConfirmed).build();
    }

    @Bean
    public Queue ledgerReversedQueue() {
        return QueueBuilder.durable(queueLedgerReversed).build();
    }

    @Bean
    public Queue ledgerDebitDeniedQueue() {
        return QueueBuilder.durable(queueLedgerDebitDenied).build();
    }

    @Bean
    public Binding ledgerDebitedBinding(Queue ledgerDebitedQueue, TopicExchange ledgerExchange) {
        return BindingBuilder
                .bind(ledgerDebitedQueue)
                .to(ledgerExchange)
                .with(routingKeyLedgerDebited);
    }

    @Bean
    public Binding ledgerDebitConfirmedBinding(Queue ledgerDebitConfirmedQueue, TopicExchange ledgerExchange) {
        return BindingBuilder
                .bind(ledgerDebitConfirmedQueue)
                .to(ledgerExchange)
                .with(routingKeyLedgerDebitConfirmed);
    }

    @Bean
    public Binding ledgerReversedBinding(Queue ledgerReversedQueue, TopicExchange ledgerExchange) {
        return BindingBuilder
                .bind(ledgerReversedQueue)
                .to(ledgerExchange)
                .with(routingKeyLedgerReversed);
    }

    @Bean
    public Binding ledgerDebitDeniedBinding(Queue ledgerDebitDeniedQueue, TopicExchange ledgerExchange) {
        return BindingBuilder
                .bind(ledgerDebitDeniedQueue)
                .to(ledgerExchange)
                .with(routingKeyLedgerDebitDenied);
    }
}