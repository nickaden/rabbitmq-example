package by.nick.rabbitmq.example.producer;

import by.nick.rabbitmq.example.domain.Receipt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JsonReceiptSource {

    @Value("${app.receipt.source.file}")
    private String filePath;
    private final ObjectMapper objectMapper;
    private Iterator<Receipt> receiptIterator;

    @PostConstruct
    public void importReceipts() throws IOException {
        List<Receipt> receipts = objectMapper.readValue(this.getClass().getResourceAsStream(filePath),
                new TypeReference<List<Receipt>>() {});
        receiptIterator = receipts.listIterator();
    }

    public Optional<Receipt> getReceipt() {
        if (receiptIterator.hasNext()) {
            return Optional.of(receiptIterator.next());
        } else {
            return Optional.empty();
        }
    }
}
