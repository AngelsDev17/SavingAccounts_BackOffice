package com.saving.accounts.dto;

import lombok.Data;

@Data
public class SendMoneyDto {
    private String account;
    private Integer amount;
}
