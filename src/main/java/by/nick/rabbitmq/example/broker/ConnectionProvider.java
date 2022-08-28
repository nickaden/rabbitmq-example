package by.nick.rabbitmq.example.broker;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class ConnectionProvider {

    private final ConnectionFactory connectionFactory;
    @SuppressWarnings("FieldMayBeFinal")
    private Map<Object, Connection> connectionPool = new HashMap<>();

    @PreDestroy
    public void tearDown() {
        connectionPool.values().forEach(connection -> {
            try {
                connection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Connection getConnection(Object requester) {
        return connectionPool.computeIfAbsent(requester, key -> {
            try {
                return connectionFactory.newConnection();
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
