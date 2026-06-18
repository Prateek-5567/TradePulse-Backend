package com.prateek.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prateek.domain.USER_ROLE;
import com.prateek.domain.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fullName = "";

    private String email = "";

    private String mobile = "";

    private String dateOfBirth = "";

    private String nationality = "";

    private String address = "";

    private String city = "";

    private String postcode = "";

    private String country = "";

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.PENDING;

    private boolean isVerified = false;

    @Embedded
    private TwoFactorAuth twoFactorAuth = new TwoFactorAuth();

    private String picture;

    @Enumerated(EnumType.STRING)
    private USER_ROLE role = USER_ROLE.ROLE_USER;
}

/*
In standard Java, developers must manually write accessor methods, constructors, and utility methods.
The @Data annotation automates this process at compile time using annotation processing,
thereby reducing code verbosity and improving maintainability.
It provides getters , setters , to_string and constructor
 */
