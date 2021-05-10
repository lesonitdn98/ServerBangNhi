package com.bangnhi.note.data.repository;

import com.bangnhi.note.data.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findById(Long id);
}
