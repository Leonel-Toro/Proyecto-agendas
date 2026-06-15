package com.calendario.agendarreservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AgendarReservasApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendarReservasApplication.class, args);
	}

}
