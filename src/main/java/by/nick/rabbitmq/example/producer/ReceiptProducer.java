package by.nick.rabbitmq.example.producer;

import by.nick.rabbitmq.example.broker.ConnectionProvider;
import by.nick.rabbitmq.example.domain.Receipt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static by.nick.rabbitmq.example.configuration.BrokerConstants.MAIN_RECEIPT_ROUTE_KEY;
import static by.nick.rabbitmq.example.configuration.BrokerConstants.RECEIPT_EXCHANGE;

@Component
@RequiredArgsConstructor
public class ReceiptProducer {

    private final ConnectionProvider connectionProvider;
    private final JsonReceiptSource receiptSource;
    private final ObjectMapper objectMapper;

    public void send(Receipt receipt) throws IOException, TimeoutException {
        Connection connection = connectionProvider.getConnection(this);
        Channel channel = connection.createChannel();
        marshallAndPublish(receipt, channel);
        channel.close();
    }

    @SneakyThrows
    private void marshallAndPublish(Receipt receipt, Channel channel) {
        byte[] messageByes = objectMapper.writeValueAsBytes(receipt);
        channel.basicPublish(RECEIPT_EXCHANGE, MAIN_RECEIPT_ROUTE_KEY, null, messageByes);
    }
}
