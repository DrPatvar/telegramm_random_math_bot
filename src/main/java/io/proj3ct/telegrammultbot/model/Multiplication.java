package io.proj3ct.telegrammultbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
public class Multiplication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String body;

    private Integer answer;

    private Long chatId;

    private boolean verify;

    private java.sql.Timestamp dateComplete;

    public Multiplication() {
    }

    public Multiplication(String body, int result, long chatId) {
        this.body = body;
        this.answer = result;
        this.chatId = chatId;
        this.dateComplete = Timestamp.valueOf(LocalDateTime.now());
    }
}
