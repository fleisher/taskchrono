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

import java.util.Optional;

@Component("task_details")
public class TaskDetailsCommand extends BaseCommand {

    @Autowired
    private TaskService taskService;

    @Autowired
    private StatsService statsService;

    @Autowired
    private TimerService timerService;

    @Override
    public void execute(Update update) {
        String data = update.getCallbackQuery().getData();
        Long taskId = Long.parseLong(data.split(":")[1]);
        Optional<Task> taskOpt = taskService.getTaskById(taskId);

        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            User user = userService.getOrCreateUser(getChatId(update), null);
            Optional<TimerSession> activeTaskTimer = timerService.getActiveSession(user, task);
            long totalSeconds = statsService.getTotalSecondsForTask(task.getUser(), task)
                    + activeTaskTimer.map(statsService::getCurrentDurationSeconds).orElse(0L);
            String stats = statsService.formatDuration(totalSeconds);

            String text = String.format("Задача: *%s*\n\nОписание: %s\nВсего затрачено: %s",
                    task.getTitle(),
                    task.getDescription() != null ? task.getDescription() : "нет",
                    stats);

            InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
            InlineKeyboardButton timerButton;
            if (activeTaskTimer.isPresent()) {
                timerButton = InlineKeyboardButton.builder()
                        .text("Остановить таймер")
                        .callbackData("stop_timer:" + activeTaskTimer.get().getId())
                        .build();
            } else {
                timerButton = InlineKeyboardButton.builder()
                        .text("Запустить таймер")
                        .callbackData("start_timer:" + task.getId())
                        .build();
            }

            keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                    timerButton
            ));

            keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder().text("Статистика").callbackData("task_stats:" + task.getId()).build(),
                    InlineKeyboardButton.builder().text("Редактировать").callbackData("edit_task:" + task.getId()).build()
            ));
            keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder().text("Удалить").callbackData("confirm_delete_task:" + task.getId()).build()
            ));
            keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder().text("К списку").callbackData("/tasks").build()
            ));

            editMessage(getChatId(update), update.getCallbackQuery().getMessage().getMessageId(), text, keyboardBuilder.build());
        }
    }
}
