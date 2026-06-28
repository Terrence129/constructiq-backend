package com.constructiq.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AiChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String answer;
    private Long projectId;
    private String chatModel;
    private String embedModel;
    private LocalDateTime generatedAt;
    private List<AiContextSnippetResponse> context;
}
