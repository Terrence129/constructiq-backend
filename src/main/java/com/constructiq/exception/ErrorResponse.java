package com.constructiq.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/27 21:56
 */

@Getter
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}
