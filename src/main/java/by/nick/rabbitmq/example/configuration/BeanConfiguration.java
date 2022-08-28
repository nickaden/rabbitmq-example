package by.nick.rabbitmq.example.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public ConnectionFactory connectionFactory(@Value("${amqp.broker.host}") String brokerHost) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(brokerHost);
        return connectionFactory;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
