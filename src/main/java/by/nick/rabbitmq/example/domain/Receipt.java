package by.nick.rabbitmq.example.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Receipt {

    private String id;
    private String recipient;
    private String sender;
    private String goodId;
    private String goodName;
    private BigDecimal amount;
}
