package com.aruba.zeta.userauth.client;

import org.springframework.stereotype.Component;

import it.aruba.zeta.user.grpc.CreateUserRequest;
import it.aruba.zeta.user.grpc.DeleteUserRequest;
import it.aruba.zeta.user.grpc.DeleteUserResponse;
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

    /**
     * Creates a new user in the user-mgmt-service.
     *
     * @param username                 the unique username
     * @param email                    the user's email address
     * @param firstName                the user's first name
     * @param lastName                 the user's last name
     * @param role                     the user's role
     * @param semanticIndexingEnabled  whether semantic indexing is enabled
     * @return UserResponse containing the created user details
     * @throws io.grpc.StatusRuntimeException on gRPC errors (e.g. ALREADY_EXISTS)
     */
    public UserResponse createUser(String username, String email, String firstName,
                                   String lastName, String role, boolean semanticIndexingEnabled) {
        return stub.createUser(CreateUserRequest.newBuilder()
                .setUsername(username)
                .setEmail(email)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setRole(role)
                .setSemanticIndexingEnabled(semanticIndexingEnabled)
                .build());
    }

    /**
     * Deletes a user from the user-mgmt-service.
     *
     * @param userId the unique identifier of the user to delete
     * @return DeleteUserResponse containing success flag and message
     * @throws io.grpc.StatusRuntimeException on gRPC errors (e.g. NOT_FOUND)
     */
    public DeleteUserResponse deleteUser(String userId) {
        return stub.deleteUser(DeleteUserRequest.newBuilder().setId(userId).build());
    }

}
