package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.entity.User;
import com.fleishera.taskchrono.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component("/tasks")
public class TasksCommand extends BaseCommand {

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(Update update) {
        Long telegramId = getChatId(update);
        User user = userService.getOrCreateUser(telegramId, null);
        List<Task> tasks = taskService.getTasks(user);

        if (tasks.isEmpty()) {
            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder().text("Создать задачу").callbackData("/create_task").build()
                    ))
                    .build();
            sendMessage(telegramId, "У тебя пока нет задач.", keyboard);
        } else {
            InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
            for (Task task : tasks) {
                keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(task.getTitle())
                                .callbackData("task_details:" + task.getId())
                                .build()
                ));
            }
            keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder().text("Создать задачу").callbackData("/create_task").build(),
                    InlineKeyboardButton.builder().text("Статистика").callbackData("/stats").build()
            ));
            sendMessage(telegramId, "Твои задачи:", keyboardBuilder.build());
        }
    }
}
