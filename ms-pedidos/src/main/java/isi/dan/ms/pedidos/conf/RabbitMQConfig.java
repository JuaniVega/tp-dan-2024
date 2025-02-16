package isi.dan.ms.pedidos.conf;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String STOCK_UPDATE_QUEUE = "stock-update-queue";
    public static final String STOCK_CHECK_QUEUE = "stock-check-queue";

    @Bean
    public Queue stockUpdateQueue() {
        return new Queue(STOCK_UPDATE_QUEUE, true);
    }

    @Bean
    public Queue stockCheckQueue() {
        return new Queue(STOCK_CHECK_QUEUE, true);
    }
}

