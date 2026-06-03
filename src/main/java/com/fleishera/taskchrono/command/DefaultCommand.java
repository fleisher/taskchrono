package com.fleishera.taskchrono.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class DefaultCommand extends BaseCommand {

    @Override
    public void execute(Update update) {
        Long chatId = getChatId(update);
        if (chatId != null) {
            sendMessage(chatId, "Команда не распознана. Выберите действие:", mainMenuKeyboard());
        }
    }
}
