package by.nick.rabbitmq.example.broker;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
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
            channel.exchangeDeclare(RECEIPT_EXCHANGE, BuiltinExchangeType.DIRECT);
            channel.queueDeclare(MAIN_RECEIPT_QUEUE_1, false, false, false, null);
            channel.queueDeclare(MAIN_RECEIPT_QUEUE_2, false, false, false, null);
            channel.queueBind(MAIN_RECEIPT_QUEUE_1, RECEIPT_EXCHANGE, MAIN_RECEIPT_ROUTE_KEY_1);
            channel.queueBind(MAIN_RECEIPT_QUEUE_2, RECEIPT_EXCHANGE, MAIN_RECEIPT_ROUTE_KEY_2);
            //Recovery flow
            channel.exchangeDeclare(RECEIPT_DLX_EXCHANGE, BuiltinExchangeType.DIRECT);
            channel.queueDeclare(RECEIPT_DLX_QUEUE, false, false, false, null);
            channel.queueBind(RECEIPT_DLX_QUEUE, RECEIPT_DLX_EXCHANGE, RECEIPT_DLX_ROUTE_KEY);

        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

    }
}
