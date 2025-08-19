package br.com.gama.mensageria.service;

import br.com.gama.mensageria.dominio.Mensagem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static br.com.gama.mensageria.service.MensageriaService.EXCHANGE_MENSAGENS;
import static br.com.gama.mensageria.service.MensageriaService.ROUTING_KEY_MENSAGEM;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MensageriaServiceTest {

    /**
     * @Mock cria um RabbitTemplate 'fake'.
     * Isso elimina a necessidade de se conectar a um RabbitMQ Server
     */
    @Mock
    private RabbitTemplate rabbitTemplate;

    /**
     * @InjectMocks cria um MensageriaService e injeta o rabbitTemplate fake nele
     */
    @InjectMocks
    private MensageriaService mensageriaService;

    /**
     * Verifica se a mensagem foi enviada corretamente, da forma que foi criada
     */
    @Test
    @DisplayName("Deve criar uma mensagem e envia-la para o RabbitMQ via RabbitTemplate")
    void shouldSendMessageToRabbitMQ() {
        //Arrange
        String sender = "sender";
        String content = "Oi, Mocks!";
        String roomName="sala_teste";

        //Act
        mensageriaService.sendMessage(roomName,sender, content);

        //Assert: verifica se o nosso serviço chamou o metodo correto no mock
        ArgumentCaptor<Mensagem> mensagemCaptor = ArgumentCaptor.forClass(Mensagem.class);
        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE_MENSAGENS), eq(ROUTING_KEY_MENSAGEM), mensagemCaptor.capture());

        //verifica se os valores foram corretamente enviados
        Mensagem mensagemEnviada = mensagemCaptor.getValue();
        assertEquals(sender, mensagemEnviada.getSender());
        assertEquals(content, mensagemEnviada.getContent());

    }
}
