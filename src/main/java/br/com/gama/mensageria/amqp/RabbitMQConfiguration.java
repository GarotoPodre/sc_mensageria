package br.com.gama.mensageria.amqp;

import br.com.gama.mensageria.service.MensageriaService;
import br.com.gama.mensageria.service.MessageConsumer;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;




/**
 * Classe de configuração para declarar infraestrutura principal do RabbitMQ (filas, exchanges, bindings)
 * necessárias à aplicação.
 * O Spring detectará os métodos @Bean e criar esses componentes no RabbitMQ no momento da inicialização.
 */
@Configuration
public class RabbitMQConfiguration {

    @Bean
    public Queue filaMensagensGeral(){
        return new Queue(MessageConsumer.FILA_MENSAGENS_GERAL, true);//fila duravel
    }

    @Bean
    public TopicExchange mensagemExchange(){
        return new TopicExchange(MensageriaService.EXCHANGE_MENSAGENS);
    }

    @Bean
    public Binding bindingMensagensGeral(Queue filaMensagensGeral, TopicExchange mensagemExchange){
        return BindingBuilder.bind(filaMensagensGeral).to(mensagemExchange).with(MensageriaService.ROUTING_KEY_MENSAGEM);
    }

}
