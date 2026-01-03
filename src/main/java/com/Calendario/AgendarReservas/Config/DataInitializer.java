package com.Calendario.AgendarReservas.Config;

import com.Calendario.AgendarReservas.Model.Role;
import com.Calendario.AgendarReservas.Model.User;
import com.Calendario.AgendarReservas.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@agendarreservas.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.admin.create-on-startup:true}")
    private boolean createAdminOnStartup;

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!createAdminOnStartup) {
                logger.info("Creación de admin en startup deshabilitada");
                return;
            }

            // Check if admin user already exists
            if (userRepository.existsByUsername(adminUsername)) {
                logger.info("Usuario admin '{}' ya existe", adminUsername);
                return;
            }

            // Create admin user
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
            admin.setEnabled(true);
            admin.setAccountNonLocked(true);

            userRepository.save(admin);

            logger.info("Usuario admin creado exitosamente:");
            logger.info("  Username: {}", adminUsername);
            logger.info("  Email: {}", adminEmail);
            logger.warn("  ¡IMPORTANTE! Cambie la contraseña del admin en producción");
        };
    }
}

