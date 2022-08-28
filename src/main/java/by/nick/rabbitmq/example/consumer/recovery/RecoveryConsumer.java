package by.nick.rabbitmq.example.consumer.recovery;

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

import static by.nick.rabbitmq.example.configuration.BrokerConstants.RECEIPT_DLX_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecoveryConsumer {

    private final ConnectionProvider connectionProvider;
    private final ObjectMapper objectMapper;
    private final LogOnlyDatabase database;


    @PostConstruct
    public void startPolling() throws IOException {
        Connection connection = connectionProvider.getConnection(this);
        Channel channel = connection.createChannel();
        channel.basicConsume(RECEIPT_DLX_QUEUE, this::storeToDatabase, tag -> {});
    }

    private void storeToDatabase(String tag, Delivery delivery) throws JsonProcessingException {
        Receipt receipt = objectMapper.readValue(new String(delivery.getBody(), StandardCharsets.UTF_8), Receipt.class);
        database.store(receipt);
    }
}
