package br.com.gama.mensageria.amqp;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

/**
 *  Classe responsável por mapear a url /ws/mensagens para o WebSocketHandler reativo
* */
@Configuration
@RequiredArgsConstructor
public class WebSocketConfiguration {
    private final WebSocketHandler webSocketHandler;

    @Bean
    public HandlerMapping handlerMapping(){
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping(Map.of("/ws/mensagens", webSocketHandler));
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return mapping;
    }
}
