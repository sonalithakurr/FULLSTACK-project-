package com.cineverse.review.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class RabbitMQConfig {

    public static final String REVIEW_EXCHANGE = "cineverse.reviews";
    public static final String RATING_UPDATED_QUEUE = "review.rating.updated.queue";
    public static final String RATING_UPDATED_KEY = "review.rating.updated";

    @Bean
    public TopicExchange reviewExchange() {
        return ExchangeBuilder.topicExchange(REVIEW_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue ratingUpdatedQueue() {
        return QueueBuilder.durable(RATING_UPDATED_QUEUE)
            .withArgument("x-dead-letter-exchange", "cineverse.dlx")
            .build();
    }

    @Bean
    public Binding ratingUpdatedBinding(Queue ratingUpdatedQueue, TopicExchange reviewExchange) {
        return BindingBuilder.bind(ratingUpdatedQueue).to(reviewExchange).with(RATING_UPDATED_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory,
                                          Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(converter);
        return template;
    }
}
