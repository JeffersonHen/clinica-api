package br.com.clinica.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.clinica.api.controller.ApiController;
import br.com.clinica.api.controller.EspecialidadeController;
import br.com.clinica.api.controller.ProfissionalController;
import br.com.clinica.api.controller.SwaggerController;
import br.com.clinica.api.repository.ProfissionalRepository;
import br.com.clinica.api.service.ProfissionalService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClinicaApiHttpTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static HttpServer server;
    private static ExecutorService executor;
    private static String baseUrl;

    @BeforeAll
    static void iniciarServidorDeTeste() throws IOException {
        ProfissionalRepository repository = new ProfissionalRepository(OBJECT_MAPPER);
        ProfissionalService service = new ProfissionalService(repository);

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        executor = Executors.newVirtualThreadPerTaskExecutor();
        server.createContext("/", new ApiController(OBJECT_MAPPER));
        server.createContext("/api/profissionais", new ProfissionalController(service, OBJECT_MAPPER));
        server.createContext("/api/especialidades", new EspecialidadeController(service, OBJECT_MAPPER));
        server.createContext("/openapi.json", new SwaggerController(OBJECT_MAPPER));
        server.createContext("/swagger", new SwaggerController(OBJECT_MAPPER));
        server.createContext("/api-docs", new SwaggerController(OBJECT_MAPPER));
        server.setExecutor(executor);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterAll
    static void pararServidorDeTeste() {
        server.stop(0);
        executor.close();
    }

    @Test
    void deveInformarQueAApiEstaOnline() throws Exception {
        HttpResponse<String> response = get("/");
        JsonNode body = json(response);

        assertEquals(200, response.statusCode());
        assertEquals("API Clinica de Saude online.", body.get("mensagem").asText());
        assertTrue(body.get("endpoints").toString().contains("/api/profissionais"));
    }

    @Test
    void deveListarTodosOsProfissionaisComSeusDados() throws Exception {
        HttpResponse<String> response = get("/api/profissionais/todos");
        JsonNode body = json(response);

        assertEquals(200, response.statusCode());
        assertEquals(10, body.size());
        assertEquals(1, body.get(0).get("id").asInt());
        assertEquals("Dr. Carlos Silva", body.get(0).get("nome").asText());
        assertEquals("Cardiologia", body.get(0).get("especialidade").asText());
        assertTrue(body.get(0).get("disponivel").asBoolean());
        assertFalse(body.get(2).get("disponivel").asBoolean());
    }

    @Test
    void deveBuscarPorNomeIgnorandoMaiusculasEMinusculas() throws Exception {
        HttpResponse<String> response = get("/api/profissionais?nome=Carlos");
        JsonNode body = json(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, body.size());
        assertEquals("Dr. Carlos Silva", body.get(0).get("nome").asText());
    }

    @Test
    void deveFiltrarPorEspecialidade() throws Exception {
        HttpResponse<String> response = get("/api/profissionais?especialidade=cardiologia");
        JsonNode body = json(response);

        assertEquals(200, response.statusCode());
        assertEquals(2, body.size());
        assertEquals(List.of("Dr. Carlos Silva", "Dr. Rafael Santos"),
                List.of(body.get(0).get("nome").asText(), body.get(1).get("nome").asText()));
    }

    @Test
    void deveAplicarNomeEEspecialidadeAoMesmoTempo() throws Exception {
        HttpResponse<String> response = get(
                "/api/profissionais?nome=Rafael&especialidade=Cardiologia");
        JsonNode body = json(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, body.size());
        assertEquals("Dr. Rafael Santos", body.get(0).get("nome").asText());
    }

    @Test
    void deveListarEspecialidadesSemDuplicacao() throws Exception {
        HttpResponse<String> response = get("/api/especialidades");
        JsonNode body = json(response);

        assertEquals(200, response.statusCode());
        assertEquals(List.of("Cardiologia", "Dermatologia", "Nutrição", "Ortopedia", "Pediatria"),
            OBJECT_MAPPER.convertValue(body, new TypeReference<List<String>>() {}));
    }

    @Test
    void deveRetornar404QuandoNaoEncontrarProfissional() throws Exception {
        HttpResponse<String> response = get("/api/profissionais?nome=Inexistente");
        JsonNode body = json(response);

        assertEquals(404, response.statusCode());
        assertEquals("Nenhum profissional encontrado.", body.get("mensagem").asText());
    }

    @Test
    void deveRecusarMetodoNaoPermitido() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/profissionais"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode body = json(response);

        assertEquals(405, response.statusCode());
        assertEquals("GET", response.headers().firstValue("Allow").orElseThrow());
        assertEquals("Metodo nao permitido.", body.get("mensagem").asText());
    }

    @Test
    void deveDisponibilizarOpenApiValido() throws Exception {
        HttpResponse<String> response = get("/openapi.json");
        JsonNode body = json(response);

        assertEquals(200, response.statusCode());
        assertEquals("3.0.3", body.get("openapi").asText());
        assertTrue(body.get("paths").has("/api/profissionais"));
        assertTrue(body.get("paths").has("/api/especialidades"));
    }

    @Test
    void deveServirPaginaDoSwagger() throws Exception {
        HttpResponse<String> response = get("/swagger");

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElseThrow()
                .startsWith("text/html"));
        assertTrue(response.body().contains("SwaggerUIBundle"));
        assertTrue(response.body().contains("/openapi.json"));
    }

    private static HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri(path)).GET().build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static URI uri(String path) {
        return URI.create(baseUrl + path);
    }

    private static JsonNode json(HttpResponse<String> response) throws IOException {
        return OBJECT_MAPPER.readTree(response.body());
    }
}
