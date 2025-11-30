package com.interviewgene.service;

import com.interviewgene.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "userByEmail", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username is email here
        return userRepository.findByEmail(username)
                             .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }
}
