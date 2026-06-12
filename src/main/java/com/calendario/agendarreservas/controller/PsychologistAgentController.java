package com.calendario.agendarreservas.controller;

import com.calendario.agendarreservas.dto.AgentChatResponse;
import com.calendario.agendarreservas.dto.ChatRequest;
import com.calendario.agendarreservas.model.ResponseApi;
import com.calendario.agendarreservas.service.PsychologistAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class PsychologistAgentController {

    private final PsychologistAgentService agentService;

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(agentService.chat(request.sessionId(), request.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApi<Void>> handleAgentError(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(new ResponseApi<>(500, "Error procesando el mensaje: " + ex.getMessage()));
    }
}
