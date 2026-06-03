package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component("/start")
public class StartCommand extends BaseCommand {

    @Override
    public void execute(Update update) {
        Long telegramId = getChatId(update);
        String username = update.hasMessage() && update.getMessage().getFrom() != null
                ? update.getMessage().getFrom().getUserName()
                : null;
        User user = userService.getOrCreateUser(telegramId, username);

        sendMessage(user.getTelegramId(), "Привет! Я TimeTaskBot. Я помогу тебе вести учет рабочего времени.\n\n" +
                "Выберите действие:", mainMenuKeyboard());
    }
}
