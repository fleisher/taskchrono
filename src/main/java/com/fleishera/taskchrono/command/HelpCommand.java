package com.fleishera.taskchrono.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component("/help")
public class HelpCommand extends BaseCommand {

    @Override
    public void execute(Update update) {
        String text = "*Доступные команды:*\n\n" +
                "/start — Старт и регистрация\n" +
                "/tasks — Список активных задач\n" +
                "/create_task — Создать новую задачу\n" +
                "/stats — Статистика по задачам\n" +
                "/help — Справка";
        sendMessage(getChatId(update), text, mainMenuKeyboard());
    }
}
