package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.entity.TimerSession;
import com.fleishera.taskchrono.repository.TimerSessionRepository;
import com.fleishera.taskchrono.service.StatsService;
import com.fleishera.taskchrono.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Component("stop_timer")
public class StopTimerCommand extends BaseCommand {

    @Autowired
    private TimerService timerService;

    @Autowired
    private StatsService statsService;

    @Autowired
    private TimerSessionRepository timerSessionRepository;

    @Override
    public void execute(Update update) {
        Long sessionId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        TimerSession session = timerSessionRepository.findByIdWithTask(sessionId).orElseThrow();
        Task task = session.getTask();
        timerService.stopTimer(session);

        String text = String.format("Задача: *%s*\n\nОписание: %s\nВсего затрачено: %s",
                task.getTitle(),
                task.getDescription() != null ? task.getDescription() : "нет",
                statsService.formatDuration(statsService.getTotalSecondsForTask(task.getUser(), task)));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Запустить таймер").callbackData("start_timer:" + task.getId()).build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Статистика").callbackData("task_stats:" + task.getId()).build(),
                        InlineKeyboardButton.builder().text("Редактировать").callbackData("edit_task:" + task.getId()).build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Удалить").callbackData("confirm_delete_task:" + task.getId()).build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("К списку").callbackData("/tasks").build()
                ))
                .build();

        editMessage(getChatId(update), update.getCallbackQuery().getMessage().getMessageId(), text, keyboard);
    }
}
