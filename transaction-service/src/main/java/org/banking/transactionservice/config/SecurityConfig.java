package org.banking.transactionservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public NewTopic accountTransferTopic(@Value("${banking.kafka.topics.transfer}") String topicName) {
        return new NewTopic(topicName, 2, (short) 1); // 3 partitions, replication factor 1
    }

    @Bean
    public NewTopic accountDepositTopic(@Value("${banking.kafka.topics.deposit}") String topicName) {
        return new NewTopic(topicName, 2, (short) 1); // 3 partitions, replication factor 1
    }

}

