package com.calendario.agendarreservas.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuracion del executor dedicado al envio asincrono de correos.
 * Aisla el envio de notificaciones del resto de tareas de la aplicacion.
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class AsyncMailConfig {

    @Bean(name = "mailTaskExecutor")
    public TaskExecutor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mail-");
        executor.initialize();
        return executor;
    }
}
