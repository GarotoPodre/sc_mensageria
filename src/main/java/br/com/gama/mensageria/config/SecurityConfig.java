package br.com.gama.mensageria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Classe pqara criar uma regra de exceção para o websocket endpoint.
 * Abilita toda as requisições para /ws/mensagens sem autenticação.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)//Desabilita CSRF, comum para APIs
                .authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers("/api/auth/**").permitAll() //Permite acesso aos endpoints de registro de login
                                .pathMatchers("/ws/mensagens/**").permitAll()//Permite acesso anônimo
                                .anyExchange().authenticated() //exige autenticação para qualquer outro endpoint (futuro)
                        )
                .build();
    }
}
