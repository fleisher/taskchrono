package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.entity.TimerSession;
import com.fleishera.taskchrono.entity.User;
import com.fleishera.taskchrono.service.StatsService;
import com.fleishera.taskchrono.service.TaskService;
import com.fleishera.taskchrono.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.List;

@Component("task_stats")
public class TaskStatsCommand extends BaseCommand {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'");

    @Autowired
    private TaskService taskService;

    @Autowired
    private StatsService statsService;

    @Autowired
    private TimerService timerService;

    @Override
    public void execute(Update update) {
        Long telegramId = getChatId(update);
        User user = userService.getOrCreateUser(telegramId, null);
        Long taskId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        Task task = taskService.getTaskById(taskId).orElseThrow();
        List<TimerSession> sessions = statsService.getFinishedSessionsForTask(user, task);
        long totalSeconds = statsService.getTotalSecondsForTask(user, task)
                + timerService.getActiveSession(user, task).map(statsService::getCurrentDurationSeconds).orElse(0L);

        StringBuilder text = new StringBuilder()
                .append("Статистика задачи: ").append(task.getTitle()).append("\n\n")
                .append("Всего: ").append(statsService.formatDuration(totalSeconds)).append("\n\n");

        if (sessions.isEmpty()) {
            text.append("Промежутков учета времени пока нет.");
        } else {
            text.append("Промежутки времени:\n");
            for (TimerSession session : sessions) {
                text.append(FORMATTER.format(session.getStartedAt().withOffsetSameInstant(ZoneOffset.UTC)))
                        .append(" - ")
                        .append(FORMATTER.format(session.getFinishedAt().withOffsetSameInstant(ZoneOffset.UTC)))
                        .append(": ")
                        .append(statsService.formatDuration(session.getDurationSeconds() != null ? session.getDurationSeconds() : 0))
                        .append("\n");
            }
        }

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("К карточке задачи").callbackData("task_details:" + taskId).build(),
                        InlineKeyboardButton.builder().text("К статистике").callbackData("/stats").build()
                ))
                .build();
        sendMessage(telegramId, text.toString(), keyboard);
    }
}
