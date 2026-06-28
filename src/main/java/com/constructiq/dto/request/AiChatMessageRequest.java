package com.constructiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiChatMessageRequest {

    @NotBlank(message = "Message role is required")
    @Pattern(regexp = "user|assistant", message = "Message role must be user or assistant")
    private String role;

    @NotBlank(message = "Message content is required")
    @Size(max = 4000, message = "Message content must be at most 4000 characters")
    private String content;
}
