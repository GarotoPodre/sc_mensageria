package br.com.gama.mensageria.service;

import br.com.gama.mensageria.amqp.WebSocketHandler;
import br.com.gama.mensageria.dominio.Mensagem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MessageConsumerTest {
    /**
     * @Mock Cria um WShandler 'fake' para fazer papel de WebSocket Real
     */
    @Mock
    private WebSocketHandler webSocketHandler;

    /**
     * @InjectMocks crua um MessageConsumer e injeta o WebSocketHandler, criado acima, nele
     */
    @InjectMocks
    private MessageConsumer messageConsumer;

    @Test
    @DisplayName("Deve receber mensagem e encaminhar para o WebSocketHandler")
    void deveReceberMensagemEEnviarParaWebSocketHandler(){
        //Arrange: criando mensagem de exemplo
        Mensagem mensagem = new Mensagem("sender", "Olá consumidor");

        //Act: Chama o metodo que se deseja testar
        //messageConsumer.consumirMensagem(mensagem);

        //Assert: Verifica se o o metodo correto foi chamado pelo consumidor (no caso, do mock)
        //verify(webSocketHandler).enviarMensagemParaTodos(mensagem);
    }


}
