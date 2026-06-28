package com.constructiq.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiChatRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 4000, message = "Message must be at most 4000 characters")
    private String message;

    private Long projectId;

    @Valid
    @Size(max = 10, message = "History must contain at most 10 messages")
    private List<AiChatMessageRequest> history = List.of();
}
