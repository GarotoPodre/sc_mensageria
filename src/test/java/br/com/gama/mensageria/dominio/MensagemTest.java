package br.com.gama.mensageria.dominio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MensagemTest {
    @Test
    @DisplayName("Verificando se o construtor recebeu todos os parâmetros e estes estão corretos")
        void shouldCreateMessageWithSenderAndContent() { // Seguindo convenção
            // Arrange: Configura os dados do teste e expectativas.
            String remetenteEsperado = "fulano";
            String conteudoEsperado = "Olá mundo";

            // Act: Executa o codigo que se quer testar.
            Mensagem msg = new Mensagem(remetenteEsperado, conteudoEsperado);

            // Assert: Verifica que o conteudo recebido eh o esperado.
            assertNotNull(msg, "O objeto Mensagem não deveria ser nulo.");
            assertEquals(remetenteEsperado, msg.getSender(), "O remetente da mensagem não corresponde ao esperado.");
            assertEquals(conteudoEsperado, msg.getContent(), "O conteúdo da mensagem não corresponde ao esperado.");
    }
    @Test
    @DisplayName("Duas mensagens com o mesmo remetente e conteúdo devem ser consideradas iguais")
    void shouldBeEqualWhenSenderAndContentAreTheSame() {
        // Arrange: Cria dois objetos separados, mas com mesmo remetente e conteúdo
        Mensagem msg1 = new Mensagem("user1", "test message");
        Mensagem msg2 = new Mensagem("user1", "test message");

        // Cria um objeto diferente para checar desigualdade
        Mensagem msg3 = new Mensagem("user2", "test message");

        // Assert: Check for value equality, not reference equality
        assertEquals(msg1, msg2, "Mensagens com o mesmo estado devem ser iguais.");
        assertEquals(msg1.hashCode(), msg2.hashCode(), "Hashcodes de mensagens iguais devem ser os mesmos.");
        assertNotEquals(msg1, msg3, "Mensagens com remetentes diferentes não devem ser iguais.");
    }
}
