package com.fourati.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-SockJS broker for live notification push (see UserNotificationService.create()).
 *
 * The frontend's useWebSocketStore already assumes this exact shape (a
 * `.withSockJS()`-registered `/ws` endpoint and a `WebSocketAuthInterceptor`
 * validating the STOMP CONNECT frame's Bearer token) — this config and
 * WebSocketAuthChannelInterceptor are the backend half that was missing.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor;
    private final AppProperties appProperties;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(appProperties.cors().allowedOrigins().toArray(new String[0]))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic for broadcast, /queue for point-to-point; /user is the prefix
        // SimpMessagingTemplate.convertAndSendToUser(...) resolves against the
        // STOMP Principal set by WebSocketAuthChannelInterceptor.
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor);
    }
}
