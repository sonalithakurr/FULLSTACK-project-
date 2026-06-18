package com.cineverse.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for the Auth Service.
 *
 * Exchange strategy:
 *  - Topic exchange "cineverse.users" for user lifecycle events
 *  - Routing keys follow pattern: user.<action>  (e.g., user.registered, user.deleted)
 *  - Other services bind their queues with matching routing key patterns
 */
@Configuration
public class RabbitMQConfig {

    public static final String USER_EXCHANGE = "cineverse.users";
    public static final String USER_REGISTERED_QUEUE = "user.registered.queue";
    public static final String USER_REGISTERED_KEY = "user.registered";

    @Bean
    public TopicExchange userExchange() {
        return ExchangeBuilder.topicExchange(USER_EXCHANGE)
            .durable(true)
            .build();
    }

    @Bean
    public Queue userRegisteredQueue() {
        // Durable queue survives broker restarts
        return QueueBuilder.durable(USER_REGISTERED_QUEUE)
            .withArgument("x-dead-letter-exchange", "cineverse.dlx") // DLX for failed messages
            .build();
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue)
            .to(userExchange)
            .with(USER_REGISTERED_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                          Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
