package com.constructiq.service;

import com.constructiq.dto.response.DocumentResponse;
import com.constructiq.config.CacheConfig;
import com.constructiq.entity.Document;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.exception.ResourceNotFoundException;
import com.constructiq.repository.DocumentRepository;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.util.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAccessService projectAccessService;
    private final Utils utils;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.DOCUMENTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
    })
    public DocumentResponse uploadDocument(Long projectId, MultipartFile file, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProject(projectId);

        projectAccessService.checkProjectManagementAccess(project, currentUser);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is required");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null
                ? "document"
                : file.getOriginalFilename());

        if (originalFileName.contains("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String storedFileName = UUID.randomUUID() + "_" + originalFileName;
        Path projectUploadPath = Path.of(uploadDir, "projects", String.valueOf(project.getId()))
                .toAbsolutePath()
                .normalize();
        Path targetPath = projectUploadPath.resolve(storedFileName).normalize();

        if (!targetPath.startsWith(projectUploadPath)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        try {
            Files.createDirectories(projectUploadPath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to store uploaded file");
        }

        Document document = Document.builder()
                .project(project)
                .fileName(originalFileName)
                .storedFileName(storedFileName)
                .filePath(targetPath.toString())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedBy(currentUser)
                .build();

        Document savedDocument = documentRepository.save(document);
        savedDocument.setFileUrl("/api/documents/" + savedDocument.getId() + "/download");

        return toResponse(documentRepository.save(savedDocument));
    }

    @Cacheable(cacheNames = CacheConfig.DOCUMENTS, key = "#authentication.name + ':project:' + #projectId")
    public List<DocumentResponse> getDocumentsByProject(Long projectId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Project project = getProject(projectId);

        projectAccessService.checkProjectAccess(project, currentUser);

        return documentRepository.findByProjectOrderByCreatedAtDesc(project)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(cacheNames = CacheConfig.DOCUMENTS, key = "#authentication.name + ':id:' + #documentId")
    public DocumentResponse getDocumentById(Long documentId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Document document = getDocument(documentId);

        projectAccessService.checkProjectAccess(document.getProject(), currentUser);

        return toResponse(document);
    }

    public DocumentFile getDocumentFile(Long documentId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Document document = getDocument(documentId);

        projectAccessService.checkProjectAccess(document.getProject(), currentUser);

        try {
            Path filePath = Path.of(document.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Document file not found");
            }

            return new DocumentFile(
                    resource,
                    document.getFileName(),
                    document.getFileType(),
                    document.getFileSize()
            );
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Document file not found");
        }
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.DOCUMENTS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
    })
    public void deleteDocument(Long documentId, Authentication authentication) {
        User currentUser = utils.getCurrentUser(authentication);
        Document document = getDocument(documentId);

        projectAccessService.checkProjectManagementAccess(document.getProject(), currentUser);

        try {
            Files.deleteIfExists(Path.of(document.getFilePath()));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to delete document file");
        }

        documentRepository.delete(document);
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    private DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .projectId(document.getProject().getId())
                .projectName(document.getProject().getName())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .uploadedById(document.getUploadedBy().getId())
                .uploadedByName(document.getUploadedBy().getName())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    @Getter
    public static class DocumentFile {

        private final Resource resource;
        private final String fileName;
        private final String fileType;
        private final Long fileSize;

        public DocumentFile(Resource resource, String fileName, String fileType, Long fileSize) {
            this.resource = resource;
            this.fileName = fileName;
            this.fileType = fileType;
            this.fileSize = fileSize;
        }
    }
}
