package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.dto.AgentChatResponse;
import com.calendario.agendarreservas.service.PsychologistAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PsychologistAgentServiceImpl implements PsychologistAgentService {

    private final ChatClient chatClient;

    @Override
    public AgentChatResponse chat(String sessionId, String userMessage) {
        ChatResponse aiResponse = chatClient.prompt()
                .advisors(spec -> spec.param("chat_memory_conversation_id", sessionId))
                .user(userMessage)
                .call()
                .chatResponse();

        String content = aiResponse.getResult().getOutput().getText();
        String thinking = extractThinking(aiResponse);
        return new AgentChatResponse(sessionId, content, thinking);
    }

    private String extractThinking(ChatResponse response) {
        try {
            Object val = response.getResult().getOutput().getMetadata().get("thinkingContent");
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
