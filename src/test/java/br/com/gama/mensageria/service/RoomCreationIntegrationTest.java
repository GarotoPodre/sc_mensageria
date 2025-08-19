package br.com.gama.mensageria.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Teste de integracao para criacao dinamica de salas (filas)
 * Verifica se a chamada de servico correta resulta na criacao da infraestrutura
 * necessaria no broker RabbitMQ
 */

@Testcontainers
@SpringBootTest
public class RoomCreationIntegrationTest {

    @Container
    static RabbitMQContainer rabbitmqContainer = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry){
        registry.add("spring.rabbitmq.host", rabbitmqContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitmqContainer::getAmqpPort);
    }

    @Autowired
    private MensageriaService mensageriaService;

    @Autowired
    private RabbitAdmin rabbitAdmin; //recurso do Spring para interagir com o broker

    @Test
    @DisplayName("Deve criar uma nova fila quando uma sala for criada")
    void deveCriarFilaParaNovaSala() {
        String roomName = "sala_teste";
        String expectedQueueName = "room." + roomName;

        mensageriaService.criarSala(roomName);
        Properties queueProperties = rabbitAdmin.getQueueProperties(expectedQueueName);
        assertNotNull(queueProperties, "A fila para a sala'"+roomName+ "'não foi criada");
        assertEquals(expectedQueueName, queueProperties.getProperty("QUEUE_NAME"));


    }


}
