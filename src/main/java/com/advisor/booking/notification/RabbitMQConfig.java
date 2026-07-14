package com.advisor.booking.notification;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_EXCHANGE = "booking-exchange";
    public static final String BOOKING_CONFIRMED_QUEUE = "booking-confirmed";
    public static final String BOOKING_CONFIRMED_ROUTING_KEY = "booking.confirmed";

    @Bean
    public DirectExchange bookingExchange() {
        return ExchangeBuilder.directExchange(BOOKING_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue bookingConfirmedQueue() {
        return QueueBuilder.durable(BOOKING_CONFIRMED_QUEUE)
            .withArgument("x-dead-letter-exchange", "booking-exchange")
            .withArgument("x-dead-letter-routing-key", "booking.failed")
            .build();
    }

    @Bean
    public Binding bookingConfirmedBinding(Queue bookingConfirmedQueue, DirectExchange bookingExchange) {
        return BindingBuilder
            .bind(bookingConfirmedQueue)
            .to(bookingExchange)
            .with(BOOKING_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Queue bookingFailedQueue() {
        return QueueBuilder.durable("booking-failed").build();
    }

    @Bean
    public Binding bookingFailedBinding(Queue bookingFailedQueue, DirectExchange bookingExchange) {
        return BindingBuilder
            .bind(bookingFailedQueue)
            .to(bookingExchange)
            .with("booking.failed");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
