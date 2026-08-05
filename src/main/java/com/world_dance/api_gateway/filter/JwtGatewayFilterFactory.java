package com.world_dance.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.world_dance.api_gateway.service.JwtService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    private final JwtService jwtService;

    @Override
    public GatewayFilter apply(Object config) {

        return (exchange, chain) -> {

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
}