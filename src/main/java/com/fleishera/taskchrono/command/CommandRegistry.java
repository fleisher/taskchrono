package com.fleishera.taskchrono.command;

import com.fleishera.taskchrono.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommandRegistry {

    private final Map<String, Command> commands;
    private final DefaultCommand defaultCommand;
    private final UserService userService;

    public Command getCommand(Update update) {
        Long telegramId = null;
        if (update.hasMessage()) {
            telegramId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            telegramId = update.getCallbackQuery().getMessage().getChatId();
        }

        if (telegramId != null && update.hasMessage() && update.getMessage().hasText() && !update.getMessage().getText().startsWith("/")) {
            com.fleishera.taskchrono.entity.User user = userService.getOrCreateUser(telegramId, null);
            if (user.getBotState() != null) {
                if ("AWAITING_TASK_TITLE".equals(user.getBotState())) {
                    return commands.get("/create_task");
                } else if (user.getBotState().startsWith("AWAITING_TASK_DESCRIPTION:")) {
                    return commands.get("/create_task");
                } else if (user.getBotState().startsWith("AWAITING_EDIT_TASK_TITLE:")) {
                    return commands.get("edit_task_title_value");
                } else if (user.getBotState().startsWith("AWAITING_EDIT_TASK_DESCRIPTION:")) {
                    return commands.get("edit_task_description_value");
                }
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            if (text.startsWith("/")) {
                String commandKey = text.split(" ")[0];
                return commands.getOrDefault(commandKey, defaultCommand);
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            String commandKey = data.split(":")[0];
            return commands.getOrDefault(commandKey, defaultCommand);
        }
        return defaultCommand;
    }
}
