package br.com.gama.mensageria.dominio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    @DisplayName("Verificando se o construtor cria o usuário com nome válido")
    void shouldCreateUserWithValidName(){
        //Arrange
        String nomeEsperado="José";

        //Act:
        Usuario usuario = new Usuario(nomeEsperado);

        //Assert: verifica se o objeto foi criado corretamente
        assertNotNull(usuario);
        assertEquals(nomeEsperado, usuario.getNome());

    }

    @ParameterizedTest
    @NullAndEmptySource//fornece valor nulo para test
    @ValueSource(strings = {" ", "\t", "\n"})//fornece strings com espaços em branco
    @DisplayName("Deve lançar exceção se o nome for nulo ou vazio ou em branco")
    void shouldThrowExceptionWhenNameIsInvalid(String nomeInvalido){
        //Act e assert: Verifica se a exceção esperada é lançada
        assertThrows(IllegalArgumentException.class, () -> new Usuario(nomeInvalido));
    }
}
