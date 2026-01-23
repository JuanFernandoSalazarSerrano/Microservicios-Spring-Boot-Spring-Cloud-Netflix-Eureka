package com.fsalazar.springcloud.msvc.users.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fsalazar.springcloud.msvc.users.entities.User;
import com.fsalazar.springcloud.msvc.users.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(User user, Long id) {
        Optional<User> existingUser = userRepository.findById(id);
        
        if (existingUser.isPresent()) {
            User userToUpdate = existingUser.get();
            
            if (user.getUsername() != null) {
                userToUpdate.setUsername(user.getUsername());
            }
            if (user.getPassword() != null) {
                userToUpdate.setPassword(user.getPassword());
            }
            if (user.getEmail() != null) {
                userToUpdate.setEmail(user.getEmail());
            }
            if (user.getEnabled() != null) {
                userToUpdate.setEnabled(user.getEnabled());
            }
            
            return userRepository.save(userToUpdate);
        }
        
        return null;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
