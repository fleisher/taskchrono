package com.fleishera.taskchrono.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bot.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramWebhookServer {

    private final TimeTaskBot bot;
    private final ObjectMapper objectMapper;

    @Value("${bot.webhook-path:/telegram/webhook}")
    private String webhookPath;

    @Value("${bot.webhook-secret:}")
    private String webhookSecret;

    @Value("${bot.webhook-port:8080}")
    private int webhookPort;

    private HttpServer server;
    private ExecutorService executorService;

    @EventListener(ApplicationReadyEvent.class)
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(webhookPort), 0);
        server.createContext(webhookPath, this::handleUpdate);
        executorService = Executors.newFixedThreadPool(4);
        server.setExecutor(executorService);
        server.start();
        log.info("Telegram webhook HTTP server started on port {} path {}", webhookPort, webhookPath);
    }

    private void handleUpdate(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        if (StringUtils.hasText(webhookSecret)) {
            String secretHeader = exchange.getRequestHeaders().getFirst("X-Telegram-Bot-Api-Secret-Token");
            if (!webhookSecret.equals(secretHeader)) {
                sendResponse(exchange, 401, "Unauthorized");
                return;
            }
        }

        try (InputStream body = exchange.getRequestBody()) {
            Update update = objectMapper.readValue(body, Update.class);
            bot.process(update);
            sendResponse(exchange, 200, "OK");
        } catch (Exception e) {
            log.error("Error handling Telegram webhook update", e);
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.stop(5);
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
