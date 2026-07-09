package com.glotrush.config;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.glotrush.constants.SecurityConstants;

@Configuration
public class RabbitMQConfig {

    public static final String SSE_EXCHANGE = "sse.notifications";

    @Bean
    public FanoutExchange sseExchange() {
        return new FanoutExchange(SSE_EXCHANGE, false, false);
    }

    @Bean
    public Queue sseQueue() {
        AnonymousQueue queue = new AnonymousQueue();
        queue.getArguments().remove(SecurityConstants.X_QUEUE_KEY_TYPE);
        return queue;
    }

    @Bean
    public Binding sseBinding(Queue sseQueue, FanoutExchange sseExchange) {
        return BindingBuilder.bind(sseQueue).to(sseExchange);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}