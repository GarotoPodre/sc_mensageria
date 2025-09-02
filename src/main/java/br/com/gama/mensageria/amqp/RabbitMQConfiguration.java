package br.com.gama.mensageria.amqp;


import br.com.gama.mensageria.service.MessageConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;




/**
 * Classe de configuração para declarar infraestrutura principal do RabbitMQ (filas, exchanges, bindings)
 * necessárias à aplicação.
 * O Spring detectará os métodos @Bean e criar esses componentes no RabbitMQ no momento da inicialização.
 * Como Funciona:
 * 1.deadLetterExchange(): E criada uma nova FanoutExchange. Qualquer menssagem enviada para cá será reenviada (broadcast)para todas as filas
 * ligadas a ela.
 * 2.filaMensagensGeral(): A parte crucial esta no uso do QueueBuilder,  em .withArgument("x-dead-letter-exchange", ...)
 * que diz ao RabbitMQ: "Se uma mensagem nessa fila é rejeitada, envie para essa exchange especifica."
 * 3.deadLetterQueue(): Fila duravel que armazenara a mensagem que falhou.
 * 4.deadLetterBinding(): Faz a conexao da exchange (.dlx) para a queue (.dlq), fechando o circuito.
 */
@Configuration
public class RabbitMQConfiguration {

    public static final String EXCHANGE_NAME = "mensagens.exchange";
    public static final String ROUTING_KEY_GERAL="room.geral";

    //>>>>>>>>>>>>BEANS DA EXCHANGE PRINCIPAL<<<<<<<<<<<<<<<<<
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public TopicExchange topicExchange(){
        //return new TopicExchange(MensageriaService.EXCHANGE_MENSAGENS);
        return new TopicExchange(EXCHANGE_NAME);
    }

    //>>>>>>>>>>>>BEANS DA FILA PRINCIPAL<<<<<<<<<<<<<<<<<
    @Bean
    public Queue filaMensagensGeral(){
        return QueueBuilder.durable(MessageConsumer.FILA_MENSAGENS_GERAL)
                .withArgument("x-dead-letter-exchange", deadLetterExchange().getName())
                .build();
    }
    @Bean
    public Binding bindingMensagensGeral(Queue filaMensagensGeral, TopicExchange topicExchange){
        return BindingBuilder.bind(filaMensagensGeral).to(topicExchange).with(ROUTING_KEY_GERAL);
    }

    //>>>>>>>>>>>>BEANS DE RESILIENCIA (DEAD LETTER)<<<<<<<<<<<<<<<<<
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
     */
    @Bean
    public Queue deadLetterQueue(){
        return new Queue(MessageConsumer.FILA_MENSAGENS_GERAL+".dlq", true);
    }

    /**
     * Cria ligação entre DLX e DLQ
     */
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange deadLetterExchange){
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }

    /**
     * Configura o conversor de mensagens para usar o formato JSON (via Jackson)
     * @return O conversor de mensagens configurado
     */
    @Bean
    public MessageConverter jsonMessageConverter(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

}
