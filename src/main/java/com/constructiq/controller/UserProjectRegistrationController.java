package com.constructiq.controller;

import com.constructiq.dto.request.UserProjectRegistrationRequest;
import com.constructiq.dto.response.UserProjectRegistrationResponse;
import com.constructiq.service.UserProjectRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/registrations")
@RequiredArgsConstructor
public class UserProjectRegistrationController {

    private final UserProjectRegistrationService registrationService;

    @PostMapping
    public UserProjectRegistrationResponse createRegistration(
            @PathVariable Long projectId,
            @Valid @RequestBody UserProjectRegistrationRequest request,
            Authentication authentication
    ) {
        return registrationService.createRegistration(projectId, request, authentication);
    }

    @GetMapping
    public List<UserProjectRegistrationResponse> getRegistrations(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        return registrationService.getRegistrations(projectId, authentication);
    }

    @DeleteMapping("/{registrationId}")
    public void deleteRegistration(
            @PathVariable Long projectId,
            @PathVariable Long registrationId,
            Authentication authentication
    ) {
        registrationService.deleteRegistration(projectId, registrationId, authentication);
    }
}
