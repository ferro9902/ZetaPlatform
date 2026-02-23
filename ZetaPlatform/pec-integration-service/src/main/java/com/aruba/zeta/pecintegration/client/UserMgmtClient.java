package com.aruba.zeta.pecintegration.client;

import org.springframework.stereotype.Component;

import it.aruba.zeta.user.grpc.CreateUserRequest;
import it.aruba.zeta.user.grpc.DeleteUserRequest;
import it.aruba.zeta.user.grpc.DeleteUserResponse;
import it.aruba.zeta.user.grpc.GetUserRequest;
import it.aruba.zeta.user.grpc.UpdateUserRequest;
import it.aruba.zeta.user.grpc.UserProvisioningServiceGrpc;
import it.aruba.zeta.user.grpc.UserResponse;

/**
 * gRPC client for user-mgmt-service.
 *
 * Used to validate that a userId exists in the central user registry before
 * creating a PEC mailbox for that user.
 */
@Component
public class UserMgmtClient {

    private final UserProvisioningServiceGrpc.UserProvisioningServiceBlockingStub stub;

    /**
     * Constructs the client with the given gRPC blocking stub.
     *
     * @param stub the blocking stub for UserProvisioningService
     */
    public UserMgmtClient(UserProvisioningServiceGrpc.UserProvisioningServiceBlockingStub stub) {
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

    /**
     * Creates a new user in the central registry.
     *
     * @param request the CreateUserRequest containing user details
     * @return UserResponse containing the created user
     * @throws io.grpc.StatusRuntimeException if the creation fails (e.g. ALREADY_EXISTS)
     */
    public UserResponse createUser(CreateUserRequest request) {
        return stub.createUser(request);
    }

    /**
     * Updates an existing user in the central registry.
     *
     * @param request the UpdateUserRequest containing updated user details
     * @return UserResponse containing the updated user
     * @throws io.grpc.StatusRuntimeException if the update fails (e.g. NOT_FOUND)
     */
    public UserResponse updateUser(UpdateUserRequest request) {
        return stub.updateUser(request);
    }

    /**
     * Deletes a user from the central registry by ID.
     *
     * @param userId the unique identifier of the user to delete
     * @return DeleteUserResponse indicating success or failure
     * @throws io.grpc.StatusRuntimeException if the deletion fails
     */
    public DeleteUserResponse deleteUser(String userId) {
        return stub.deleteUser(DeleteUserRequest.newBuilder().setId(userId).build());
    }
}
