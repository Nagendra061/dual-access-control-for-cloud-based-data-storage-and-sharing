package com.project.dualaccesscontrol.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
}
