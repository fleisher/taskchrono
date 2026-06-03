package com.fleishera.taskchrono.bot;

import com.fleishera.taskchrono.command.Command;
import com.fleishera.taskchrono.command.CommandRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bot.enabled", havingValue = "true", matchIfMissing = true)
public class TimeTaskBot {

    private final CommandRegistry commandRegistry;
    private final TelegramClient telegramClient;

    public void process(Update update) {
        try {
            answerCallbackQuery(update);
            Command command = commandRegistry.getCommand(update);
            command.execute(update);
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }

    private void answerCallbackQuery(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }

        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(update.getCallbackQuery().getId())
                    .build());
        } catch (Exception e) {
            log.error("Error answering callback query", e);
        }
    }
}
