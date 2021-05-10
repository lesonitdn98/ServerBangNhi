package com.bangnhi.note.data.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "jwt")
@Data
public class JWT {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;

    public JWT() {
    }

    public JWT(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
