package by.nick.rabbitmq.example.consumer;

import by.nick.rabbitmq.example.broker.ConnectionProvider;
import by.nick.rabbitmq.example.domain.Receipt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiptHandler {

    private final ObjectMapper objectMapper;
    private final ConnectionProvider connectionProvider;

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


}
