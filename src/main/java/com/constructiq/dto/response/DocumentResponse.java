package com.constructiq.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long projectId;
    private String projectName;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
