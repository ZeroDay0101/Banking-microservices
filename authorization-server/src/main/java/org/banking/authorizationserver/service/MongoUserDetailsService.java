package org.banking.authorizationserver.service;

import org.banking.authorizationserver.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MongoUserDetailsService implements UserDetailsService {

    private final UserRepository accountRepository;

    public MongoUserDetailsService(UserRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        org.banking.authorizationserver.model.User account = accountRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(username)
        );


        return new User(account.getUsername(), account.getPassword(), new ArrayList<>());
    }
}
