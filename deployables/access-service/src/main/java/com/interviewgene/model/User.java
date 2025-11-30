package com.interviewgene.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "phone")
    private String phoneNo;

    @Column(nullable = false)
    private String role;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role);
    }

    @Override
    public String getUsername() {
        return this.email;   // IMPORTANT FIX
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;         // You can customize later
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;         // You can customize later
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;         // You can customize later
    }

    @Override
    public boolean isEnabled() {
        return this.enabled; // IMPORTANT FIX
    }
}
