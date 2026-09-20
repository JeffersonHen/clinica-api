package br.com.clinica.api.repository;

import br.com.clinica.api.model.Profissional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ProfissionalRepository {

    private static final String CAMINHO_DADOS = "data/profissionais.json";
    private final ObjectMapper objectMapper;

    public ProfissionalRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Profissional> buscarTodos() {
        try (InputStream arquivo = ProfissionalRepository.class.getClassLoader()
                .getResourceAsStream(CAMINHO_DADOS)) {
            if (arquivo == null) {
                throw new IllegalStateException("Arquivo de profissionais nao encontrado.");
            }
            return objectMapper.readValue(arquivo, new TypeReference<List<Profissional>>() { });
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel carregar os profissionais.", exception);
        }
    }
}