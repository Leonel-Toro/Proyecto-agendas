package com.calendario.agendarreservas.service;

import com.calendario.agendarreservas.dto.AgentChatResponse;

public interface PsychologistAgentService {
    AgentChatResponse chat(String sessionId, String userMessage);
}
