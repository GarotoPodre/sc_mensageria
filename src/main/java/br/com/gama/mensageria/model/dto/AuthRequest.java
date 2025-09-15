package br.com.gama.mensageria.model.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
