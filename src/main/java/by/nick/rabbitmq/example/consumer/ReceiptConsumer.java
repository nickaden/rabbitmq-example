package by.nick.rabbitmq.example.consumer;

import by.nick.rabbitmq.example.broker.ConnectionProvider;
import by.nick.rabbitmq.example.domain.Receipt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static by.nick.rabbitmq.example.configuration.BrokerConstants.MAIN_RECEIPT_QUEUE_1;
import static by.nick.rabbitmq.example.configuration.BrokerConstants.MAIN_RECEIPT_QUEUE_2;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiptConsumer {

    private final ConnectionProvider connectionProvider;
    private final ObjectMapper objectMapper;
    private final ReceiptHandler receiptHandler;

    @PostConstruct
    public void startPolling() throws IOException {
        Connection connection = connectionProvider.getConnection(this);
        Channel channel1 = connection.createChannel();
        channel1.basicConsume(MAIN_RECEIPT_QUEUE_1, true, this::consumeFromMainQueue, consumerTag -> {});

        Channel channel2 = connection.createChannel();
        channel2.basicConsume(MAIN_RECEIPT_QUEUE_2, true, this::consumeFromMainQueue, consumerTag -> {});
    }



    public void consumeFromMainQueue(String tag, Delivery delivery) throws JsonProcessingException {
        Receipt receipt = objectMapper.readValue(new String(delivery.getBody(), StandardCharsets.UTF_8), Receipt.class);
        receiptHandler.handle(receipt);
    }
}
