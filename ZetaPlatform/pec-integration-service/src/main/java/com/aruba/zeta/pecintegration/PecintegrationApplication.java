package com.aruba.zeta.pecintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.grpc.client.ImportGrpcClients;

import com.aruba.zeta.integrtoken.grpc.IntegrationTokenServiceGrpc;

@SpringBootApplication
@ConfigurationPropertiesScan
@ImportGrpcClients(target = "user-auth", types = IntegrationTokenServiceGrpc.IntegrationTokenServiceBlockingStub.class)
public class PecintegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(PecintegrationApplication.class, args);
	}

}
