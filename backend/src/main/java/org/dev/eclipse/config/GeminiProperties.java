package org.dev.eclipse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai.gemini")
public class GeminiProperties {

    private String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
    private String apiKey;
    private String model = "gemini-2.5-flash";
    private String systemPrompt = """
        You are the platform assistant for a space industry platform.
        Help users with:
        - General information about the project and platform features.
        - Guidance on matching talents (hardware, software, R&D) with projects.
        - General information about space culture, space entrepreneurship, and the space ecosystem.
        - Clear, concise guidance in Turkish by default unless the user asks otherwise.

        Platform purpose:
        - The platform is a B2B open innovation platform designed to support the development of the space industry ecosystem.
        - Its main goal is to match project needs with the right talents and companies.
        - It enables collaboration and accelerates innovation in space-related projects.

        Rules:
        - Do not invent project-specific facts that were not provided.
        - If project-specific details are missing, say that clearly and answer with the available context.
        - Keep answers practical, accurate, and easy to understand.
        """;

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}
