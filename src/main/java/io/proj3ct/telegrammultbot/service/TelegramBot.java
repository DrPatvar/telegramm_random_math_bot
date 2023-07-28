package io.proj3ct.telegrammultbot.service;

import com.vdurmont.emoji.EmojiParser;
import io.proj3ct.telegrammultbot.config.BotConfig;
import io.proj3ct.telegrammultbot.model.Multiplication;
import io.proj3ct.telegrammultbot.model.User;
import io.proj3ct.telegrammultbot.repository.MultiplicationRepository;
import io.proj3ct.telegrammultbot.repository.UserRepository;
import io.proj3ct.telegrammultbot.util.RandomNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private BotConfig config;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MultiplicationRepository multiplicationRepository;

    static final int MAX_MULT_ID_MINUS_ONE = 8;

    static final String NEXT_EXAM = "Следующий пример";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "начало"));
        listOfCommands.add(new BotCommand("/mult", "Задачи"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start" -> {
                    showStart(chatId, update.getMessage().getChat().getFirstName());
                    creatUser(update);
                }
                case "/mult" -> {
                    RandomNumber randomNumber = new RandomNumber();
                    sendMessage(randomNumber.getStringExample(), chatId);
                    Multiplication multiplication = new Multiplication(randomNumber.getStringExample(), randomNumber.getAnswer(), chatId);
                    multiplicationRepository.save(multiplication);
                }
                case "/exit", "/statistics" -> {

                }
                default -> {
                    List<Multiplication> multiplicationList = multiplicationRepository.getByChatId(chatId);
                    Multiplication dbMultiplication = multiplicationList.get(0);
                    int answer;
                    int dbAnswer = dbMultiplication.getAnswer();
                    try {
                        answer = Integer.parseInt(update.getMessage().getText());
                        if (answer == dbAnswer) {
                            sendMessage("Правильно", chatId);
                            dbMultiplication.setVerify(true);
                            dbMultiplication.setDateComplete(Timestamp.valueOf(LocalDateTime.now()));
                            multiplicationRepository.save(dbMultiplication);
                        } else {
                            sendMessage("Неправильно", chatId);
                        }
                    } catch (Exception e) {
                        sendMessage("Введите цифры", chatId);
                        e.printStackTrace();
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (callbackData.equals(NEXT_EXAM)) {
                RandomNumber randomNumber = new RandomNumber();
                var mult = getRandomMult();
                mult.ifPresent(randomMult -> addButtonAndSendMessage(randomMult.getBody(), chatId));
            }
        }
    }

    private Optional<Multiplication> getRandomMult() {
        var r = new Random();
        var randomId = r.nextInt(MAX_MULT_ID_MINUS_ONE) + 1;
        return multiplicationRepository.findById(randomId);
    }

    private void addButtonAndSendMessage(String mult, long chatId) {
        SendMessage message = new SendMessage();
        message.setText(mult);
        message.setChatId(chatId);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var inlinekeyboardButton = new InlineKeyboardButton();
        inlinekeyboardButton.setCallbackData(NEXT_EXAM);
        inlinekeyboardButton.setText(EmojiParser.parseToUnicode("следующий пример " + ":rolling_on_the_floor_laughing:"));
        rowInline.add(inlinekeyboardButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        send(message);
    }

    private void showStart(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode(
                "Привет, " + name + "! :smile:" + " Приятно познакомиться! Я тренажер таблицы умножения, меня написал Ivan Shurkov \n");
        sendMessage(answer, chatId);
    }

    private void sendMessage(String textToSend, long chatId) {
        SendMessage message = new SendMessage(); // Create a message object object
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        send(message);
    }

    private void send(SendMessage msg) {
        try {
            execute(msg); // Sending our message object to user
        } catch (TelegramApiException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void creatUser(Update update) {
        User user = new User();
        user.setChatId(update.getMessage().getChatId());
        user.setFirstName(update.getMessage().getChat().getFirstName());
        user.setBio(update.getMessage().getChat().getBio());
        user.setLastName(update.getMessage().getChat().getLastName());
        user.setDescription(update.getMessage().getChat().getDescription());
        userRepository.save(user);
    }
}
