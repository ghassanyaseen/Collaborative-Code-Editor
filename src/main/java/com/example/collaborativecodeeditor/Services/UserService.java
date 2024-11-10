package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.Provider;
import com.example.collaborativecodeeditor.Entity.Role;
import com.example.collaborativecodeeditor.Entity.User;
import com.example.collaborativecodeeditor.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User saveNewUser(String username, String password, Provider provider, String providerID) {
        User user = new User();
        user.setUsername(username);
        user.setProvider(provider);
        user.setProviderID(providerID);

        // Encode password only for normal users
        if (provider == Provider.NORMAL && password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        } else {
            user.setPassword(null); // OAuth2 users do not have a password
        }

        user.setRole(userRepository.count() == 0 ? Role.ADMIN : Role.VIEWER);
        return userRepository.save(user);
    }




    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }


    public User getByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }


    public List<User> findAllUsers() {
        return userRepository.findAll();
    }


    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setRole(newRole);
        userRepository.save(user);
    }


    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }


    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

}