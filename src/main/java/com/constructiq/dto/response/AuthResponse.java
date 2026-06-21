package com.constructiq.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/27 21:46
 */
@Getter
@Builder
public class AuthResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private String tokenType;
    private UserResponse user;
}
