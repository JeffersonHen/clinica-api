# API Clinica de Saude

API REST em Java puro para consulta de profissionais de saude. O servidor HTTP usa `HttpServer` do JDK, os dados sao lidos de um arquivo JSON local e o Jackson faz a conversao entre objetos Java e JSON.

## Tecnologias

- Java 25
- `com.sun.net.httpserver.HttpServer`
- Jackson
- Maven
- JSON

## Como executar

Requer Java 25 e Maven instalados.

```bash
cd clinica-api
./run/run.sh
```

A aplicacao inicia em `http://localhost:8080`.

O script executa `mvn package` e inicia o JAR executavel. Como ele calcula a raiz do projeto sozinho, tambem pode ser chamado por caminho absoluto a partir de outra pasta:

```bash
/home/jeffe/projetos/clinica-api/run/run.sh
```

## Endpoints

| Metodo | Rota | Descricao |
| --- | --- | --- |
| GET | `/` | Mostra os endpoints disponiveis. |
| GET | `/api-docs` | Abre a interface Swagger UI padrão para testar a API. |
| GET | `/swagger` | Alias da interface Swagger UI. |
| GET | `/openapi.json` | Retorna a especificacao OpenAPI. |
| GET | `/api/profissionais` | Lista todos os profissionais. |
| GET | `/api/profissionais/todos` | Lista todos os profissionais explicitamente, sem filtros. |
| GET | `/api/profissionais?nome=Carlos` | Busca parcial por nome, sem diferenciar maiusculas e minusculas. |
| GET | `/api/profissionais?especialidade=Cardiologia` | Filtra por especialidade. |
| GET | `/api/profissionais?nome=Carlos&especialidade=Cardiologia` | Aplica os dois filtros. |
| GET | `/api/especialidades` | Lista especialidades sem duplicacao. |

Cada profissional possui `id`, `nome`, `especialidade` e `disponivel`.

O projeto nao utiliza Spring, banco de dados, Docker ou autenticacao. As dependencias sao gerenciadas diretamente pelo Maven.

Para testar pelo Swagger, inicie a API e acesse [http://localhost:8080/api-docs](http://localhost:8080/api-docs). A pagina usa a especificacao local em `/openapi.json`.

Quando uma busca com filtro nao encontra resultados, a API retorna HTTP 404 e uma resposta como:

```json
{
  "mensagem": "Nenhum profissional encontrado."
}
```

## Testes

```bash
mvn test
```

## Estrutura

- `model`: representa os profissionais.
- `repository`: le `src/main/resources/data/profissionais.json` com Jackson.
- `service`: aplica os filtros e lista especialidades.
- `controller`: implementa os handlers HTTP do JDK.