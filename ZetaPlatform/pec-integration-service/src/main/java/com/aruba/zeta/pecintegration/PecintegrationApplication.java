package com.aruba.zeta.pecintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;

import it.aruba.zeta.user.grpc.UserProvisioningServiceGrpc;

@SpringBootApplication
@ImportGrpcClients(target = "user-mgmt", types = UserProvisioningServiceGrpc.UserProvisioningServiceBlockingStub.class)
public class PecintegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(PecintegrationApplication.class, args);
	}

}
