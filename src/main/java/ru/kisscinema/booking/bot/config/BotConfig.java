package ru.kisscinema.booking.bot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.properties")
public class BotConfig {

    @Getter
    @Value("${bot.name}")
    String botName;

    @Getter
    @Value("${bot.token}")
    String token;
}

