package com.todo.flux.module.user.entity;

import com.todo.flux.module.user.dto.RegisterRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.Serializable;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(unique = true)
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleEnum role;

    public static User fromRequest(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            return null;
        }

        User user = new User();
        BeanUtils.copyProperties(registerRequest, user);
        user.setPassword(new BCryptPasswordEncoder().encode(registerRequest.password()));
        return user;
    }
}
