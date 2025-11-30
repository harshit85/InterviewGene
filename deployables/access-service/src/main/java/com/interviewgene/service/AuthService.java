package com.interviewgene.service;

import com.interviewgene.dto.AuthResponse;
import com.interviewgene.dto.LoginRequest;
import com.interviewgene.dto.RegisterRequest;
import com.interviewgene.model.User;
import com.interviewgene.repository.UserRepository;
import com.interviewgene.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @CacheEvict(value = "userByEmail", key = "#request.email")
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                        .name(request.getFullName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role("ROLE_USER")
                        .enabled(true)
                        .build();

        userRepository.save(user);

        String token = jwtUtils.generateToken(user);
        return AuthResponse.builder()
                           .token(token)
                           .tokenType("Bearer")
                           .email(user.getEmail())
                           .fullName(user.getName())
                           .role(user.getRole())
                           .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        );

        authenticationManager.authenticate(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                                  .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String token = jwtUtils.generateToken(user);

        return AuthResponse.builder()
                           .token(token)
                           .tokenType("Bearer")
                           .email(user.getEmail())
                           .fullName(user.getName())
                           .role(user.getRole())
                           .build();
    }
}
