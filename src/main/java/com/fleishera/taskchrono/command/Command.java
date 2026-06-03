package com.fleishera.taskchrono.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface Command {
    void execute(Update update) throws TelegramApiException;
}
