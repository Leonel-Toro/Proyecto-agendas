package com.calendario.agendarreservas.dto;

import org.springframework.lang.Nullable;

public record AgentChatResponse(
        String sessionId,
        String response,
        @Nullable String thinking
) {}
