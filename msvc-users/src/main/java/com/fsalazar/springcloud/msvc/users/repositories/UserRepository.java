package com.fsalazar.springcloud.msvc.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fsalazar.springcloud.msvc.users.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
}
