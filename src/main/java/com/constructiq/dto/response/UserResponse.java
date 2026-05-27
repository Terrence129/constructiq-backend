package com.constructiq.dto.response;

import com.constructiq.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/27 21:45
 */
@Getter
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private UserRole role;
}
