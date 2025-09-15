package br.com.gama.mensageria.controller;

import br.com.gama.mensageria.model.User;
import br.com.gama.mensageria.model.dto.AuthRequest;
import br.com.gama.mensageria.model.dto.AuthResponse;
import br.com.gama.mensageria.repository.UserRepository;
import br.com.gama.mensageria.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@RequestBody AuthRequest authRequest) {
        //Verifica se o usuário já existe antes de salvar
        return userRepository.findByUsername(authRequest.getUsername())
                .flatMap(existingUser ->
                        //Se o usuário exixtir, retorna um erro de conflito
                        Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("Username já utilizado!"))
                )
                .switchIfEmpty(Mono.defer(()->{
                    //Se o usuário não existir, cria o novo usuário
                    User newUser = new User(null, authRequest.getUsername(), passwordEncoder.encode(authRequest.getPassword()));
                    return userRepository.save(newUser)
                            .map(u -> ResponseEntity.ok("User registered successfully!"));

        }));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest authRequest) {
        return userRepository.findByUsername(authRequest.getUsername())
                .filter(user -> passwordEncoder.matches(authRequest.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtProvider.createToken(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities()));
                    return ResponseEntity.ok(new AuthResponse(token));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
