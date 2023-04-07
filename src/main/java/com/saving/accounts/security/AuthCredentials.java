package com.saving.accounts.security;

import lombok.Data;

@Data
public class AuthCredentials {
    private String identification;
    private String password;
}
