package br.com.gama.mensageria.amqp;

import br.com.gama.mensageria.service.MensageriaService;
import br.com.gama.mensageria.service.MessageConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Teste de integração
 * @SpringBootTest(class=...) inicia o contexto Spring, mas carrega somente a classe de configuração.
 * Essa abordagem torna mais rápida a inicializacao do que iniciar todo o aplicativo.
 */

@SpringBootTest(classes = RabbitMQConfiguration.class)
public class RabbitMQConfigurationTest {

    @Autowired
    private ApplicationContext context;//Contexto Spring iniciado para o teste

    @Test
    @DisplayName("Deve criar um bean para a fila geral de mensagens")
    void deveCriarBeanParaFilaGeralDeMensagens() {

        //Assert: verifica se um bean do tipo Queue com o nome correto existe no contexto
        Queue queue = context.getBean("filaMensagensGeral", Queue.class);
        assertNotNull(queue);
        assertEquals(MessageConsumer.FILA_MENSAGENS_GERAL,queue.getName());
    }

    @Test
    @DisplayName("Deve criar um bean para a exchange principal de mensagens")
    void deveCriarBeanParaExchangePrincipalDeMensagens() {
        Exchange exchange=context.getBean("mensagemExchange", Exchange.class);
        assertNotNull(exchange);
        assertEquals(MensageriaService.EXCHANGE_MENSAGENS,exchange.getName());
    }

    @Test
    @DisplayName("Deve criar um binding entre a fila geral e a exchange")
    void deveCriarBindingParaFilaGeral(){
        Binding binding = context.getBean("bindingMensagensGeral", Binding.class);
        assertNotNull(binding);
        assertEquals(MessageConsumer.FILA_MENSAGENS_GERAL,binding.getDestination());
        assertEquals(MensageriaService.ROUTING_KEY_MENSAGEM,binding.getRoutingKey());

    }

}
