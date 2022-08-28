package by.nick.rabbitmq.example.configuration;

public class BrokerConstants {

    private BrokerConstants() {


    }

    public static final String RECEIPT_EXCHANGE = "receipt-exchange";
    public static final String MAIN_RECEIPT_QUEUE_1 = "receipt-queue-1";
    public static final String MAIN_RECEIPT_QUEUE_2 = "receipt-queue-2";
    public static final String MAIN_RECEIPT_ROUTE_KEY_1 = "receipt-main-1";
    public static final String MAIN_RECEIPT_ROUTE_KEY_2 = "receipt-main-2";

    public static final String RECEIPT_DLX_EXCHANGE = "receipt-exchange-recovery";
    public static final String RECEIPT_DLX_QUEUE = "receipt-queue-recovery";
    public static final String RECEIPT_DLX_ROUTE_KEY = "receipt-recovery";
}
