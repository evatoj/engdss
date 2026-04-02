package com.pagamento.biometricadapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração central do RabbitMQ para o Biometric Adapter Service.
 *
 * <h2>Topologia</h2>
 * <pre>
 *  Exchange: biometric.exchange  (direct)
 *  ├── biometric.verification.request   → BiometricRequestConsumer
 *  │       DLQ: biometric.verification.request.dlq
 *  └── biometric.verification.response  → SAGA
 *          DLQ: biometric.verification.response.dlq
 * </pre>
 *
 * <h2>Dead Letter Queue</h2>
 * <p>Mensagens que excedam {@code x-message-ttl} ou atinjam {@code x-max-retries}
 * são roteadas automaticamente para as DLQs, permitindo análise e reprocessamento manual.
 *
 * <p>Propriedades configuráveis em {@code application.yml}:
 * <pre>
 * rabbitmq:
 *   queues:
 *     request:  biometric.verification.request
 *     response: biometric.verification.response
 *   exchange:   biometric.exchange
 *   ttl-ms:     60000
 * </pre>
 */
@Configuration
public class RabbitMQConfig {

    // ------------------------------------------------------------------ //
    //  Nomes de filas / exchange — injetados do application.yml            //
    // ------------------------------------------------------------------ //

    @Value("${rabbitmq.queues.request:biometric.verification.request}")
    private String requestQueue;

    @Value("${rabbitmq.queues.response:biometric.verification.response}")
    private String responseQueue;

    @Value("${rabbitmq.exchange:biometric.exchange}")
    private String exchange;

    @Value("${rabbitmq.ttl-ms:60000}")
    private int messageTtlMs;

    // ── Exchange ─────────────────────────────────────────────────────── //

    @Bean
    public DirectExchange biometricExchange() {
        return ExchangeBuilder.directExchange(exchange)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(exchange + ".dlx")
                .durable(true)
                .build();
    }

    // ── Filas de trabalho ─────────────────────────────────────────────── //

    @Bean
    public Queue requestQueue() {
        return QueueBuilder.durable(requestQueue)
                .withArgument("x-dead-letter-exchange", exchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", requestQueue + ".dlq")
                .withArgument("x-message-ttl", messageTtlMs)
                .build();
    }

    @Bean
    public Queue responseQueue() {
        return QueueBuilder.durable(responseQueue)
                .withArgument("x-dead-letter-exchange", exchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", responseQueue + ".dlq")
                .withArgument("x-message-ttl", messageTtlMs)
                .build();
    }

    // ── Dead Letter Queues ────────────────────────────────────────────── //

    @Bean
    public Queue requestDlq() {
        return QueueBuilder.durable(requestQueue + ".dlq").build();
    }

    @Bean
    public Queue responseDlq() {
        return QueueBuilder.durable(responseQueue + ".dlq").build();
    }

    // ── Bindings ─────────────────────────────────────────────────────── //

    @Bean
    public Binding requestBinding(Queue requestQueue, DirectExchange biometricExchange) {
        return BindingBuilder.bind(requestQueue)
                .to(biometricExchange)
                .with(this.requestQueue);
    }

    @Bean
    public Binding responseBinding(Queue responseQueue, DirectExchange biometricExchange) {
        return BindingBuilder.bind(responseQueue)
                .to(biometricExchange)
                .with(this.responseQueue);
    }

    @Bean
    public Binding requestDlqBinding(Queue requestDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(requestDlq)
                .to(deadLetterExchange)
                .with(requestQueue + ".dlq");
    }

    @Bean
    public Binding responseDlqBinding(Queue responseDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(responseDlq)
                .to(deadLetterExchange)
                .with(responseQueue + ".dlq");
    }

    // ── Serialização JSON ─────────────────────────────────────────────── //

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setExchange(exchange);
        return template;
    }

    /**
     * Container factory com ACK manual e prefetch configurável.
     * Prefetch = 1 garante que cada worker processe uma mensagem por vez,
     * alinhando com a natureza síncrona da chamada à Validra.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Value("${rabbitmq.prefetch:1}") int prefetch) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setPrefetchCount(prefetch);
        return factory;
    }
}
