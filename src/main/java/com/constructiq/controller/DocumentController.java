package com.constructiq.controller;

import com.constructiq.dto.response.DocumentResponse;
import com.constructiq.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(
            value = "/api/projects/{projectId}/documents/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentResponse uploadDocument(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return documentService.uploadDocument(projectId, file, authentication);
    }

    @GetMapping("/api/projects/{projectId}/documents")
    public List<DocumentResponse> getDocumentsByProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        return documentService.getDocumentsByProject(projectId, authentication);
    }

    @GetMapping("/api/documents/{documentId}")
    public DocumentResponse getDocumentById(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        return documentService.getDocumentById(documentId, authentication);
    }

    @GetMapping("/api/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        DocumentService.DocumentFile documentFile = documentService.getDocumentFile(documentId, authentication);
        MediaType mediaType = documentFile.getFileType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(documentFile.getFileType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(documentFile.getFileSize())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documentFile.getFileName() + "\""
                )
                .body(documentFile.getResource());
    }

    @DeleteMapping("/api/documents/{documentId}")
    public void deleteDocument(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        documentService.deleteDocument(documentId, authentication);
    }
}
