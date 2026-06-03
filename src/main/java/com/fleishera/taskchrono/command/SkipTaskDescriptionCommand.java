package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.entity.User;
import com.fleishera.taskchrono.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Component("skip_task_description")
public class SkipTaskDescriptionCommand extends BaseCommand {

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(Update update) {
        Long telegramId = getChatId(update);
        User user = userService.getOrCreateUser(telegramId, null);
        Long taskId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        Task task = taskService.getTaskById(taskId).orElseThrow();
        userService.updateUserState(user, null);
        sendMessage(telegramId, "Задача '" + task.getTitle() + "' создана без описания.", InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Открыть задачу").callbackData("task_details:" + taskId).build(),
                        InlineKeyboardButton.builder().text("Создать еще").callbackData("/create_task").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Список задач").callbackData("/tasks").build()
                ))
                .build());
    }
}
