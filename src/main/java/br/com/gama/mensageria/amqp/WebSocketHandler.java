package br.com.gama.mensageria.amqp;

import br.com.gama.mensageria.dominio.Mensagem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@Log4j2
public class WebSocketHandler implements org.springframework.web.reactive.socket.WebSocketHandler {

    private final Sinks.Many<Mensagem> messageSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Flux<String> messageFlux;

    public WebSocketHandler(ObjectMapper objectMapper) {
        this.messageFlux = messageSink.asFlux().map(message -> {
            try {
                return objectMapper.writeValueAsString(message);
            } catch (JsonProcessingException e) {
                log.error("Erro ao serializar mensagem para JSON", e);
                return "{}";
            }
        }).share(); // .share() para que múltiplos clientes possam se inscrever
    }

    public void enviarMensagemParaTodos(Mensagem message) {
        messageSink.tryEmitNext(message);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("Nova conexão WebSocket estabelecida: {}", session.getId());
        return session.send(messageFlux.map(session::textMessage));
    }
}
