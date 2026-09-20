package br.com.clinica.api.controller;

import br.com.clinica.api.service.ProfissionalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EspecialidadeController implements HttpHandler {

    private final ProfissionalService profissionalService;
    private final ObjectMapper objectMapper;

    public EspecialidadeController(ProfissionalService profissionalService, ObjectMapper objectMapper) {
        this.profissionalService = profissionalService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET");
            HttpJson.send(exchange, objectMapper, 405, Map.of("mensagem", "Metodo nao permitido."));
            return;
        }

        try {
            List<String> especialidades = profissionalService.listarEspecialidades();
            HttpJson.send(exchange, objectMapper, 200, especialidades);
        } catch (RuntimeException exception) {
            HttpJson.send(exchange, objectMapper, 500,
                    Map.of("mensagem", "Os dados dos profissionais estao indisponiveis."));
        }
    }
}