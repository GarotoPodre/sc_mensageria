package br.com.gama.mensageria.amqp;

import br.com.gama.mensageria.service.MensageriaService;
import br.com.gama.mensageria.service.MessageConsumer;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;




/**
 * Classe de configuração para declarar infraestrutura principal do RabbitMQ (filas, exchanges, bindings)
 * necessárias à aplicação.
 * O Spring detectará os métodos @Bean e criar esses componentes no RabbitMQ no momento da inicialização.
 * How It Works
 * 1.deadLetterExchange(): E criada uma nova FanoutExchange. Qualquer menssagem enviada para cá será reenviada (broadcast)para todas as filas
 * ligadas a ela.
 * 2.filaMensagensGeral(): A parte crucial esta no uso do QueueBuilder,  em .withArgument("x-dead-letter-exchange", ...)
 * que diz ao RabbitMQ: "Se uma mensagem nessa fila é rejeitada, envie para essa exchange especifica."
 * 3.deadLetterQueue(): Fila duravel que armazenara a mensagem que falhou.
 * 4.deadLetterBinding(): Faz a conexao da exchange (.dlx) para a queue (.dlq), fechando o circuito.
 */
@Configuration
public class RabbitMQConfiguration {

    @Bean
    public Queue filaMensagensGeral(){
        return QueueBuilder.durable(MessageConsumer.FILA_MENSAGENS_GERAL)
                .withArgument("x-dead-letter-exchange", deadLetterExchange().getName())
                .build();
    }

    /**
     * Uma DLX recebe mensagens que não puderam ser processadas.
     * O FanoutExchange é usado para que qualquer fila ligada a ela recebea a mensagem
     *, o que é util para o monitoramento ou reprocessamento.
     */
    @Bean
    public FanoutExchange deadLetterExchange(){
        return new FanoutExchange(MessageConsumer.FILA_MENSAGENS_GERAL+".dlx");
    }

    /**
     * Uma DLQ armazena as mensagens que falharam para análise posterior.
     * @return
     */
    @Bean
    public Queue deadLetterQueue(){
        return new Queue(MessageConsumer.FILA_MENSAGENS_GERAL+".dlq");
    }

    /**
     * Cria ligação entre DLX e DLQ
     */
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange deadLetterExchange){
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }
    //public Queue filaMensagensGeral(){
    //    return new Queue(MessageConsumer.FILA_MENSAGENS_GERAL, true);//fila duravel
    //}

    @Bean
    public TopicExchange mensagensExchange(){
        return new TopicExchange(MensageriaService.EXCHANGE_MENSAGENS);
    }

    @Bean
    public Binding bindingMensagensGeral(Queue filaMensagensGeral, TopicExchange mensagensExchange){
        return BindingBuilder.bind(filaMensagensGeral).to(mensagensExchange).with(MensageriaService.ROUTING_KEY_MENSAGEM);
    }

    /**
     * Configura o conversor de mensagens para usar o formato JSON (via Jackson)
     * @return O conversor de mensagens configurado
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
