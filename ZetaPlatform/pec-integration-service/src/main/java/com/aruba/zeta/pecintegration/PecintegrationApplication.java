package com.aruba.zeta.pecintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.grpc.client.ImportGrpcClients;

import com.aruba.zeta.integrtoken.grpc.IntegrationTokenServiceGrpc;
import it.aruba.zeta.user.grpc.UserManagementServiceGrpc;

@SpringBootApplication
@ConfigurationPropertiesScan
@ImportGrpcClients(target = "user-mgmt", types = UserManagementServiceGrpc.UserManagementServiceBlockingStub.class)
@ImportGrpcClients(target = "user-auth", types = IntegrationTokenServiceGrpc.IntegrationTokenServiceBlockingStub.class)
public class PecintegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(PecintegrationApplication.class, args);
	}

}
