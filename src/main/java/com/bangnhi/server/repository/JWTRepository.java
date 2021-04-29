package com.bangnhi.server.repository;

import com.bangnhi.server.model.JWT;
import org.springframework.data.repository.CrudRepository;

public interface JWTRepository extends CrudRepository<JWT, Integer> {
    JWT findByToken(String token);
    void deleteByToken(String token);
}
