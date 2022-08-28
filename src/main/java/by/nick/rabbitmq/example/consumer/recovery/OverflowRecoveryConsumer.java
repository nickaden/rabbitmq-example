package by.nick.rabbitmq.example.consumer.recovery;

import by.nick.rabbitmq.example.broker.ConnectionProvider;
import by.nick.rabbitmq.example.domain.Receipt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static by.nick.rabbitmq.example.configuration.BrokerConstants.RECEIPT_OVERFLOW_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverflowRecoveryConsumer {

    private final ConnectionProvider provider;
    private final LogOnlyDatabase database;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void startPolling() throws IOException {
        Connection connection = provider.getConnection(this);
        Channel channel = connection.createChannel();
        channel.basicConsume(RECEIPT_OVERFLOW_QUEUE, true, this::consumeOverflowedMessage, consumerTag -> {});
    }

    @SneakyThrows
    private void consumeOverflowedMessage(String tag, Delivery delivery) {
        String message = new String(delivery.getBody());
        log.info("Got message to recover because queue is overflowed {} because of {}", message,
                delivery.getProperties().getHeaders().get("x-first-death-reason"));
        database.store(objectMapper.readValue(message, Receipt.class));
    }
}
