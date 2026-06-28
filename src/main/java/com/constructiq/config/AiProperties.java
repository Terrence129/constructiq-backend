package com.constructiq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.provider")
public class AiProperties {

    private String name = "local";

    private String baseUrl = "http://localhost:11434";

    private String apiKey;

    private String chatModel = "llama3.1";

    private String embedModel = "all-MiniLM-L6-v2";

    private String chatFormat = "local";

    private String embedFormat = "local";

    private String chatEndpoint = "/api/chat";

    private String embedEndpoint = "/api/embeddings";

    private String embedFallbackEndpoint = "/api/embed";

    private boolean embeddingsEnabled = true;

    private double temperature = 0.2;
}
