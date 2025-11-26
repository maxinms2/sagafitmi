package com.sagafitmi.ecommerce.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.sagafitmi.ecommerce.model.User;
import com.sagafitmi.ecommerce.repository.UserRepository;

@Component
public class PepperAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final String pepper;

    public PepperAuthenticationProvider(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
            @Value("${app.security.pepper:Peluso3000.}") String pepper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.pepper = pepper != null ? pepper : "";
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        Object credentials = authentication.getCredentials();
        String password = credentials == null ? "" : credentials.toString();

        User user = userRepository.findByEmail(username).orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        String toCheck = password + pepper;
        if (!passwordEncoder.matches(toCheck, user.getPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        List<GrantedAuthority> authorities = List.of(authority);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, null, authorities);
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
