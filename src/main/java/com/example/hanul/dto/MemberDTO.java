package com.example.hanul.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private String id;
    private String name;
    private String email;
    private String password;
    private String newPassword;
    private String token;

    public MemberDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
