package by.nick.rabbitmq.example.consumer.recovery;

import by.nick.rabbitmq.example.domain.Receipt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogOnlyDatabase {

    public final ObjectMapper objectMapper;

    //There is no real database - support please read logs XD
    public void store(Receipt receipt) throws JsonProcessingException {
        log.info("Stored new entry: {}", objectMapper.writeValueAsString(receipt));
    }
}
