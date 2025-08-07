package br.com.gama.mensageria.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

import static br.com.gama.mensageria.service.AmqpManagementService.EXCHANGE_MENSAGENS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AmqpManagementServiceTest {

    @Mock
    private AmqpAdmin amqpAdmin;

    @InjectMocks
    private AmqpManagementService amqpManagementService;

    @Test
    @DisplayName("Deve criar uma fila e um binding para um novo usuário")
    void shouldCreateQueueAndBindingForNewUser() {
        //Arrange
        String nome = "usuario";
        String nomeFila = "fila." + nome;

        //Act
        amqpManagementService.criarInfraestruturaParaUsuario(nome);

        //Assert
        ArgumentCaptor<Queue> queueArgumentCaptor = ArgumentCaptor.forClass(Queue.class);
        verify(amqpAdmin).declareQueue(queueArgumentCaptor.capture());
        assertEquals(nomeFila, queueArgumentCaptor.getValue().getName());

        ArgumentCaptor<Binding> bindingArgumentCaptor = ArgumentCaptor.forClass(Binding.class);
        verify(amqpAdmin).declareBinding(bindingArgumentCaptor.capture());
        Binding binding = bindingArgumentCaptor.getValue();
        assertEquals(nomeFila, binding.getDestination());
        assertEquals(nome, binding.getRoutingKey());
    }
}