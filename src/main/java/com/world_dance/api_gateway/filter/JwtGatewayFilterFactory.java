package com.world_dance.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.world_dance.api_gateway.service.JwtService;

import reactor.core.publisher.Mono;

@Component
public class JwtGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtGatewayFilterFactory.Config> {

    private final JwtService jwtService;

    public JwtGatewayFilterFactory(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // 1. Omitir validación de JWT para rutas públicas o de integración externa
            if (path.startsWith("/actuator") || path.startsWith("/api/v1/auth") || path.startsWith("/api/v1/stream/oauth")) {
                return chain.filter(exchange);
            }

            // 2. Validar presencia del token Authorization
            String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return unauthorized(exchange);
            }

            String token = authorization.substring(7);

            if (!jwtService.isTokenValid(token)) {
                return unauthorized(exchange);
            }

            Long userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);

            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(request -> request
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Email", email))
                .build();

            return chain.filter(modifiedExchange);
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    // Clase de configuración requerida por AbstractGatewayFilterFactory
    public static class Config {
        // Puedes agregar propiedades de configuración si las necesitas en el futuro
    }
}