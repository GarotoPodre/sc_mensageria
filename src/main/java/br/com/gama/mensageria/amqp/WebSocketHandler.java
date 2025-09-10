package br.com.gama.mensageria.amqp;

import br.com.gama.mensageria.dominio.Mensagem;
import br.com.gama.mensageria.service.DynamicListenerManager;
import br.com.gama.mensageria.service.MessagePublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Log4j2
public class WebSocketHandler implements org.springframework.web.reactive.socket.WebSocketHandler {

    //private final Sinks.Many<Mensagem> messageSink = Sinks.many().multicast().onBackpressureBuffer();
    //private final Flux<String> messageFlux;

    private final ObjectMapper objectMapper;
    private final DynamicListenerManager listenerManager;
    private final MessagePublisher messagePublisher;

    // Mapeia um nome de sala para um conjunto de sessões inscritas.
    private final ConcurrentMap<String, Set<WebSocketSession>> roomSubscriptions = new ConcurrentHashMap<>();
    // Mapeia uma sessão a um conjunto de salas para limpeza eficiente.
    private final ConcurrentMap<WebSocketSession, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();


    public WebSocketHandler(ObjectMapper objectMapper,
                            DynamicListenerManager listenerManager,
                            MessagePublisher messagePublisher) {

        this.objectMapper = objectMapper;
        this.listenerManager = listenerManager;
        this.messagePublisher = messagePublisher;

    }

    /**
     * Envia uma mensagem apenas para os clientes WebSocket inscritos em uma sala específica.
     * @param roomName O nome da sala de destino.
     * @param message A mensagem a ser enviada.
     */
    public void enviarMensagemParaSala(String roomName, Mensagem message){
        log.info("Enviando mensagem para a sala '{}': {}", roomName, message);
        Set<WebSocketSession> subscribers = roomSubscriptions.get(roomName);
        if(subscribers==null || subscribers.isEmpty()){
            log.debug("Nenhum cliente inscrito na sala '{}'. A mensagem não será enviada.", roomName);
            return;
        }

        log.info("Enviando mensagem para {} clientes na sala '{}'", subscribers.size(), roomName);
        String jsonMessage;
        try{
            jsonMessage = objectMapper.writeValueAsString(message);
        }catch (JsonProcessingException e){
            log.error("Falha ao serializar mensagem para a sala '{}'", roomName, e);
            return;
        }

        Flux.fromIterable(subscribers)
                .filter(WebSocketSession::isOpen)//filtra apenas as sessões abertas
                //cria e envia a mensagem da forma reativa correta para casa sessão
                .flatMap(session -> session.send(Mono.just(session.textMessage(jsonMessage))))
                .doOnError(e -> log.error("Erro ao enviar mensagem para a sala {}", roomName,e))
                .subscribe();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("Nova conexão WebSocket estabelecida: {}", session.getId());
        sessionSubscriptions.put(session, ConcurrentHashMap.newKeySet());

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(jsonPayLoad -> routeIncomingMessage(session, jsonPayLoad))
                .doOnError(e -> log.error("Erro no fluxo de entrada do WebSocket para a sessão {}",session.getId(),e))
                .doFinally(signalType -> {
                    log.info("Conexão WebSocket {} terminando ({}), limpando inscrições.", session.getId(), signalType);
                    cleanupSubscriptions(session);

                })
                .then();
    }

    private Mono<Void> routeIncomingMessage(WebSocketSession session, String jsonPayload) {
        return Mono.fromRunnable(() ->{
            try {
                log.info("Mensagem recebida via WebSocket: {}", jsonPayload);
                JsonNode jsonNode = objectMapper.readTree(jsonPayload);
                //O uso do path() é mais seguro que .get(), pois lança NullPointerException se o campo não existir
                String type = jsonNode.path("type").asText();

                switch (type){
                    case "create_room":
                        String roomToCreate = jsonNode.path("roomName").asText();
                        if(roomToCreate !=null && !roomToCreate.isEmpty()){
                            log.info("Processando comando 'create_room' para a sala: {}", roomToCreate);
                            listenerManager.createListenerForRoom(roomToCreate);
                        }
                        break;
                    case "chat_message":
                        String sender = jsonNode.path("sender").asText();
                        String content = jsonNode.path("content").asText();
                        String roomName = jsonNode.path("roomName").asText();

                        if (roomName != null && !roomName.isEmpty() && sender != null && !sender.isEmpty()){
                            Mensagem chatMessage = new Mensagem(sender, content);
                            messagePublisher.publishMessageToRoom(roomName, chatMessage);
                        }
                        break;
                    case "subscribe":
                        String roomNameToSubscribe = jsonNode.path("roomName").asText();
                        if(roomNameToSubscribe!=null && !roomNameToSubscribe.isEmpty()){
                            subscribe(session, roomNameToSubscribe);
                        }
                        break;
                    case "unsubscribe":
                        String roomToUnsubscribe = jsonNode.path("roomName").asText();
                        if(roomToUnsubscribe!= null && !roomToUnsubscribe.isEmpty()){
                            unsubscribe(session, roomToUnsubscribe);
                    }
                    break;

                    default:
                        log.warn("Tipo de mensagem desconhecida: {}", type);
                        break;
                }
            }catch (Exception e){
                log.error("Falha ao processar mensagem WebSocket: {}", jsonPayload,e);
            }
        });
    }
    private void subscribe(WebSocketSession session, String roomName){
        roomSubscriptions.computeIfAbsent(roomName, key -> ConcurrentHashMap.newKeySet())
                .add(session);
        sessionSubscriptions.get(session).add(roomName);
        log.info("Sessão {} inscrita na sala {}", session.getId(), roomName);
    }

    private void unsubscribe(WebSocketSession session, String roomName){
        Set<WebSocketSession> subscribers = roomSubscriptions.get(roomName);
        if(subscribers !=null){
            subscribers.remove(session);
        }
        sessionSubscriptions.get(session).remove(roomName);
        log.info("Sessão {} cancelou a inscrição da sala {}", session.getId(),roomName);
    }

    private void cleanupSubscriptions(WebSocketSession session){
        Set<String> subscribedRooms = sessionSubscriptions.remove(session);
        if(subscribedRooms !=null){
            subscribedRooms.forEach(roomName -> {
                Set<WebSocketSession> room = roomSubscriptions.get(roomName);
                if(room !=null)  room.remove(session);
            });
        }
    }

}
