package com.aruba.zeta.userauth.client;

import org.springframework.stereotype.Component;

import it.aruba.zeta.user.grpc.GetUserRequest;
import it.aruba.zeta.user.grpc.UserManagementServiceGrpc;
import it.aruba.zeta.user.grpc.UserResponse;

/**
 * gRPC client for user-mgmt-service.
 *
 * Used to validate that a userId exists in the central user registry.
 */
@Component
public class UserMgmtClient {

    private final UserManagementServiceGrpc.UserManagementServiceBlockingStub stub;

    /**
     * Constructs the client with the given gRPC blocking stub.
     *
     * @param stub the blocking stub for UserManagementService
     */
    public UserMgmtClient(UserManagementServiceGrpc.UserManagementServiceBlockingStub stub) {
        this.stub = stub;
    }

    /**
     * Returns the UserResponse if the user exists.
     *
     * @param userId the unique identifier of the user
     * @return UserResponse containing the user details
     * @throws io.grpc.StatusRuntimeException if the user is not found (NOT_FOUND) or other gRPC errors occur
     */
    public UserResponse getUser(String userId) {
        return stub.getUser(GetUserRequest.newBuilder().setId(userId).build());
    }

}
