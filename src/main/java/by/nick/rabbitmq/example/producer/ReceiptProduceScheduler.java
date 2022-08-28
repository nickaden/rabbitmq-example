package by.nick.rabbitmq.example.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiptProduceScheduler {

    private final JsonReceiptSource receiptSource;
    private final ReceiptProducer producer;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void scheduledSend() {
        receiptSource.getReceipt().ifPresentOrElse(receipt -> {
            try {
                producer.send(receipt);
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }, this::logEmptySource);
    }

    private void logEmptySource() {
        log.warn("Receipt source is empty");
    }
}
