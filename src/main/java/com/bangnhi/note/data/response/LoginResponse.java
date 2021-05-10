package com.bangnhi.note.data.response;

import com.bangnhi.note.data.model.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String fullName;
    private String username;
    private String email;
    private boolean enable;

    public LoginResponse(String accessToken, User user) {
        this.accessToken = accessToken;
        this.fullName = user.getFullName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.enable = user.isEnable();
    }
}
