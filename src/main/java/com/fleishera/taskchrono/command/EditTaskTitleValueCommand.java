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

@Component("edit_task_title_value")
public class EditTaskTitleValueCommand extends BaseCommand {

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(Update update) {
        Long telegramId = getChatId(update);
        User user = userService.getOrCreateUser(telegramId, null);
        Long taskId = Long.parseLong(user.getBotState().split(":")[1]);
        Task task = taskService.getTaskById(taskId).orElseThrow();
        task.setTitle(update.getMessage().getText().trim());
        taskService.updateTask(task);
        userService.updateUserState(user, null);
        sendMessage(telegramId, "Название обновлено.", taskKeyboard(taskId));
    }

    private InlineKeyboardMarkup taskKeyboard(Long taskId) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Открыть задачу").callbackData("task_details:" + taskId).build()
                ))
                .build();
    }
}
