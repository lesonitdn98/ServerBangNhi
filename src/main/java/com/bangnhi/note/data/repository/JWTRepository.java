package com.bangnhi.note.data.repository;

import com.bangnhi.note.data.model.JWT;
import org.springframework.data.repository.CrudRepository;

public interface JWTRepository extends CrudRepository<JWT, Integer> {
    JWT findByToken(String token);

    void deleteByToken(String token);
}
