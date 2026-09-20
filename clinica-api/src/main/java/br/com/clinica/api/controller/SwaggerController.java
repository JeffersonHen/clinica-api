package br.com.clinica.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class SwaggerController implements HttpHandler {

    private static final String OPEN_API_SPECIFICATION = """
            {
              "openapi": "3.0.3",
              "info": {
                "title": "API Clinica de Saude",
                "description": "Consulta de profissionais de saude da clinica.",
                "version": "1.0.0"
              },
              "servers": [
                { "url": "http://localhost:8080" }
              ],
              "paths": {
                "/api/profissionais/todos": {
                  "get": {
                    "summary": "Lista todos os profissionais sem filtros",
                    "responses": {
                      "200": {
                        "description": "Lista completa de profissionais",
                        "content": {
                          "application/json": {
                            "schema": {
                              "type": "array",
                              "items": { "$ref": "#/components/schemas/Profissional" }
                            }
                          }
                        }
                      }
                    }
                  }
                },
                "/api/profissionais": {
                  "get": {
                    "summary": "Lista e filtra profissionais",
                    "parameters": [
                      {
                        "name": "nome",
                        "in": "query",
                        "required": false,
                        "schema": { "type": "string" },
                        "description": "Busca parcial e sem diferenciar maiusculas e minusculas."
                      },
                      {
                        "name": "especialidade",
                        "in": "query",
                        "required": false,
                        "schema": { "type": "string" },
                        "description": "Filtra pela especialidade do profissional."
                      }
                    ],
                    "responses": {
                      "200": {
                        "description": "Lista de profissionais",
                        "content": {
                          "application/json": {
                            "schema": {
                              "type": "array",
                              "items": { "$ref": "#/components/schemas/Profissional" }
                            }
                          }
                        }
                      },
                      "404": {
                        "description": "Nenhum profissional encontrado",
                        "content": {
                          "application/json": {
                            "schema": { "$ref": "#/components/schemas/MensagemErro" }
                          }
                        }
                      }
                    }
                  }
                },
                "/api/especialidades": {
                  "get": {
                    "summary": "Lista especialidades disponiveis",
                    "responses": {
                      "200": {
                        "description": "Especialidades sem duplicacao",
                        "content": {
                          "application/json": {
                            "schema": {
                              "type": "array",
                              "items": { "type": "string" }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              },
              "components": {
                "schemas": {
                  "Profissional": {
                    "type": "object",
                    "required": ["id", "nome", "especialidade", "disponivel"],
                    "properties": {
                      "id": { "type": "integer", "format": "int64", "example": 1 },
                      "nome": { "type": "string", "example": "Dr. Carlos Silva" },
                      "especialidade": { "type": "string", "example": "Cardiologia" },
                      "disponivel": { "type": "boolean", "example": true }
                    }
                  },
                  "MensagemErro": {
                    "type": "object",
                    "properties": {
                      "mensagem": { "type": "string", "example": "Nenhum profissional encontrado." }
                    }
                  }
                }
              }
            }
            """;

    private static final String SWAGGER_HTML = """
            <!doctype html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>API Clinica de Saude - Swagger</title>
              <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
            </head>
            <body>
              <div id="swagger-ui"></div>
              <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
              <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-standalone-preset.js"></script>
              <script>
                window.onload = () => {
                  window.ui = SwaggerUIBundle({
                    url: '/openapi.json',
                    dom_id: '#swagger-ui',
                    deepLinking: true,
                    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
                    layout: 'StandaloneLayout'
                  });
                };
              </script>
            </body>
            </html>
            """;

    private final ObjectMapper objectMapper;

    public SwaggerController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET");
            HttpJson.send(exchange, objectMapper, 405, Map.of("mensagem", "Metodo nao permitido."));
            return;
        }

        if (exchange.getRequestURI().getPath().equals("/openapi.json")) {
            JsonNode specification = objectMapper.readTree(OPEN_API_SPECIFICATION);
            HttpJson.send(exchange, objectMapper, 200, specification);
            return;
        }

        HttpJson.sendText(exchange, 200, SWAGGER_HTML, "text/html; charset=UTF-8");
    }
}