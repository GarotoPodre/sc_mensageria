package br.com.gama.mensageria.amqp;

import br.com.gama.mensageria.service.MensageriaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *  Full end-to-end integration test.
 *  Esse teste iniciará completamente a aplicação, conectará a ela com um verdadeiro cliente  WebSocket,
 *  e verificará o fluxo da mensagem completamente do serviço ao cliente.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MensageriaService mensageriaService;

    @Autowired
    private ObjectMapper objectMapper;

    private BlockingQueue<String> mensagensRecebidas;
    private String webSocketUri;

    @BeforeEach
    public void setup() {
        mensagensRecebidas =new LinkedBlockingQueue<>();
        webSocketUri ="ws://localhost:" + port + "/ws/mensagens";
    }

    @Test
    @DisplayName("Deve enviar mensagem para o serviço e recebê-la via WebSocket")
    void deveReceberMensagemViaWebSocket() throws Exception{
        //Arrange
        CountDownLatch connectionLatch = new CountDownLatch(1);

        // Arrange: Conecta um cliente WebSocket que adiciona mensagens recebidas a uma fila
        WebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(
                new URI(webSocketUri),
                session -> {
                    connectionLatch.countDown();
                    return session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnNext(mensagensRecebidas::add)
                            .then();
                }
        ).subscribe(); // Inicia a conexão em background

        // Espera pela conexão pro até 2 segundos
        connectionLatch.await(2, TimeUnit.SECONDS);

        String sender = "TesteDeIntegracao";
        String content = "Salve, Reactive WebSocket!";

        // Act: Envia uma mensagem para o RabbitMQ através do nosso serviço
        mensageriaService.sendMessage(sender, content);

        // Assert: Verifica se o cliente WebSocket recebeu a mensagem
        String receivedMessageJson = mensagensRecebidas.poll(5, TimeUnit.SECONDS);
        assertNotNull(receivedMessageJson, "Não recebeu mensagem via WebSocket a tempo.");

        var receivedMessage = objectMapper.readTree(receivedMessageJson);
        assertEquals(sender, receivedMessage.get("sender").asText());
        assertEquals(content, receivedMessage.get("content").asText());
    }
}
