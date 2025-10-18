package com.example.chatapp.dto;


import lombok.Getter;
import lombok.Setter; 

@Getter
@Setter

public class JwtResponse {
    private String token;
    private String username;



    public JwtResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }


}