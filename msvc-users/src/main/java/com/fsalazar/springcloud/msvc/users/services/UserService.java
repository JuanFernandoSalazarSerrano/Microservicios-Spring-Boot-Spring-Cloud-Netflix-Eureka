package com.fsalazar.springcloud.msvc.users.services;

import java.util.List;
import java.util.Optional;

import com.fsalazar.springcloud.msvc.users.entities.User;

public interface UserService {
    
    List<User> findAll();
    
    Optional<User> findById(Long id);

    User save(User user);

    User update(User user, Long id);

    void deleteById(Long id);
}
