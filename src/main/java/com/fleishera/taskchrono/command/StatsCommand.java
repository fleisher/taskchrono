package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.entity.TimerSession;
import com.fleishera.taskchrono.entity.User;
import com.fleishera.taskchrono.service.StatsService;
import com.fleishera.taskchrono.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

@Component("/stats")
public class StatsCommand extends BaseCommand {

    @Autowired
    private StatsService statsService;

    @Autowired
    private TimerService timerService;

    @Override
    public void execute(Update update) {
        Long telegramId = getChatId(update);
        User user = userService.getOrCreateUser(telegramId, null);
        List<Task> tasks = statsService.getUserTasks(user);

        if (tasks.isEmpty()) {
            sendMessage(telegramId, "Статистика пока пустая: задач еще нет.", InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder().text("Создать задачу").callbackData("/create_task").build()
                    ))
                    .build());
            return;
        }

        StringBuilder text = new StringBuilder("Статистика по задачам:\n\n");
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
        for (Task task : tasks) {
            long seconds = statsService.getTotalSecondsForTask(user, task);
            seconds += timerService.getActiveSession(user, task)
                    .map(statsService::getCurrentDurationSeconds)
                    .orElse(0L);
            String duration = statsService.formatDuration(seconds);
            text.append(task.getTitle()).append(": ").append(duration).append("\n");
            keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(task.getTitle() + " - " + duration)
                            .callbackData("task_stats:" + task.getId())
                            .build()
            ));
        }
        keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("Список задач").callbackData("/tasks").build(),
                InlineKeyboardButton.builder().text("Создать задачу").callbackData("/create_task").build()
        ));

        sendMessage(telegramId, text.toString(), keyboardBuilder.build());
    }
}
