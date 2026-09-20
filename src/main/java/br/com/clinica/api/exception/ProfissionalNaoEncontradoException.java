package br.com.clinica.api.exception;

public class ProfissionalNaoEncontradoException extends RuntimeException {

    public ProfissionalNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}