package br.com.gama.mensageria.dominio;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * os campos são finais para manter a imutabilidade do objeto. Isso torna tudo mais seguro.
 * O Lombok é inteligente o bastante para saber que timestamp não deve ser cobrado no construtor.
 * Por se tratar de uma classe que representa mensagens, duas mensagens com mesmo texto e remetente
 * podem ser consideradas iguais
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Mensagem implements Serializable {
    private String sender;
    private String content;
    private LocalDateTime timestamp;

    public Mensagem(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp=LocalDateTime.now();
    }



}
