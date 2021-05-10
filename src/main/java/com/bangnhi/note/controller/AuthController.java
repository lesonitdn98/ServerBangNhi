package com.bangnhi.note.controller;

import com.bangnhi.note.data.model.JWT;
import com.bangnhi.note.data.model.User;
import com.bangnhi.note.data.repository.JWTRepository;
import com.bangnhi.note.data.repository.UserRepository;
import com.bangnhi.note.data.response.BaseResponse;
import com.bangnhi.note.data.response.LoginResponse;
import com.bangnhi.note.utils.AppUtils;
import com.bangnhi.note.utils.JwtTokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final UserRepository userRepository;

    private final JWTRepository jwtRepository;

    public AuthController(UserRepository userRepository, JWTRepository jwtRepository) {
        this.userRepository = userRepository;
        this.jwtRepository = jwtRepository;
    }

    @PostMapping("register")
    public ResponseEntity<BaseResponse<Object>> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String email) {
        BaseResponse<Object> responseBody;
        HttpStatus status;
        if (username.isEmpty()) {
            responseBody = new BaseResponse<>(false, "Username is Empty!");
            status = HttpStatus.BAD_REQUEST;
        } else if (password.isEmpty()) {
            responseBody = new BaseResponse<>(false, "Password is Empty!");
            status = HttpStatus.BAD_REQUEST;
        } else if (name.isEmpty()) {
            responseBody = new BaseResponse<>(false, "Name is Empty!");
            status = HttpStatus.BAD_REQUEST;
        } else if (email.isEmpty()) {
            responseBody = new BaseResponse<>(false, "Email is Empty!");
            status = HttpStatus.BAD_REQUEST;
        } else if (userRepository.findByUsername(username) != null) {
            responseBody = new BaseResponse<>(false, "User already exists!");
            status = HttpStatus.CONFLICT;
        } else if (userRepository.findByEmail(email) != null) {
            responseBody = new BaseResponse<>(false, "Email already exists!");
            status = HttpStatus.CONFLICT;
        } else {
            User newUser = new User(name, username, AppUtils.getMD5(password), email, false);
            userRepository.save(newUser);
            responseBody = new BaseResponse<>(true, "Register success");
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(responseBody, null, status);
    }

    @PostMapping("login")
    public @ResponseBody
    ResponseEntity<BaseResponse<LoginResponse>> login(@RequestParam String username, @RequestParam String password) {
        BaseResponse<LoginResponse> responseBody;
        HttpStatus status;
        User user = userRepository.findByUsername(username);
        if (username.isEmpty()) {
            responseBody = new BaseResponse<>(false, "Username is Empty!");
            status = HttpStatus.BAD_REQUEST;
        } else if (password.isEmpty()) {
            responseBody = new BaseResponse<>(false, "Password is Empty!");
            status = HttpStatus.BAD_REQUEST;
        } else if (user == null) {
            responseBody = new BaseResponse<>(false, "Username does not exist!");
            status = HttpStatus.NOT_FOUND;
        } else {
            if (!user.getPassword().equals(AppUtils.getMD5(password))) {
                responseBody = new BaseResponse<>(false, "Password is incorrect!");
                status = HttpStatus.BAD_REQUEST;
            } else {
                String jwt = JwtTokenUtils.generateToken(user);
                JWT token = new JWT(jwt);
                jwtRepository.save(token);
                responseBody = new BaseResponse<>(true, "Login success", new LoginResponse(jwt, user));
                status = HttpStatus.OK;
            }
        }
        return new ResponseEntity<>(responseBody, null, status);
    }

    @PostMapping("logout")
    @Transactional
    public ResponseEntity<BaseResponse<Object>> logout(@RequestHeader("Authorization") String auth) {
        BaseResponse<Object> responseBody;
        HttpStatus status;
        String token = JwtTokenUtils.getJwtFromRequest(auth);
        if (AppUtils.validateAuthToken(token, jwtRepository)) {
            jwtRepository.deleteByToken(token);
            responseBody = new BaseResponse<>(true, "Logout Success");
            status = HttpStatus.OK;
        } else {
            responseBody = new BaseResponse<>(false, "Logout Failed!");
            status = HttpStatus.UNAUTHORIZED;
        }
        return new ResponseEntity<>(responseBody, status);
    }
}
