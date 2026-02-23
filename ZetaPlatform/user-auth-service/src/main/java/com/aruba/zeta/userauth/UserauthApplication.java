package com.aruba.zeta.userauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;

import it.aruba.zeta.user.grpc.UserManagementServiceGrpc;

@SpringBootApplication
@ImportGrpcClients(target = "user-mgmt", types = UserManagementServiceGrpc.UserManagementServiceBlockingStub.class)
public class UserauthApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserauthApplication.class, args);
	}

}
