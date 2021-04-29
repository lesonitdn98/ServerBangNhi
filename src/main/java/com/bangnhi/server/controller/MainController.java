package com.bangnhi.server.controller;

import com.bangnhi.server.jwt.CustomUserDetails;
import com.bangnhi.server.jwt.JwtAuthenticationFilter;
import com.bangnhi.server.jwt.JwtTokenProvider;
import com.bangnhi.server.model.JWT;
import com.bangnhi.server.model.User;
import com.bangnhi.server.repository.JWTRepository;
import com.bangnhi.server.repository.UserRepository;
import com.bangnhi.server.response.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class MainController {
    private final UserRepository userRepository;

    private final JWTRepository jwtRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    public MainController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserRepository userRepository, PasswordEncoder passwordEncoder, JWTRepository jwtRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtRepository = jwtRepository;
    }

    @GetMapping
    public String home() {
        return "API Document: http://localhost:8080/api/swagger-ui.html";
    }

    @PostMapping("/login")
    public LoginResponse authenticateUser(@RequestParam String username, @RequestParam String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken((CustomUserDetails) authentication.getPrincipal());

        JWT token = new JWT(jwt);
        jwtRepository.save(token);
        return new LoginResponse(jwt, userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @PostMapping("/register")
    public @ResponseBody
    ResponseEntity<String> register(@RequestParam String username, @RequestParam String password, @RequestParam String name
            , @RequestParam String email) {

        if (username.isEmpty()) {
            return new ResponseEntity<>("Username is Empty!", null, HttpStatus.BAD_REQUEST);
        }
        if (password.isEmpty()) {
            return new ResponseEntity<>("Password is Empty!", null, HttpStatus.BAD_REQUEST);
        }
        if (name.isEmpty()) {
            return new ResponseEntity<>("Name is Empty!", null, HttpStatus.BAD_REQUEST);
        }
        if (email.isEmpty()) {
            return new ResponseEntity<>("Email is Empty!", null, HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findByUsername(username) != null) {
            return new ResponseEntity<>("User already exists!", null, HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findByEmail(email) != null) {
            return new ResponseEntity<>("Email already exists!", null, HttpStatus.BAD_REQUEST);
        }

        User newUser = new User(name, username, passwordEncoder.encode(password), email, false);
        userRepository.save(newUser);
        return new ResponseEntity<>("Register Success", null, HttpStatus.OK);
    }

    @GetMapping("/all")
    public @ResponseBody
    Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/logout")
    @Transactional
    public @ResponseBody
    ResponseEntity<String> logout(HttpServletRequest request) {
        String token = JwtAuthenticationFilter.getJwtFromRequest(request);
        if (StringUtils.hasText(token) && (jwtRepository.findByToken(token) != null)) {
            jwtRepository.deleteByToken(token);
        } else {
            return new ResponseEntity<>("Logout Failed!", null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Logout Success", null, HttpStatus.OK);
    }
}
