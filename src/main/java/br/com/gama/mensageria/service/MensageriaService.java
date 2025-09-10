package br.com.gama.mensageria.service;

import br.com.gama.mensageria.dominio.Mensagem;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class MensageriaService {
    public static final String EXCHANGE_MENSAGENS ="mensagens.exchange";
    public static final String ROUTING_KEY_MENSAGEM ="mensagens.nova";

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private final TopicExchange topicExchange;
    private final DynamicListenerManager listenerManager;


    public void sendMessage(String roomName, String sender, String content) {

        Mensagem mensagem = new Mensagem(sender, content);
        String routingKey = roomName != null ? roomName:ROUTING_KEY_MENSAGEM;

        log.info("Enviando mensagem para a sala '{}' com a chave de roteamento '{}': {}", roomName, routingKey, mensagem);

        rabbitTemplate.convertAndSend(EXCHANGE_MENSAGENS, routingKey, mensagem);
    }

    public void criarSala(String roomName) {
        String queueName = "room."+roomName;
        String routingkey = roomName;//a chave de roteamento é o próprio nome da sala

        Queue queue = new Queue(queueName, true);
        Binding binding = BindingBuilder.bind(queue).to(topicExchange).with(routingkey);

        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
        //log.info("Sala '{}' criada com a fila '{}' e binding para a chave '{}'", roomName, queueName, routingkey);

    }
}
