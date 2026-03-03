package org.banking.authorizationserver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Getter
@Document(collection = "users")
@NoArgsConstructor
public class User {

    @Id
    private String username;
    private String password;
    private List<GrantedAuthority> roles;


    public User(String username, String password, List<GrantedAuthority> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }


}
