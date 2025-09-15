package br.com.gama.mensageria.config;

import br.com.gama.mensageria.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class WebSocketAuthAdapterConfig {

    private final JwtProvider jwtProvider;

    /**
     * Cria um WebSocketHandlerAdapter customizado que garante que nosso serviço de handshake
     * de segurança seja usado, em vez do padrão do Spring Boot.
     */
    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter(createWebSocketService());
    }

    private WebSocketService createWebSocketService() {
        HandshakeWebSocketService service = new HandshakeWebSocketService() {
            @Override
            public Mono<Void> handleRequest(ServerWebExchange exchange, WebSocketHandler handler) {
                String token = extractToken(exchange);
                log.debug("Tentando handshake WebSocket com token: {}", token != null ? "presente" : "ausente");

                if (token == null) {
                    log.warn("Handshake WebSocket rejeitado: Nenhum token fornecido.");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                return Mono.just(token)
                        .filter(jwtProvider::validateToken)
                        .flatMap(jwtProvider::getAuthentication)
                        .flatMap(authentication -> {
                            log.info("Handshake WebSocket bem-sucedido para o usuário: {}", authentication.getName());
                            return super.handleRequest(exchange.mutate().principal(Mono.just(authentication)).build(), handler);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("Handshake WebSocket rejeitado: Token inválido.");
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }));
            }
        };
        service.setSessionAttributePredicate(s -> true);
        return service;
    }

    private String extractToken(ServerWebExchange exchange) {
        return exchange.getRequest().getQueryParams().getFirst("token");
    }


}
