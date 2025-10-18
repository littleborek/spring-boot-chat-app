package com.example.chatapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class EditMessageRequest {
	
    private Long messageId;
    private String newContent;



}