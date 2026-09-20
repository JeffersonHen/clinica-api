package br.com.clinica.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.clinica.api.exception.ProfissionalNaoEncontradoException;
import br.com.clinica.api.model.Profissional;
import br.com.clinica.api.repository.ProfissionalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProfissionalServiceTest {

    private final ProfissionalService profissionalService =
            new ProfissionalService(new ProfissionalRepository(new ObjectMapper()));

    @Test
    void deveRetornarTodosOsProfissionais() {
        List<Profissional> profissionais = profissionalService.buscarProfissionais(null, null);

        assertEquals(10, profissionais.size());
    }

    @Test
    void deveBuscarTodosSemAplicarFiltros() {
        List<Profissional> profissionais = profissionalService.buscarTodos();

        assertEquals(10, profissionais.size());
        assertEquals("Dr. Carlos Silva", profissionais.getFirst().getNome());
    }

    @Test
    void deveBuscarPorNomeSemDiferenciarMaiusculas() {
        List<Profissional> profissionais = profissionalService.buscarProfissionais("cArLoS", null);

        assertEquals(1, profissionais.size());
        assertEquals("Dr. Carlos Silva", profissionais.getFirst().getNome());
    }

    @Test
    void deveFiltrarPorEspecialidade() {
        List<Profissional> profissionais = profissionalService.buscarProfissionais(null, "cardiologia");

        assertEquals(2, profissionais.size());
        assertEquals("Cardiologia", profissionais.getFirst().getEspecialidade());
    }

    @Test
    void deveListarEspecialidadesSemRepeticao() {
        assertEquals(List.of("Cardiologia", "Dermatologia", "Nutrição", "Ortopedia", "Pediatria"),
            profissionalService.listarEspecialidades());
    }

    @Test
    void deveInformarQuandoNenhumProfissionalForEncontrado() {
        ProfissionalNaoEncontradoException exception = assertThrows(
            ProfissionalNaoEncontradoException.class,
            () -> profissionalService.buscarProfissionais("ZZZZ", null));

        assertEquals("Nenhum profissional encontrado.", exception.getMessage());
    }
}