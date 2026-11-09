package com.tracker.notification;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TelegramChannel implements NotificationChannel {

    private final String token;
    private final String chatId;
    private final HttpClient client = HttpClient.newHttpClient();

    public TelegramChannel(@Value("${tracker.telegram.token:}") String token,
                           @Value("${tracker.telegram.chat-id:}") String chatId) {
        this.token = token;
        this.chatId = chatId;
    }

    @Override
    public String name() {
        return "Telegram";
    }

    @Override
    public boolean isEnabled() {
        return !token.isBlank() && !chatId.isBlank();
    }

    @Override
    public void send(String message) throws Exception {
        String body = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.telegram.org/bot" + token + "/sendMessage"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Telegram replied " + response.statusCode() + ": " + response.body());
        }
    }
}
