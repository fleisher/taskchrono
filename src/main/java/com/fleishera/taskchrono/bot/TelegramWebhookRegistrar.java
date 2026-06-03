package com.fleishera.taskchrono.bot;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bot.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramWebhookRegistrar {

    private static final long INITIAL_DELAY_SECONDS = 5;
    private static final long RETRY_DELAY_SECONDS = 30;

    private final TelegramClient telegramClient;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Value("${bot.webhook-url:}")
    private String webhookUrl;

    @Value("${bot.webhook-path:/telegram/webhook}")
    private String webhookPath;

    @Value("${bot.webhook-secret:}")
    private String webhookSecret;

    @EventListener(ApplicationReadyEvent.class)
    public void registerWebhook() {
        if (!StringUtils.hasText(webhookUrl)) {
            log.warn("BOT_WEBHOOK_URL is not configured. Telegram webhook was not registered.");
            return;
        }

        executorService.schedule(this::registerWebhookWithRetry, INITIAL_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void registerWebhookWithRetry() {
        try {
            String normalizedWebhookUrl = normalizeWebhookUrl(webhookUrl);
            SetWebhook.SetWebhookBuilder<?, ?> builder = SetWebhook.builder()
                    .url(normalizedWebhookUrl)
                    .dropPendingUpdates(false);
            if (StringUtils.hasText(webhookSecret)) {
                builder.secretToken(webhookSecret);
            }
            telegramClient.execute(builder.build());
            log.info("Telegram webhook registered: {}", normalizedWebhookUrl);
            executorService.shutdown();
        } catch (TelegramApiRequestException e) {
            log.error("Telegram webhook registration failed. errorCode={}, apiResponse={}. Retrying in {} seconds",
                    e.getErrorCode(), e.getApiResponse(), RETRY_DELAY_SECONDS, e);
            scheduleRetry();
        } catch (Exception e) {
            log.error("Telegram webhook registration failed. Retrying in {} seconds", RETRY_DELAY_SECONDS, e);
            scheduleRetry();
        }
    }

    private void scheduleRetry() {
        if (!executorService.isShutdown()) {
            executorService.schedule(this::registerWebhookWithRetry, RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private String normalizeWebhookUrl(String rawWebhookUrl) {
        String trimmed = rawWebhookUrl.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "https://" + trimmed;
        }

        URI uri = URI.create(trimmed);
        if (StringUtils.hasText(uri.getPath()) && !"/".equals(uri.getPath())) {
            return trimmed;
        }

        String normalizedPath = webhookPath.startsWith("/") ? webhookPath : "/" + webhookPath;
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) + normalizedPath : trimmed + normalizedPath;
    }

    @PreDestroy
    public void stop() {
        executorService.shutdownNow();
    }
}
