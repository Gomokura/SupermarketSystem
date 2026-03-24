package com.supermarket.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;     // 可选
    private String nickname;  // 可选
}
