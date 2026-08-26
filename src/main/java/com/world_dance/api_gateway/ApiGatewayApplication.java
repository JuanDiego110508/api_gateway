package com.world_dance.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {
    "org.springframework.cloud.consul.discovery.ConsulDiscoveryHealthIndicatorAutoConfiguration",
    "org.springframework.cloud.consul.serviceregistration.ConsulAutoRegistrationAutoConfiguration"
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}