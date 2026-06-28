package com.constructiq.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class AiContextSnippetResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sourceType;
    private Long sourceId;
    private Long projectId;
    private String projectName;
    private String title;
    private Double score;
    private String excerpt;
}
