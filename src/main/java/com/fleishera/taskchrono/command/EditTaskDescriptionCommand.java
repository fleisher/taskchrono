package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component("edit_task_description")
public class EditTaskDescriptionCommand extends BaseCommand {

    @Override
    public void execute(Update update) {
        Long telegramId = getChatId(update);
        User user = userService.getOrCreateUser(telegramId, null);
        Long taskId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        userService.updateUserState(user, "AWAITING_EDIT_TASK_DESCRIPTION:" + taskId);
        sendMessage(telegramId, "Введите новое описание задачи:", mainMenuKeyboard());
    }
}
