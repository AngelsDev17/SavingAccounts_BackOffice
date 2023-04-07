package com.saving.accounts.dto;

import lombok.Data;

@Data
public class AccountDto {
    private Integer id;
    private String name;
    private String identification;
    private String password;
    private Integer amount;
}
