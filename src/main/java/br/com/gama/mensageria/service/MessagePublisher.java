package br.com.gama.mensageria.service;

import br.com.gama.mensageria.amqp.RabbitMQConfiguration;
import br.com.gama.mensageria.dominio.Mensagem;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Serviço dedicado a publicar mensagens no RabbitMQ
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class MessagePublisher {

    private static final String ROOM_ROUTING_KEY_PREFIX = "room.";
    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica um mensagem de chat para uma sala específica.
     * O routing key garantirá que a mensagem vá para a fila da sala correta.
     *
     * @param roomName o nome da sala para a qual a mensagem será enviada.
     * @param mensagem o objeto da mensagem a ser enviada.
     */
    public void publishMessageToRoom(String roomName, Mensagem mensagem){
        String routingKey = ROOM_ROUTING_KEY_PREFIX+roomName;
        log.info("Publicando mensagem para a sala '{}' (routingKey: {}): {}", roomName,routingKey, mensagem);
        rabbitTemplate.convertAndSend(RabbitMQConfiguration.EXCHANGE_NAME, routingKey, mensagem);

    }

}
