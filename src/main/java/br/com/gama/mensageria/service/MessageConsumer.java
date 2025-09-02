package br.com.gama.mensageria.service;

import br.com.gama.mensageria.amqp.WebSocketHandler;
import br.com.gama.mensageria.dominio.Mensagem;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class MessageConsumer {
    public static final String FILA_MENSAGENS_GERAL = "mensagens.geral";
    private final WebSocketHandler webSocketHandler;
    /**
     * Construtor que injeta o WebSocketHandler.
     * A anotação @Lazy é crucial, pois quebra uma possível dependência circular,
     * instruindo o Spring a injetar um proxy do WebSocketHandler inicialmente,
     * e o bean real será resolvido apenas quando for usado pela primeira vez.
     */
    public MessageConsumer(@Lazy WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }


    @RabbitListener(queues = FILA_MENSAGENS_GERAL, id="geral-listener")
    public void consumirMensagemGeral(Mensagem mensagem) {
        final String GERAL_ROOM_NAME = "geral";

        log.info("Mensagem recebida na fila geral: {}", mensagem);
        webSocketHandler.enviarMensagemParaSala(GERAL_ROOM_NAME,mensagem);
    }

    /**
     * Consome uma mensagem da sala dinâmica específica.
     * Este método é chamado pelo listener criado dinamicamente.
     * @param roomName O nome da sala da qual a mensagem se originou
     * @param mensagem O objeto da mensagem
     */
    public void consumirMensagemDeSala(String roomName, Mensagem mensagem){
        log.info("Mensagem recebida para a sala '{}': {}", roomName, mensagem);
        webSocketHandler.enviarMensagemParaSala(roomName, mensagem);
    }

}
