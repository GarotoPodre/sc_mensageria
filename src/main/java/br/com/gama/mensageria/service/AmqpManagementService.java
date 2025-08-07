package br.com.gama.mensageria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmqpManagementService {

    public static final String EXCHANGE_MENSAGENS = "mensagens.exchange";
    private final AmqpAdmin amqpAdmin;

    public void criarInfraestruturaParaUsuario(String nomeUsuario) {
        String nomeFila = "fila." + nomeUsuario;
        Queue fila = new Queue(nomeFila, true);//duravel

        //Criando binding entre a fila e a exchange principal
        //usando o nome do usuario como routing key
        Binding binding = new Binding(nomeFila, Binding.DestinationType.QUEUE, EXCHANGE_MENSAGENS, nomeUsuario, null);

        //Declara a fila e o binding no servidor RabbitMQ
        amqpAdmin.declareQueue(fila);
        amqpAdmin.declareBinding(binding);
    }
}