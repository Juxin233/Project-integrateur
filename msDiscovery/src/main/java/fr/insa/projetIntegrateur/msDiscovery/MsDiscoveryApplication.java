package fr.insa.projetIntegrateur.msDiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer // This is mandatory for the Discovery Service
public class MsDiscoveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsDiscoveryApplication.class, args);
    }
}