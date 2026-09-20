package br.com.clinica.api.service;

import br.com.clinica.api.exception.ProfissionalNaoEncontradoException;
import br.com.clinica.api.model.Profissional;
import br.com.clinica.api.repository.ProfissionalRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;

    public ProfissionalService(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    public List<Profissional> buscarTodos() {
        return profissionalRepository.buscarTodos();
    }

    public List<Profissional> buscarProfissionais(String nome, String especialidade) {
        Stream<Profissional> profissionais = profissionalRepository.buscarTodos().stream();

        if (temTexto(nome)) {
            String nomeNormalizado = nome.trim().toLowerCase(Locale.ROOT);
            profissionais = profissionais.filter(profissional ->
                    profissional.getNome().toLowerCase(Locale.ROOT).contains(nomeNormalizado));
        }

        if (temTexto(especialidade)) {
            String especialidadeNormalizada = especialidade.trim().toLowerCase(Locale.ROOT);
            profissionais = profissionais.filter(profissional ->
                    profissional.getEspecialidade().toLowerCase(Locale.ROOT).equals(especialidadeNormalizada));
        }

        List<Profissional> resultado = profissionais.toList();
        if (resultado.isEmpty() && (temTexto(nome) || temTexto(especialidade))) {
            throw new ProfissionalNaoEncontradoException(criarMensagemNaoEncontrado(nome, especialidade));
        }

        return resultado;
    }

    public List<String> listarEspecialidades() {
        return profissionalRepository.buscarTodos().stream()
                .map(Profissional::getEspecialidade)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    private String criarMensagemNaoEncontrado(String nome, String especialidade) {
        if (temTexto(especialidade) && !temTexto(nome)) {
            return "Nenhum profissional encontrado para a especialidade informada.";
        }
        if (temTexto(nome) && !temTexto(especialidade)) {
            return "Nenhum profissional encontrado.";
        }
        return "Nenhum profissional encontrado para os filtros informados.";
    }
}