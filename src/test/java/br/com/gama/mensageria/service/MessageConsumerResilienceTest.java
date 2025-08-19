package br.com.gama.mensageria.service;


import br.com.gama.mensageria.amqp.WebSocketHandler;

import br.com.gama.mensageria.dominio.Mensagem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Teste de integração.
 * Simula a falha no consumidor e verifica se a mensagem é corretamente roteada para a DLQ (Dead Letter Queue).
 * - Usa @MockBean para substituir o WebSocketHandler real, que simula uma falha para testar o comportamento da DLQ
 * Usa Testcontainer, um conjunto de classes para rodar Docker conteineres diretamente dos testes java.
 */
@Testcontainers
@SpringBootTest
public class MessageConsumerResilienceTest {
    @Container
    static RabbitMQContainer rabbitmqContainer = new RabbitMQContainer("rabbitmq:3.13.0-management");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry){
        registry.add("spring.rabbitmq.host", rabbitmqContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitmqContainer::getAmqpPort);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private WebSocketHandler webSocketHandler;

    private static final String DLQ_NAME = MessageConsumer.FILA_MENSAGENS_GERAL+".dlq";

    @Test
    @DisplayName("Deve enviar mensagem para a DLQ quando processamento falhar")
    void deveEnviarMsgParaDLQEmCasoDeFalha(){
        doThrow(new RuntimeException("Simulando falha no Websocket")).when(webSocketHandler).enviarMensagemParaTodos(any(Mensagem.class));
        Mensagem mensagemComFalha = new Mensagem("TestSender", "Esta mensagem vai falhar");
        rabbitTemplate.convertAndSend(MensageriaService.EXCHANGE_MENSAGENS,MensageriaService.ROUTING_KEY_MENSAGEM, mensagemComFalha);
        Mensagem mensagemDaDLQ = (Mensagem) rabbitTemplate.receiveAndConvert(DLQ_NAME, TimeUnit.SECONDS.toMillis(5));
        assertNotNull(mensagemDaDLQ, "A mensagem não chegou na DLQ a tempo");
        assertEquals(mensagemComFalha, mensagemDaDLQ);


    }
}
