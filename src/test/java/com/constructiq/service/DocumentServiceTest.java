package com.constructiq.service;

import com.constructiq.dto.response.DocumentResponse;
import com.constructiq.entity.Document;
import com.constructiq.entity.Project;
import com.constructiq.entity.User;
import com.constructiq.repository.DocumentRepository;
import com.constructiq.repository.ProjectRepository;
import com.constructiq.util.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectAccessService projectAccessService;

    @Mock
    private Utils utils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DocumentService documentService;

    @TempDir
    private Path tempDir;

    @Test
    void uploadDocumentStoresFileAndMetadata() throws Exception {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());
        User uploader = User.builder().id(1L).name("Admin User").build();
        Project project = Project.builder().id(10L).name("Harbour Tower").createdBy(uploader).build();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "site-plan.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        when(utils.getCurrentUser(authentication)).thenReturn(uploader);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            if (document.getId() == null) {
                document.setId(100L);
            }
            return document;
        });

        DocumentResponse response = documentService.uploadDocument(10L, file, authentication);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, times(2)).save(captor.capture());
        Document savedDocument = captor.getAllValues().get(1);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getFileName()).isEqualTo("site-plan.pdf");
        assertThat(response.getFileUrl()).isEqualTo("/api/documents/100/download");
        assertThat(savedDocument.getFileSize()).isEqualTo(file.getSize());
        assertThat(Files.readString(Path.of(savedDocument.getFilePath()))).isEqualTo("PDF content");
    }

    @Test
    void uploadDocumentRejectsUserWithoutManagementAccess() {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());
        User member = User.builder().id(2L).build();
        User creator = User.builder().id(1L).build();
        Project project = Project.builder().id(10L).createdBy(creator).build();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "site-plan.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        when(utils.getCurrentUser(authentication)).thenReturn(member);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You do not have permission to manage this project"))
                .when(projectAccessService)
                .checkProjectManagementAccess(project, member);

        assertThatThrownBy(() -> documentService.uploadDocument(10L, file, authentication))
                .isInstanceOf(AccessDeniedException.class);

        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void getDocumentsByProjectChecksProjectAccess() {
        User member = User.builder().id(2L).build();
        User uploader = User.builder().id(1L).name("Admin User").build();
        Project project = Project.builder().id(10L).name("Harbour Tower").createdBy(uploader).build();
        Document document = document(project, uploader, "uploads/test.pdf");

        when(utils.getCurrentUser(authentication)).thenReturn(member);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(documentRepository.findByProjectOrderByCreatedAtDesc(project)).thenReturn(List.of(document));

        List<DocumentResponse> documents = documentService.getDocumentsByProject(10L, authentication);

        verify(projectAccessService).checkProjectAccess(project, member);
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getId()).isEqualTo(100L);
    }

    @Test
    void getDocumentFileReturnsStoredResource() throws Exception {
        User member = User.builder().id(2L).build();
        User uploader = User.builder().id(1L).name("Admin User").build();
        Project project = Project.builder().id(10L).name("Harbour Tower").createdBy(uploader).build();
        Path storedFile = tempDir.resolve("site-plan.pdf");
        Files.writeString(storedFile, "PDF content");
        Document document = document(project, uploader, storedFile.toString());

        when(utils.getCurrentUser(authentication)).thenReturn(member);
        when(documentRepository.findById(100L)).thenReturn(Optional.of(document));

        DocumentService.DocumentFile documentFile = documentService.getDocumentFile(100L, authentication);

        verify(projectAccessService).checkProjectAccess(project, member);
        assertThat(documentFile.getFileName()).isEqualTo("site-plan.pdf");
        assertThat(documentFile.getResource().exists()).isTrue();
    }

    @Test
    void deleteDocumentRemovesFileAndMetadata() throws Exception {
        User uploader = User.builder().id(1L).name("Admin User").build();
        Project project = Project.builder().id(10L).name("Harbour Tower").createdBy(uploader).build();
        Path storedFile = tempDir.resolve("site-plan.pdf");
        Files.writeString(storedFile, "PDF content");
        Document document = document(project, uploader, storedFile.toString());

        when(utils.getCurrentUser(authentication)).thenReturn(uploader);
        when(documentRepository.findById(100L)).thenReturn(Optional.of(document));

        documentService.deleteDocument(100L, authentication);

        verify(projectAccessService).checkProjectManagementAccess(project, uploader);
        verify(documentRepository).delete(document);
        assertThat(Files.exists(storedFile)).isFalse();
    }

    private Document document(Project project, User uploader, String filePath) {
        return Document.builder()
                .id(100L)
                .project(project)
                .fileName("site-plan.pdf")
                .storedFileName("stored-site-plan.pdf")
                .filePath(filePath)
                .fileUrl("/api/documents/100/download")
                .fileType("application/pdf")
                .fileSize(11L)
                .uploadedBy(uploader)
                .build();
    }
}
