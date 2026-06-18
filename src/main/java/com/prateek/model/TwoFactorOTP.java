package com.prateek.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class TwoFactorOTP {
    @Id
    private String id;

    private String otp;

    @OneToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user; // internally JPA treats it as UserId only because Id is the primary key of User table.

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String jwt;

}
