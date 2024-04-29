package com.Park.services;

import com.Park.controllers.dto.UserRegistrationDto;
import com.Park.entities.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    User findByUsername(String username);

    List<User> findAllUsers();

    User findById(int id);

    User save(UserRegistrationDto registration);

    void update(User user);

    void deleteUserById(int id);


}
