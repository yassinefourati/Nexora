package com.fourati.app.config;

import com.fourati.app.security.WebSocketAuthChannelInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-SockJS endpoint for live notification push (see UserNotificationService.create()).
 *
 * Broker: the in-memory simple broker by default (one replica), or a RabbitMQ
 * STOMP relay when {@code app.websocket.relay.enabled=true}. With the relay,
 * user-destination and user-registry broadcasts are enabled so a push for a
 * user connected to a different replica is delivered through the broker.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
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
        AppProperties.Relay relay = appProperties.websocket().relay();
        if (relay.enabled()) {
            log.info("WebSocket broker: STOMP relay {}:{}", relay.host(), relay.port());
            registry.enableStompBrokerRelay("/topic", "/queue")
                    .setRelayHost(relay.host())
                    .setRelayPort(relay.port())
                    .setClientLogin(relay.username())
                    .setClientPasscode(relay.password())
                    .setSystemLogin(relay.username())
                    .setSystemPasscode(relay.password())
                    // Cross-replica routing of /user/** destinations
                    .setUserDestinationBroadcast("/topic/unresolved-user-destination")
                    .setUserRegistryBroadcast("/topic/simp-user-registry");
        } else {
            log.info("WebSocket broker: in-memory simple broker (single replica)");
            registry.enableSimpleBroker("/topic", "/queue");
        }
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor);
    }
}
