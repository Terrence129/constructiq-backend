package com.constructiq.service;

import com.constructiq.config.AiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiProviderClientTest {

    @Test
    void chatSupportsOpenAiCompatibleResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiProviderClient client = new AiProviderClient(openAiProperties(), new ObjectMapper(), builder);

        server.expect(requestTo("https://api.example.com/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {
                              "message": {
                                "role": "assistant",
                                "content": "Use alternate supplier planning."
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        String response = client.chat(List.of(
                new AiProviderClient.ChatMessage("user", "What should we do?")
        ));

        assertThat(response).isEqualTo("Use alternate supplier planning.");
        server.verify();
    }

    @Test
    void embedSupportsOpenAiCompatibleResponse() {
        AiProperties properties = openAiProperties();
        properties.setEmbedEndpoint("/embeddings");
        properties.setEmbedFormat("openai");
        properties.setEmbedModel("text-embedding-3-small");

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiProviderClient client = new AiProviderClient(properties, new ObjectMapper(), builder);

        server.expect(requestTo("https://api.example.com/embeddings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andRespond(withSuccess("""
                        {
                          "data": [
                            {
                              "embedding": [0.1, 0.2, 0.3]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<Double> embedding = client.embed("steel delivery risk");

        assertThat(embedding).containsExactly(0.1, 0.2, 0.3);
        server.verify();
    }

    private AiProperties openAiProperties() {
        AiProperties properties = new AiProperties();
        properties.setBaseUrl("https://api.example.com");
        properties.setApiKey("test-key");
        properties.setChatEndpoint("/chat/completions");
        properties.setChatFormat("openai");
        properties.setChatModel("provider-chat");

        return properties;
    }
}
