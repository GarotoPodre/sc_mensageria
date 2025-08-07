package br.com.gama.mensageria.service;

import br.com.gama.mensageria.dominio.Mensagem;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MensageriaService {
    public static final String EXCHANGE_MENSAGENS ="mensagens.exchange";
    public static final String ROUTING_KEY_MENSAGEM ="mensagens.nova";

    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(String sender, String content) {

        Mensagem mensagem = new Mensagem(sender, content);
        rabbitTemplate.convertAndSend(EXCHANGE_MENSAGENS, ROUTING_KEY_MENSAGEM, mensagem);
    }
}
