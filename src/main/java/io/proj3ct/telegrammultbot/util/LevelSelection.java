package io.proj3ct.telegrammultbot.util;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class LevelSelection {

    private static final String EASY_LEVEL = "EASY";
    private static final String MEDIUM_LEVEL = "MEDIUM";
    private static final String HARD_LEVEL = "HARD";

    public static SendMessage levelSelection(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите уровень сложности");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var easyButton = new InlineKeyboardButton();
        var mediumButton = new InlineKeyboardButton();
        var hardButton = new InlineKeyboardButton();

        easyButton.setText("легкий");
        easyButton.setCallbackData(EASY_LEVEL);

        mediumButton.setText("средний");
        mediumButton.setCallbackData(MEDIUM_LEVEL);

        hardButton.setText("тяжелый");
        hardButton.setCallbackData(HARD_LEVEL);

        rowInline.add(easyButton);
        rowInline.add(mediumButton);
        rowInline.add(hardButton);

        rowsInline.add(rowInline);

        markup.setKeyboard(rowsInline);
        message.setReplyMarkup(markup);

       return message;
    }
}
