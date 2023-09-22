package com.upx.userconsumer.model;

import lombok.Data;

@Data
public class UserKeycloakDTO {
    private Integer userId;
    private String keycloakId;
    private String email;
    private String userName;
}
