package com.fsalazar.springcloud.msvc.users.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fsalazar.springcloud.msvc.users.entities.Role;
import com.fsalazar.springcloud.msvc.users.entities.User;
import com.fsalazar.springcloud.msvc.users.repositories.RoleRepository;
import com.fsalazar.springcloud.msvc.users.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        List<Role> roles = getRoleOptional(user);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    private List<Role> getRoleOptional(User user) {
        // create roles list, call to DB to search roles by name, finds the role and adds it to the end of the list of user roles to set that list to the user and stablish the user roles
        List<Role> roles = new ArrayList<>();
        Optional<Role> roleOptional = roleRepository.findByName("ROLE_USER");
        roleOptional.ifPresent(role -> roles.add(role));

        if (user.isAdmin()){
            Optional<Role> adminRoleOptional = roleRepository.findByName("ROLE_ADMIN");
            adminRoleOptional.ifPresent(role -> roles.add(role));
        }
        return roles;
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
