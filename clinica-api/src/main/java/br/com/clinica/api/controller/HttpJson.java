package br.com.clinica.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

final class HttpJson {

    private HttpJson() {
    }

    static void send(HttpExchange exchange, ObjectMapper objectMapper, int status, Object body)
            throws IOException {
        sendText(exchange, status, objectMapper.writeValueAsString(body),
                "application/json; charset=UTF-8");
    }

    static void sendText(HttpExchange exchange, int status, String body, String contentType)
            throws IOException {
        byte[] conteudo = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, conteudo.length);
        try (OutputStream resposta = exchange.getResponseBody()) {
            resposta.write(conteudo);
        }
    }
}