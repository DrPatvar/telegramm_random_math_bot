package io.proj3ct.telegrammultbot.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.sql.SQLException;

@Configuration
@Data
@Slf4j
@PropertySource("classpath:application.properties")
public class BotConfig {

    @Value("${telegram.botName}")
    String botUserName;

    @Value("${telegram.botToken}")
    String token;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2Server() throws SQLException{
        log.info("Start H2 TCP server");
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
    }
}
