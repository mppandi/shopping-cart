package com.example.shop.user;

import com.example.shop.common.BusinessRuleException;
import com.example.shop.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Creates the account and returns a token, so the user is logged in straight away. */
    @Transactional
    public String register(String username, String password) {
        if (users.existsByUsername(username)) {
            throw new BusinessRuleException("Username '" + username + "' is already taken");
        }
        users.save(new UserEntity(username, passwordEncoder.encode(password)));
        return jwtService.generateToken(username);
    }

    /** Throws AuthenticationException (mapped to 401) when the credentials are wrong. */
    public String login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        return jwtService.generateToken(username);
    }
}
