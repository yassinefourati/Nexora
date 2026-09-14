package com.fourati.app.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
    @DefaultValue Async async,
    @DefaultValue Export export,
    @DefaultValue Storage storage,
    @DefaultValue Cors cors,
    @DefaultValue Websocket websocket) {

    public record Async(
			@DefaultValue("5") @Min(1) int corePoolSize,
			@DefaultValue("20") @Min(1) int maxPoolSize,
			@DefaultValue("100") @Min(0) int queueCapacity,        
			@DefaultValue("async-") @NotBlank String threadNamePrefix) { 
    }

	public record Export(@DefaultValue("10000") @Min(1) @Max(100_000) int maxRows) {
	}

	public record Storage(@DefaultValue("uploads") @NotBlank String location,
			@DefaultValue("50") @Min(1) int maxFileSizeMb) {
	}

	/** STOMP broker relay for multi-replica WebSocket push (RabbitMQ with the STOMP plugin). */
	public record Websocket(@DefaultValue Relay relay) {
	}

	public record Relay(@DefaultValue("false") boolean enabled,
			@DefaultValue("localhost") @NotBlank String host,
			@DefaultValue("61613") @Min(1) int port,
			@DefaultValue("guest") String username,
			@DefaultValue("guest") String password) {
	}

	public record Cors(@DefaultValue("*") List<String> allowedOrigins,
			@DefaultValue("GET,POST,PUT,PATCH,DELETE,OPTIONS") List<String> allowedMethods,
			@DefaultValue("*") List<String> allowedHeaders, 
			@DefaultValue("false") boolean allowCredentials,
			@DefaultValue("3600") long maxAge) {
	}

}
