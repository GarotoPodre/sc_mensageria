package br.com.gama.mensageria.service;

import br.com.gama.mensageria.dominio.Mensagem;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MensageriaService {
    public static final String EXCHANGE_MENSAGENS ="mensagens.exchange";
    public static final String ROUTING_KEY_MENSAGEM ="mensagens.nova";

    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;

    public void sendMessage(String sender, String content) {

        Mensagem mensagem = new Mensagem(sender, content);
        rabbitTemplate.convertAndSend(EXCHANGE_MENSAGENS, ROUTING_KEY_MENSAGEM, mensagem);
    }

    public void criarInfraestruturaParaUsuario(String nomeUsuario) {
        String nomeFila = "fila."+nomeUsuario;
        Queue fila = new Queue(nomeFila,true);//duravel

        //Criando binding entre a fila e a exchange principal
        //usando o nome do usuario como routing key
        Binding binding=new Binding(nomeFila, Binding.DestinationType.QUEUE, EXCHANGE_MENSAGENS, nomeUsuario, null);

        //Declara a fila e o binding no servidor RabbitMQ
        amqpAdmin.declareQueue(fila);
        amqpAdmin.declareBinding(binding);
    }

}
