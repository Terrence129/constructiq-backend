package com.constructiq.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiAdviceRequest {

    private Long projectId;

    @Size(max = 1000, message = "Focus must be at most 1000 characters")
    private String focus;
}
