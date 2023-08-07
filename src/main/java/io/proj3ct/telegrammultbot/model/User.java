package io.proj3ct.telegrammultbot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "users")
@Data
public class User {

    @Id
    private Long chatId;

    private String phoneNumber;

    private java.sql.Timestamp registeredAt;

    private String levelSelection;

    private String firstName;

    private String lastName;

    private String userName;

    private String bio;

    private String description;

    private String pinnedMessage;
}