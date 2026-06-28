package com.constructiq.controller;

import com.constructiq.dto.request.AiAdviceRequest;
import com.constructiq.dto.request.AiChatRequest;
import com.constructiq.dto.response.AiAdviceResponse;
import com.constructiq.dto.response.AiChatResponse;
import com.constructiq.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public AiChatResponse chat(
            @Valid @RequestBody AiChatRequest request,
            Authentication authentication
    ) {
        return aiService.chat(request, authentication);
    }

    @PostMapping("/advice")
    public AiAdviceResponse advice(
            @Valid @RequestBody AiAdviceRequest request,
            Authentication authentication
    ) {
        return aiService.advice(request, authentication);
    }
}
