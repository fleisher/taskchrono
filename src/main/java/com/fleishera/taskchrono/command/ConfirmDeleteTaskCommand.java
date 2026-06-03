package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Component("confirm_delete_task")
public class ConfirmDeleteTaskCommand extends BaseCommand {

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(Update update) {
        Long taskId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        Task task = taskService.getTaskById(taskId).orElseThrow();
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Удалить навсегда").callbackData("delete_task:" + taskId).build(),
                        InlineKeyboardButton.builder().text("Отмена").callbackData("task_details:" + taskId).build()
                ))
                .build();
        sendMessage(getChatId(update), "Удалить задачу '" + task.getTitle() + "' вместе со всей историей времени?", keyboard);
    }
}
