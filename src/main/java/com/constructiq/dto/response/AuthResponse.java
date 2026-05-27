package com.constructiq.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/27 21:46
 */
@Getter
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private UserResponse user;
}
