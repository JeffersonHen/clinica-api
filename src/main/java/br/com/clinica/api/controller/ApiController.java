package br.com.clinica.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ApiController implements HttpHandler {

    private final ObjectMapper objectMapper;

    public ApiController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET");
            HttpJson.send(exchange, objectMapper, 405, Map.of("mensagem", "Metodo nao permitido."));
            return;
        }

        Map<String, Object> resposta = Map.of(
                "mensagem", "API Clinica de Saude online.",
                "endpoints", List.of(
                        "/api/profissionais",
                    "/api/profissionais/todos",
                    "/api/especialidades",
                    "/swagger",
                    "/openapi.json"));
        HttpJson.send(exchange, objectMapper, 200, resposta);
    }
}
