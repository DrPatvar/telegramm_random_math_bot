package io.proj3ct.telegrammultbot.repository;

import io.proj3ct.telegrammultbot.model.Multiplication;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MultiplicationRepository extends CrudRepository<Multiplication, Integer> {

    @Query("SELECT m FROM Multiplication m WHERE m.chatId =:chatId ORDER BY m.id DESC")
    List<Multiplication>  getByChatId (@Param("chatId") Long chatId);

}
