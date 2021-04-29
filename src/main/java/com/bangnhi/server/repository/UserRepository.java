package com.bangnhi.server.repository;

import com.bangnhi.server.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findById(Long id);
}
