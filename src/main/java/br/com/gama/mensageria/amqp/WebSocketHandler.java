package br.com.gama.mensageria.amqp;

import br.com.gama.mensageria.dominio.Mensagem;
import org.springframework.stereotype.Component;

@Component
public class WebSocketHandler {
    public void enviarMensagemParaTodos(Mensagem message) {
        // Implementação para enviar mensagem para todos os clientes WebSocket
        // Por enquanto, apenas um stub
        //A logica real do WebSocket virá aqui mais tarde
        System.out.println("Enviando mensagem para todos os clientes WebSocket: " + message);
    }

}
