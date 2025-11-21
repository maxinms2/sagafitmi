package com.sagafitmi.ecommerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sagafitmi.ecommerce.dto.AuthRequestDTO;
import com.sagafitmi.ecommerce.dto.AuthResponseDTO;
import com.sagafitmi.ecommerce.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO auth) {
        if (auth == null || auth.getEmail() == null || auth.getPassword() == null) {
            return ResponseEntity.badRequest().body("email and password required");
        }

        try {
            var authToken = new UsernamePasswordAuthenticationToken(auth.getEmail(), auth.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Construir lista de roles para el JWT
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String jwt = jwtUtil.generateToken(auth.getEmail(), roles);

            AuthResponseDTO response = AuthResponseDTO.builder().token(jwt).build();
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
