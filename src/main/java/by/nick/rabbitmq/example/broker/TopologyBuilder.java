package by.nick.rabbitmq.example.broker;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static by.nick.rabbitmq.example.configuration.BrokerConstants.*;

@Component
@RequiredArgsConstructor
public class TopologyBuilder {

    private final ConnectionFactory connectionFactory;

    @PostConstruct
    public void setUpTopology() {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            //Main flow
            Map<String, Object> mainQueueArgs = Map.of(
                    "x-message-ttl", 5000,
                    "x-max-length", 5,
                    "x-dead-letter-exchange", RECEIPT_OVERFLOW_EXCHANGE,
                    "x-dead-letter-routing-key", RECEIPT_OVERFLOW_ROUTE_KEY,
                    "x-overflow", "reject-publish-dlx"
            );
            channel.exchangeDeclare(RECEIPT_EXCHANGE, BuiltinExchangeType.DIRECT);
            channel.queueDeclare(MAIN_RECEIPT_QUEUE_1, false, false, false, mainQueueArgs);
            channel.queueDeclare(MAIN_RECEIPT_QUEUE_2, false, false, false, mainQueueArgs);
            channel.queueBind(MAIN_RECEIPT_QUEUE_1, RECEIPT_EXCHANGE, MAIN_RECEIPT_ROUTE_KEY_1);
            channel.queueBind(MAIN_RECEIPT_QUEUE_2, RECEIPT_EXCHANGE, MAIN_RECEIPT_ROUTE_KEY_2);
            //Recovery flow
            channel.exchangeDeclare(RECEIPT_DLX_EXCHANGE, BuiltinExchangeType.DIRECT);
            channel.queueDeclare(RECEIPT_DLX_QUEUE, false, false, false, null);
            channel.queueBind(RECEIPT_DLX_QUEUE, RECEIPT_DLX_EXCHANGE, RECEIPT_DLX_ROUTE_KEY);

            //Retry flow
            channel.exchangeDeclare(RECEIPT_RETRY_EXCHANGE, BuiltinExchangeType.DIRECT);
            Map<String, Object> retryQueueArgs = Map.of(
                    "x-message-ttl", 5000,
                    "x-dead-letter-exchange", RECEIPT_EXCHANGE,
                    "x-dead-letter-routing-key", MAIN_RECEIPT_ROUTE_KEY_1
            );
            channel.queueDeclare(RECEIPT_RETRY_QUEUE, false, false, false, retryQueueArgs);
            channel.queueBind(RECEIPT_RETRY_QUEUE, RECEIPT_RETRY_EXCHANGE, RECEIPT_RETRY_ROUTE_KEY);

            //Queue overflow recovery
            channel.exchangeDeclare(RECEIPT_OVERFLOW_EXCHANGE, BuiltinExchangeType.DIRECT);
            channel.queueDeclare(RECEIPT_OVERFLOW_QUEUE, false, false, false, null);
            channel.queueBind(RECEIPT_OVERFLOW_QUEUE, RECEIPT_OVERFLOW_EXCHANGE, RECEIPT_OVERFLOW_ROUTE_KEY);

        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

    }
}
