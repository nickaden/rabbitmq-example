package by.nick.rabbitmq.example.configuration;

public class BrokerConstants {

    private BrokerConstants() {


    }

    public static final String RECEIPT_EXCHANGE = "receipt-exchange";
    public static final String MAIN_RECEIPT_QUEUE = "receipt-queue";
    public static final String MAIN_RECEIPT_ROUTE_KEY = "receipt-main";

    public static final String RECEIPT_DLX_EXCHANGE = "receipt-exchange-recovery";
    public static final String RECEIPT_DLX_QUEUE = "receipt-queue-recovery";
    public static final String RECEIPT_DLX_ROUTE_KEY = "receipt-recovery";
}
