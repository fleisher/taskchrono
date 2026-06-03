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

@Component("/create_task")
public class CreateTaskCommand extends BaseCommand {

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(Update update) {
        Long telegramId = getChatId(update);
        User user = userService.getOrCreateUser(telegramId, null);

        if (update.hasCallbackQuery() || isCreateTaskCommand(update)) {
            userService.updateUserState(user, "AWAITING_TASK_TITLE");
            sendMessage(telegramId, "Введите название новой задачи:", mainMenuKeyboard());
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            sendMessage(telegramId, "Введите текстом название задачи.", mainMenuKeyboard());
            return;
        }

        String state = user.getBotState();
        String text = update.getMessage().getText().trim();
        if ("AWAITING_TASK_TITLE".equals(state)) {
            Task task = taskService.createTask(user, text, null);
            userService.updateUserState(user, "AWAITING_TASK_DESCRIPTION:" + task.getId());
            sendMessage(telegramId, "Введите описание задачи:", skipDescriptionKeyboard(task.getId()));
        } else if (state != null && state.startsWith("AWAITING_TASK_DESCRIPTION:")) {
            Long taskId = Long.parseLong(state.split(":")[1]);
            Task task = taskService.getTaskById(taskId).orElseThrow();
            task.setDescription(text);
            taskService.updateTask(task);
            userService.updateUserState(user, null);
            sendMessage(telegramId, "Задача '" + task.getTitle() + "' создана.", taskCreatedKeyboard(task.getId()));
        }
    }

    private boolean isCreateTaskCommand(Update update) {
        return update.hasMessage() && update.getMessage().hasText() && "/create_task".equals(update.getMessage().getText());
    }

    private InlineKeyboardMarkup skipDescriptionKeyboard(Long taskId) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Без описания").callbackData("skip_task_description:" + taskId).build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Список задач").callbackData("/tasks").build()
                ))
                .build();
    }

    private InlineKeyboardMarkup taskCreatedKeyboard(Long taskId) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Открыть задачу").callbackData("task_details:" + taskId).build(),
                        InlineKeyboardButton.builder().text("Создать еще").callbackData("/create_task").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Список задач").callbackData("/tasks").build()
                ))
                .build();
    }
}
