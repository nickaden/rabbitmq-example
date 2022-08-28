package by.nick.rabbitmq.example.consumer;

import by.nick.rabbitmq.example.broker.ConnectionProvider;
import by.nick.rabbitmq.example.domain.Receipt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static by.nick.rabbitmq.example.configuration.BrokerConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiptConsumer {

    private final ConnectionProvider connectionProvider;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void startPolling() throws IOException {
        Connection connection = connectionProvider.getConnection(this);
        Channel channel1 = connection.createChannel();
        channel1.basicConsume(MAIN_RECEIPT_QUEUE_1, true, this::consumeFromMainQueue, consumerTag -> {});

        Channel channel2 = connection.createChannel();
        channel2.basicConsume(MAIN_RECEIPT_QUEUE_2, true, this::consumeFromMainQueue, consumerTag -> {
        });
    }

    public void consumeFromMainQueue(String tag, Delivery delivery) throws IOException {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        Receipt receipt = objectMapper.readValue(message, Receipt.class);
        log.info("Received message: {}", message);
        try {
            checkForErrorCondition(receipt);
        } catch (NotHandledMessageException exception) {
            processUnhandledMessage(delivery, message);
        }
    }

    private void checkForErrorCondition(Receipt receipt) {
        if (receipt.getGoodId().startsWith("5")) {
            log.info("Not processable receipt with goodId {}. Will retry", receipt.getGoodId());
            throw new NotHandledMessageException(receipt.getGoodId());
        }
    }

    private void processUnhandledMessage(Delivery delivery, String message) throws IOException {
        Integer attempt = Optional.ofNullable(delivery.getProperties())
                .map(AMQP.BasicProperties::getHeaders)
                .map(map -> map.get("x-attempt"))
                .map(LongString.class::cast)
                .map(LongString::toString)
                .map(Integer::valueOf)
                .orElse(1);
        if (attempt <= 3) {
            retryPublish(message, attempt);
        } else {
            recoveryPublish(message);
        }
    }

    private void retryPublish(String message, Integer attempt) throws IOException {
        Connection connection = connectionProvider.getConnection(this);
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties().builder()
                .headers(Map.of("x-attempt", String.valueOf(attempt + 1)))
                .build();
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish(RECEIPT_RETRY_EXCHANGE, RECEIPT_RETRY_ROUTE_KEY, basicProperties,
                    message.getBytes(StandardCharsets.UTF_8));
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void recoveryPublish(String message)
            throws IOException {
        Connection connection = connectionProvider.getConnection(this);
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish(RECEIPT_DLX_EXCHANGE, RECEIPT_DLX_ROUTE_KEY, null,
                    message.getBytes(StandardCharsets.UTF_8));
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
