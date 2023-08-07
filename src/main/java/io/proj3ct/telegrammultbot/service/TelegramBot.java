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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.proj3ct.telegrammultbot.util.LevelSelection.levelSelection;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private BotConfig config;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MultiplicationRepository multiplicationRepository;

    static final String NEXT_EXAM = "Следующий пример";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начало"));
        listOfCommands.add(new BotCommand("/mult", "Задачи"));
        listOfCommands.add(new BotCommand("/statistics", "Результаты"));
        listOfCommands.add(new BotCommand("/help", "Помощь"));
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
                    start(update, chatId);
                }
                case "/mult" -> {
                    calculation(chatId);
                }
                case "/statistics" -> {
                    List<Multiplication> multiplicationList = multiplicationRepository.findByChatIdAndDateCompleteAfter(chatId, Timestamp.valueOf(LocalDateTime.of(LocalDate.now(), LocalTime.MIN)));
                    String msg = "Твои результаты за сегодня ";
                    String msg2 = "У вас нет решенных задач ";
                    statistics(update, chatId, multiplicationList, msg, msg2);
                }
                case "/all_statistics" -> {
                    List<Multiplication> multiplicationList = multiplicationRepository.getByChatId(chatId);
                    String msg = "Твои результаты ";
                    String msg2 = "У вас нет решенных задач";
                    statistics(update, chatId, multiplicationList, msg, msg2);
                }

                case "/help" -> {
                    sendMessage("/start - начала программы\n" +
                            "/mult - выдача новой задачи для решения\n" +
                            "/statistics - статистика решений за текущий день\n" +
                            "/all_statistics - статистика решений за весь период\n", chatId);
                }

                default -> {
                    List<Multiplication> multiplicationList = multiplicationRepository.getByChatId(chatId);
                    Multiplication dbMultiplication = multiplicationList.get(0);
                    int answer;
                    int dbAnswer = dbMultiplication.getAnswer();
                    try {
                        answer = Integer.parseInt(update.getMessage().getText());
                        if (answer == dbAnswer) {
                            addAnswerDB(chatId, dbMultiplication, answer, "Правильно ", ":smile:", true);
                        } else {
                            addAnswerDB(chatId, dbMultiplication, answer, "Неправильно ", ":frowning_face:", false);
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
            switch (callbackData) {
                case "Следующий пример" -> {
                    calculation(chatId);
                }
                case "EASY" -> {
                    addButtonAndSendMessage("Вы выбрали легкий уровень, умножение чисел от 1 до 5", chatId);
                    updateUser(callbackData, chatId);
                }
                case "MEDIUM"->{
                    addButtonAndSendMessage("Вы выбрали средний уровень, умножение чисел от 1 до 7", chatId);
                    updateUser(callbackData, chatId);
                }
                case "HARD"->{
                    addButtonAndSendMessage("Вы выбрали сложный уровень, умножение чисел от 1 до 9", chatId);
                    updateUser(callbackData, chatId);
                }
            }
        }
    }

    private void addAnswerDB(long chatId, Multiplication dbMultiplication, int answer, String s, String s2, boolean b) {
        addButtonAndSendMessage(EmojiParser.parseToUnicode(s + s2), chatId);
        dbMultiplication.setVerify(b);
        dbMultiplication.setYourAnswer(answer);
        dbMultiplication.setDateComplete(Timestamp.valueOf(LocalDateTime.now()));
        multiplicationRepository.save(dbMultiplication);
    }

    private void statistics(Update update, long chatId, List<Multiplication> multiplicationList, String message, String messageFoul) {
        if (!multiplicationList.isEmpty()) {
            sendMessage(message + update.getMessage().getChat().getFirstName(), chatId);
            for (Multiplication mult : multiplicationList) {
                String answerEmoji = mult.isVerify() ? ":heavy_check_mark: " : ":x: ";
                String yourAnswer = mult.getYourAnswer()!=null ? String.valueOf(mult.getYourAnswer())  : "Не решен";
                sendMessage(EmojiParser.parseToUnicode(answerEmoji) + mult.getBody() + " = " + yourAnswer, chatId);
            }
        } else {
            sendMessage(messageFoul, chatId);
        }
    }

    private void calculation(long chatId) {
        User user = userRepository.getUserByChatId(chatId);
        RandomNumber.randomNumber(user.getLevelSelection());
        sendMessage(RandomNumber.stringExample, chatId);
        Multiplication multiplication = new Multiplication(RandomNumber.stringExample, RandomNumber.answer, chatId);
        multiplicationRepository.save(multiplication);
    }

    private void start(Update update, long chatId) {
        showStart(chatId, update.getMessage().getChat().getFirstName());
        creatUser(update);
        send(levelSelection(chatId));
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
        inlinekeyboardButton.setText(EmojiParser.parseToUnicode("Новый пример " + ":point_up_2:"));
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

    private void updateUser (String callbackData, long chatId){
        User user = userRepository.getUserByChatId(chatId);
        user.setLevelSelection(callbackData);
        userRepository.save(user);
    }
}
