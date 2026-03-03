package org.banking.authorizationserver.service;

import org.banking.authorizationserver.kafka.producer.UserDataProducer;
import org.banking.authorizationserver.model.User;
import org.banking.authorizationserver.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserDataProducer userDataProducer;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserDataProducer userDataProducer,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userDataProducer = userDataProducer;
        this.passwordEncoder = passwordEncoder;
    }

    public void createAccount(String username, String password) {
        User user = new User(username, passwordEncoder.encode(password), Collections.emptyList());

        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateKeyException("User with username " + username + " already exists");
        }

        userRepository.save(user);

        userDataProducer.sendMessage(username);
    }
}
