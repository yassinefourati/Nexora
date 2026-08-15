package com.fourati;

import com.fourati.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
    }

    @Component
    @RequiredArgsConstructor
    static class StartupLogger {

        private final Environment env;

        @EventListener(ApplicationReadyEvent.class)
        public void onReady() {
            boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

			String port = env.getProperty("server.port", "8080");
			String context = env.getProperty("server.servlet.context-path", "");
			String base = "http://localhost:" + port + context;
			String name = env.getProperty("spring.application.name", "template");
			String profile = String.join(", ", env.getActiveProfiles());
            if (profile.isBlank()) 
            	profile = "default";

			String line = "-".repeat(54);

            System.out.println();
            System.out.println(line);
            System.out.printf("  %s  [%s]%n", name.toUpperCase(), profile);
            System.out.println(line);

            if (!isProd) {
                System.out.println();
				System.out.printf("  %-16s %s%n", "Swagger UI", base + "/swagger-ui.html");
				System.out.printf("  %-16s %s%n", "OpenAPI JSON", base + "/v3/api-docs");
                System.out.println();
                System.out.println(line); 
            }
           
            System.out.println();
			System.out.printf("  %-16s %s%n", "Health", base + "/actuator/health");
			System.out.printf("  %-16s %s%n", "Info", base + "/actuator/info");
			System.out.printf("  %-16s %s%n", "Metrics", base + "/actuator/metrics");
			System.out.printf("  %-16s %s%n", "Prometheus", base + "/actuator/prometheus");
            System.out.println();
            System.out.println(line);
            System.out.println();
        }
    }
}