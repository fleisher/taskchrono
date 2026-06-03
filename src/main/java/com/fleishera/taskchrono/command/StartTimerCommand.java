package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.entity.TimerSession;
import com.fleishera.taskchrono.entity.User;
import com.fleishera.taskchrono.service.TaskService;
import com.fleishera.taskchrono.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Optional;

@Component("start_timer")
public class StartTimerCommand extends BaseCommand {

    @Autowired
    private TimerService timerService;

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(Update update) {
        Long telegramId = getChatId(update);
        User user = userService.getOrCreateUser(telegramId, null);

        String data = update.getCallbackQuery().getData();
        Long taskId = Long.parseLong(data.split(":")[1]);
        Task task = taskService.getTaskById(taskId).orElseThrow();

        Optional<TimerSession> activeSession = timerService.getActiveSession(user);

        if (activeSession.isPresent()) {
            TimerSession current = activeSession.get();
            if (current.getTask().getId().equals(taskId)) {
                sendMessage(telegramId, "Таймер для этой задачи уже запущен.", InlineKeyboardMarkup.builder()
                        .keyboardRow(new InlineKeyboardRow(
                                InlineKeyboardButton.builder().text("Открыть задачу").callbackData("task_details:" + taskId).build(),
                                InlineKeyboardButton.builder().text("Остановить таймер").callbackData("stop_timer:" + current.getId()).build()
                        ))
                        .build());
                return;
            }

            String text = String.format("Сейчас у вас уже запущен таймер для задачи '%s'. Запустить новый таймер и остановить текущий?",
                    current.getTask().getTitle());

            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder().text("✅ Да").callbackData("confirm_start_timer:" + taskId).build(),
                            InlineKeyboardButton.builder().text("❌ Нет").callbackData("task_details:" + taskId).build()
                    ))
                    .build();

            sendMessage(telegramId, text, keyboard);
        } else {
            timerService.startStopwatch(user, task);
            sendMessage(telegramId, "Таймер запущен для задачи: " + task.getTitle(), InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder().text("Открыть задачу").callbackData("task_details:" + task.getId()).build()
                    ))
                    .build());
        }
    }
}
