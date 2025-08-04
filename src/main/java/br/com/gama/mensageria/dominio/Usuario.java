package br.com.gama.mensageria.dominio;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Usuario {
    private final String nome;

    public Usuario(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do usuário não pode ser nulo ou em branco.");
        }
        this.nome = nome;
    }

}
