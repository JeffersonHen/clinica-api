package br.com.clinica.api;

import br.com.clinica.api.controller.EspecialidadeController;
import br.com.clinica.api.controller.ApiController;
import br.com.clinica.api.controller.ProfissionalController;
import br.com.clinica.api.repository.ProfissionalRepository;
import br.com.clinica.api.service.ProfissionalService;
import br.com.clinica.api.controller.SwaggerController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClinicaApiApplication {

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProfissionalRepository profissionalRepository = new ProfissionalRepository(objectMapper);
        ProfissionalService profissionalService = new ProfissionalService(profissionalRepository);

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                ApiController apiController = new ApiController(objectMapper);
            ProfissionalController profissionalController =
                    new ProfissionalController(profissionalService, objectMapper);
            EspecialidadeController especialidadeController =
                    new EspecialidadeController(profissionalService, objectMapper);
                SwaggerController swaggerController = new SwaggerController(objectMapper);

            server.createContext("/", apiController);
            server.createContext("/api/profissionais", profissionalController);
            server.createContext("/api/especialidades", especialidadeController);
                server.createContext("/openapi.json", swaggerController);
                server.createContext("/swagger", swaggerController);
            server.createContext("/api-docs", swaggerController);
            server.setExecutor(executor);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.stop(0);
                executor.close();
            }));
            server.start();

            System.out.println("API Clinica de Saude iniciada em http://localhost:8080");
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel iniciar a API na porta 8080.", exception);
        }
    }
}