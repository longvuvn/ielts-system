package com.ddhva.ielts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.logging.Logger;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class IeltsApplication {

	public static void main(String[] args) {
		SpringApplication.run(IeltsApplication.class, args);
	}
}
