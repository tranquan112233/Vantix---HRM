package poly.edu.vantix.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class JwtUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final List<GrantedAuthority> authorities;

    public JwtUserPrincipal(
            Long id,
            String username,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.username = Objects.requireNonNull(username, "username must not be null");
        this.authorities = authorities == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(authorities));
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
