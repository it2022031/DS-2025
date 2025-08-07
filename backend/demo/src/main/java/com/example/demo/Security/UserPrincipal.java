package com.example.demo.Security;

import com.example.demo.Entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(User user) {
        this.user = user;
        // βρες ένα πρώτο role ή default σε USER
        Role primary = user.getRoles().stream().findFirst().orElse(Role.USER);
        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + primary.name())
        );
    }

    public static UserPrincipal fromUser(User user) {
        return new UserPrincipal(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // είναι hashed
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // Μπορείς να τα προσαρμόσεις αν έχεις πεδία για αυτά στο User
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    // Προαιρετικά: getter για το underlying entity
    public User getUser() {
        return user;
    }
}
