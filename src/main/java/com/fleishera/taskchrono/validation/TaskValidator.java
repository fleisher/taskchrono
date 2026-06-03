package com.fleishera.taskchrono.validation;

import com.fleishera.taskchrono.exception.BotException;
import org.springframework.stereotype.Component;

@Component
public class TaskValidator {
    public void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BotException("Название задачи не может быть пустым.");
        }
        if (title.length() > 255) {
            throw new BotException("Название задачи слишком длинное.");
        }
    }
}
