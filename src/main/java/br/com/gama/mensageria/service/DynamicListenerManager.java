package br.com.gama.mensageria.service;



import br.com.gama.mensageria.dominio.Mensagem;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Essa classe age como uma fabrica e gerenciadora para RabbitMQ listeners.
 * Seu trabalho é crior, iniciar e parar message consumers para filas
 * que sao criadas apos a aplicacao já ter iniciado.
 *
 * E a classe chave para construcao de salas dinamicas.
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class DynamicListenerManager {

    private static final String QUEUE_PREFIX="room.";

    private final ConnectionFactory connectionFactory;
    private final MessageConverter messageConverter;
    private final MessageConsumer messageConsumer;
    private final RabbitAdmin rabbitAdmin;
    private final TopicExchange topicExchange;
    private final ConcurrentMap<String, SimpleMessageListenerContainer> runningContainers=new ConcurrentHashMap<>();

    /**
     * Esse é o método público principal. É chamado quando uma nova sala é criada.
     * Ela pega o roomName, constrói a fila com o nome correspondente (e.g., room.papo_furado),
     * e então cria e inicia um new listener container que consumirá mensagens somente da fila especifica.
     * Também  previne listeners duplicados de ser criados para a mesma fila.
     * @param roomName
     */
    public void createListenerForRoom(String roomName){
        String queueName = QUEUE_PREFIX+roomName;

        //computIfAbsent garante a criação e inserção atômica.
        runningContainers.computeIfAbsent(queueName, key ->{
           log.info("Criando a infraestrutura para a fila '{}'", key);
           //Etapa 1: Criar a fila e o binding administrativamente.
            createQueueAndBinding(key);

           SimpleMessageListenerContainer container=createContainer(key);
           container.start();
           log.info("Listener para a fila '{}' iniciado com sucesso", key);
           return container;
        });
    }

    /**
     * Remove um listener para uma sala específica
     * Deve ser chamado quando uma sala é deletada para liberar recursos.
     * @param roomName o nome da sala para remover o listener.
     */
    public void removeListenerForRoom(String roomName){
        String queueName = QUEUE_PREFIX+roomName;
        SimpleMessageListenerContainer container = runningContainers.remove(queueName);
        if(container!=null) {
            log.info("Parando e removendo listener para a fila '{}'", queueName);
            container.stop();
            log.info("Listener para a fila '{}' parado com sucesso", queueName);

            rabbitAdmin.deleteQueue(queueName);
            log.info("Fila '{}' removida com sucesso", queueName);
        }else{
            log.warn("Nenhum listener encontrado para a fila '{}' para ser removido", queueName);
        }
    }

    /**
     * Método ajudante privado que faz o trabalho em baixo-nível.
     * Constrói um SimpleMessageListenerContainer, que é o Spring object que atualmente conecta
     * ao RabbitMQ e escuta por mensagens. Configura o contêiner para:
     * 1.Escutar a queueName correta.
     * 2.Usar a class MessageConsumer existente para processar as mensagens.
     * 3.Chamar especificamente o método consumirMensagem sobre o consumer.
     * 4.Utiliza o correto JSON converter para retornar a message em um objeto Mensagem.
     * @param queueName
     * @return um SimpleMessageListenerContainer configurado mas não iniciado
     */
    private SimpleMessageListenerContainer createContainer(String queueName){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);

        //criado listener específico que sabe o nome da sala.
        container.setMessageListener(message -> {
            String roomName = queueName.substring(QUEUE_PREFIX.length());
            try{
                Mensagem mensagemObj = (Mensagem) messageConverter.fromMessage(message);
                messageConsumer.consumirMensagemDeSala(roomName, mensagemObj);
            }catch (Exception e){
                log.error("Falha ao processar mensagem para a sala {}", roomName, e);
            }
        });

        return container;

    }

    /**
     * Use o RabbitAdmin para declarar programaticamente uma nova fila e seu binding.
     * Isso garante que a infraestrutura exista no broker antes que o listener tente se conectar.
     * @param queueName o nome da nova fila
     */
    private void createQueueAndBinding(String queueName){
        //O nome da fila também serve como routing key para o binding
        Queue queue = new Queue(queueName, true, false, false);
        Binding binding = BindingBuilder.bind(queue)
                .to(topicExchange)
                .with(queueName);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
        log.info("Fila '{}' e seu binding para exchange '{}' foram declarados com sucesso", queueName, topicExchange.getName());
    }

    /**
     * Método de limpeza crucial.
     * A anotação @PreDestroy diz ao Spring para executar esse método automaticamente quando a aplicação é derrubada.
     * Para todos os dynamic listeners criados, garantindo uma saída limpa e prevenindo vazamento de recursos.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Desligando todos os listeners dinâmicos...");
        runningContainers.values().forEach(SimpleMessageListenerContainer::stop);
        runningContainers.clear();
    }


}
