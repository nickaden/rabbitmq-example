package by.nick.rabbitmq.example.consumer;

import by.nick.rabbitmq.example.broker.ConnectionProvider;
import by.nick.rabbitmq.example.domain.Receipt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static by.nick.rabbitmq.example.configuration.BrokerConstants.RECEIPT_DLX_EXCHANGE;
import static by.nick.rabbitmq.example.configuration.BrokerConstants.RECEIPT_DLX_ROUTE_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiptHandler {

    private final ObjectMapper objectMapper;
    private final ConnectionProvider connectionProvider;

    @Retryable(value = NotHandledMessageException.class)
    public void handle(Receipt receipt) throws JsonProcessingException {
        String message = objectMapper.writeValueAsString(receipt);
        log.info("Received message: {}", message);
        checkForErrorCondition(receipt);
    }

    private void checkForErrorCondition(Receipt receipt) {
        if (receipt.getGoodId().startsWith("5")) {
            log.info("Not processable receipt with goodId {}. Will retry", receipt.getGoodId());
            throw new NotHandledMessageException(receipt.getGoodId());
        }
    }

    @Recover
    public void recoveryPublish(NotHandledMessageException exception, Receipt receipt)
            throws IOException, TimeoutException {
        Connection connection = connectionProvider.getConnection(this);
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish(RECEIPT_DLX_EXCHANGE, RECEIPT_DLX_ROUTE_KEY, null,
                    objectMapper.writeValueAsBytes(receipt));
        }
    }
}
