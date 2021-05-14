package com.bangnhi.note.controller;

import com.bangnhi.note.data.model.User;
import com.bangnhi.note.data.repository.JWTRepository;
import com.bangnhi.note.data.repository.UserRepository;
import com.bangnhi.note.data.response.BaseResponse;
import com.bangnhi.note.utils.AppUtils;
import com.bangnhi.note.utils.JwtTokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserRepository userRepository;

    private final JWTRepository jwtRepository;

    public UserController(UserRepository userRepository, JWTRepository jwtRepository) {
        this.userRepository = userRepository;
        this.jwtRepository = jwtRepository;
    }

    @GetMapping
    public @ResponseBody
    ResponseEntity<BaseResponse<Iterable<User>>> getAllUsers(@RequestHeader("Authorization") String auth) {
        BaseResponse<Iterable<User>> responseBody;
        HttpStatus status;
        String token = JwtTokenUtils.getJwtFromRequest(auth);
        if (!AppUtils.validateAuthToken(token, jwtRepository)) {
            status = HttpStatus.UNAUTHORIZED;
            responseBody = new BaseResponse<>(false, "Unauthorized");
        } else {
            Iterable<User> users = userRepository.findAll();
            status = HttpStatus.OK;
            responseBody = new BaseResponse<>(true, "", users);
        }
        return new ResponseEntity<>(responseBody, status);
    }
}
