package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.entity.User;
import com.fleishera.taskchrono.service.TaskService;
import com.fleishera.taskchrono.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Component("confirm_start_timer")
public class ConfirmStartTimerCommand extends BaseCommand {

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

        timerService.startStopwatch(user, task);
        sendMessage(telegramId, "Предыдущий таймер остановлен. Новый таймер запущен для задачи: " + task.getTitle(), InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Открыть задачу").callbackData("task_details:" + task.getId()).build()
                ))
                .build());
    }
}
