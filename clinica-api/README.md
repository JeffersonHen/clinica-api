# API de Profissionais de Saúde

API REST desenvolvida em Java 25 para consultar os profissionais de uma clínica.
O sistema lê os dados de um arquivo JSON local e disponibiliza as informações
por HTTP em formato JSON.

Este projeto foi feito em Java. O
servidor HTTP utiliza `com.sun.net.httpserver.HttpServer`, e o Jackson é usado
para ler o arquivo JSON e serializar as respostas da API.

## O que a API faz

- Lista todos os profissionais cadastrados.
- Lista as especialidades disponíveis, sem duplicação.
- Busca profissionais por nome, aceitando busca parcial.
- Filtra profissionais por especialidade.
- Combina os filtros de nome e especialidade.
- Informa se cada profissional está disponível.
- Retorna mensagens JSON quando uma busca não encontra resultados.
- Disponibiliza uma interface Swagger para testar os endpoints pelo navegador.

## Tecnologias

- Java 25
- Maven
- `HttpServer` do JDK
- Jackson Databind
- JUnit 5
- JSON

## Dados

Os profissionais ficam em:

```text
src/main/resources/data/profissionais.json
```

Cada registro possui:

```json
{
  "id": 1,
  "nome": "Dr. Carlos Silva",
  "especialidade": "Cardiologia",
  "disponivel": true
}
```

Não há persistência em banco de dados. Para alterar os profissionais, basta
editar o arquivo JSON e reiniciar a aplicação.

## Como executar

Requisitos:

- Java 25
- Maven

Execute o script padrão:

```bash
chmod +x ./run/run.sh
run/run.sh
```

O script compila o projeto, executa os testes, gera o JAR executável e inicia a
API na porta `8080`.

Para parar a aplicação, pressione `Ctrl+C` no terminal onde ela está rodando.

## Endpoints da API

| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/` | Verifica se a API está online e mostra os principais links. |
| GET | `/api/profissionais` | Lista ou filtra profissionais. |
| GET | `/api/profissionais/todos` | Lista explicitamente todos os profissionais, sem filtros. |
| GET | `/api/especialidades` | Lista as especialidades disponíveis sem repetição. |
| GET | `/api-docs` | Abre o Swagger UI para testar a API. |
| GET | `/swagger` | Alias do Swagger UI. |
| GET | `/openapi.json` | Retorna a especificação OpenAPI em JSON. |

### Exemplos de consultas

Listar todos sem filtros:

```http
GET http://localhost:8080/api/profissionais/todos
```

Buscar pelo nome, ignorando maiúsculas e minúsculas:

```http
GET http://localhost:8080/api/profissionais?nome=Carlos
```

Filtrar por especialidade:

```http
GET http://localhost:8080/api/profissionais?especialidade=Cardiologia
```

Combinar nome e especialidade:

```http
GET http://localhost:8080/api/profissionais?nome=Carlos&especialidade=Cardiologia
```

Listar especialidades:

```http
GET http://localhost:8080/api/especialidades
```

## Exemplo de resposta

```json
[
  {
    "id": 1,
    "nome": "Dr. Carlos Silva",
    "especialidade": "Cardiologia",
    "disponivel": true
  }
]
```

Quando uma busca com filtro não encontra resultados, a API retorna HTTP `404`:

```json
{
  "mensagem": "Nenhum profissional encontrado."
}
```

Para uma especialidade inexistente, a mensagem é:

```json
{
  "mensagem": "Nenhum profissional encontrado para a especialidade informada."
}
```

## Swagger

Com a API em execução, abra:

```text
http://localhost:8080/api-docs
```

O Swagger permite visualizar os endpoints, informar parâmetros de consulta e
executar as requisições usando o botão `Try it out`.

## Arquitetura

O fluxo de uma requisição é:

```text
Cliente HTTP ou Swagger
          ↓
HttpServer
          ↓
Controller
          ↓
Service
          ↓
Repository
          ↓
profissionais.json
```

## Testes

Execute os testes com:

```bash
mvn test
```

O projeto possui testes para:

- Listagem completa.
- Busca por nome.
- Filtro por especialidade.
- Filtros combinados.
- Listagem sem duplicação de especialidades.
- Busca sem resultados.

## Decisões do projeto

O projeto não utiliza Spring Boot, banco de dados, Docker, autenticação ou
frontend separado. Essa escolha mantém a aplicação pequena e alinhada ao
objetivo de consultar profissionais armazenados em um JSON local.