package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
public abstract class BaseCommand implements Command {

    @Autowired
    protected TelegramClient telegramClient;

    @Autowired
    protected UserService userService;

    protected Long getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return null;
    }

    protected void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    protected void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();
            telegramClient.execute(message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    protected void editMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
        try {
            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();
            telegramClient.execute(edit);
        } catch (Exception e) {
            log.error("Error editing message", e);
        }
    }

    protected InlineKeyboardMarkup mainMenuKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Список задач").callbackData("/tasks").build(),
                        InlineKeyboardButton.builder().text("Создать задачу").callbackData("/create_task").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Статистика").callbackData("/stats").build()
                ))
                .build();
    }
}
