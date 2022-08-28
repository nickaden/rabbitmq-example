package by.nick.rabbitmq.example.consumer;

public class NotHandledMessageException extends RuntimeException {
    public NotHandledMessageException(String goodId) {
        super(goodId);
    }
}
