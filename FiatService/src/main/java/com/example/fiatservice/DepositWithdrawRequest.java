package com.example.fiatservice;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositWithdrawRequest {
    private Long userId;
    private String currency;
    private BigDecimal amount;

}
