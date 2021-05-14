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
            status = HttpStatus.BAD_REQUEST;
            responseBody = new BaseResponse<>(false, "Username is Empty!");
        } else if (password.isEmpty()) {
            status = HttpStatus.BAD_REQUEST;
            responseBody = new BaseResponse<>(false, "Password is Empty!");
        } else if (name.isEmpty()) {
            status = HttpStatus.BAD_REQUEST;
            responseBody = new BaseResponse<>(false, "Name is Empty!");
        } else if (email.isEmpty()) {
            status = HttpStatus.BAD_REQUEST;
            responseBody = new BaseResponse<>(false, "Email is Empty!");
        } else if (userRepository.findByUsername(username) != null) {
            status = HttpStatus.CONFLICT;
            responseBody = new BaseResponse<>(false, "User already exists!");
        } else if (userRepository.findByEmail(email) != null) {
            status = HttpStatus.CONFLICT;
            responseBody = new BaseResponse<>(false, "Email already exists!");
        } else {
            User newUser = new User(name, username, AppUtils.getMD5(password), email, false);
            userRepository.save(newUser);
            status = HttpStatus.OK;
            responseBody = new BaseResponse<>(true, "Register success");
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
            status = HttpStatus.BAD_REQUEST;
            responseBody = new BaseResponse<>(false, "Username is Empty!");
        } else if (password.isEmpty()) {
            status = HttpStatus.BAD_REQUEST;
            responseBody = new BaseResponse<>(false, "Password is Empty!");
        } else if (user == null) {
            status = HttpStatus.NOT_FOUND;
            responseBody = new BaseResponse<>(false, "Username does not exist!");
        } else {
            if (!user.getPassword().equals(AppUtils.getMD5(password))) {
                status = HttpStatus.BAD_REQUEST;
                responseBody = new BaseResponse<>(false, "Password is incorrect!");
            } else {
                String jwt = JwtTokenUtils.generateToken(user);
                JWT token = new JWT(jwt);
                jwtRepository.save(token);
                status = HttpStatus.OK;
                responseBody = new BaseResponse<>(true, "Login success", new LoginResponse(jwt, user));
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
            status = HttpStatus.OK;
            responseBody = new BaseResponse<>(true, "Logout Success");
        } else {
            status = HttpStatus.UNAUTHORIZED;
            responseBody = new BaseResponse<>(false, "Logout Failed!");
        }
        return new ResponseEntity<>(responseBody, status);
    }
}
