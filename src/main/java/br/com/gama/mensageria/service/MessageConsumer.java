package br.com.gama.mensageria.service;

import br.com.gama.mensageria.amqp.WebSocketHandler;
import br.com.gama.mensageria.dominio.Mensagem;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class MessageConsumer {
    public static final String FILA_GENERICA_MENSAGENS = "mensagens.generica";

    private final WebSocketHandler webSocketHandler;

    @RabbitListener(queues = FILA_GENERICA_MENSAGENS)
    public void consumirMensagem(Mensagem mensagem) {

        log.info("Mensagem recebida: {}", mensagem);
        webSocketHandler.enviarMensagemParaTodos(mensagem);
    }

}
