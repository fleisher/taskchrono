package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Component("delete_task")
public class DeleteTaskCommand extends BaseCommand {

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(Update update) {
        Long taskId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        Task task = taskService.getTaskById(taskId).orElseThrow();
        String title = task.getTitle();
        taskService.deleteTask(task);
        sendMessage(getChatId(update), "Задача '" + title + "' удалена полностью.", InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Список задач").callbackData("/tasks").build(),
                        InlineKeyboardButton.builder().text("Создать задачу").callbackData("/create_task").build()
                ))
                .build());
    }
}
