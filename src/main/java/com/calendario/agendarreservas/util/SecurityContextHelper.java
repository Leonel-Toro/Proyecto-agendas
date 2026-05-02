package com.calendario.agendarreservas.util;

import com.calendario.agendarreservas.exception.UnauthorizedOperationException;
import com.calendario.agendarreservas.model.User;
import com.calendario.agendarreservas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityContextHelper {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedOperationException("Usuario no autenticado");
        }
        String principal = authentication.getName();
        return userRepository.findByUsernameOrEmail(principal, principal)
                .orElseThrow(() -> new UnauthorizedOperationException("Usuario no encontrado: " + principal));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
