package br.com.clinica.api.controller;

import br.com.clinica.api.exception.ProfissionalNaoEncontradoException;
import br.com.clinica.api.model.Profissional;
import br.com.clinica.api.service.ProfissionalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ProfissionalController implements HttpHandler {

    private final ProfissionalService profissionalService;
    private final ObjectMapper objectMapper;

    public ProfissionalController(ProfissionalService profissionalService, ObjectMapper objectMapper) {
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
            List<Profissional> profissionais;
            if (exchange.getRequestURI().getPath().equals("/api/profissionais/todos")) {
                profissionais = profissionalService.buscarTodos();
            } else {
                Map<String, String> parametros = extrairParametros(exchange);
                profissionais = profissionalService.buscarProfissionais(
                        parametros.get("nome"), parametros.get("especialidade"));
            }
            HttpJson.send(exchange, objectMapper, 200, profissionais);
        } catch (ProfissionalNaoEncontradoException exception) {
            HttpJson.send(exchange, objectMapper, 404, Map.of("mensagem", exception.getMessage()));
        } catch (RuntimeException exception) {
            HttpJson.send(exchange, objectMapper, 500,
                    Map.of("mensagem", "Os dados dos profissionais estao indisponiveis."));
        }
    }

    private Map<String, String> extrairParametros(HttpExchange exchange) {
        Map<String, String> parametros = new HashMap<>();
        String consulta = exchange.getRequestURI().getRawQuery();
        if (consulta == null || consulta.isBlank()) {
            return parametros;
        }

        for (String parametro : consulta.split("&")) {
            String[] partes = parametro.split("=", 2);
            String chave = decodificar(partes[0]);
            String valor = partes.length == 2 ? decodificar(partes[1]) : "";
            parametros.put(chave, valor);
        }
        return parametros;
    }

    private String decodificar(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }
}