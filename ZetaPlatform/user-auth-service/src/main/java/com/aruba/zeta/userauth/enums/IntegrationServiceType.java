package com.aruba.zeta.userauth.enums;

/**
 * Identifies the third-party integration service associated with a stored token.
 * Used to disambiguate token records per user in {@code service_tokens}.
 */
public enum IntegrationServiceType {
    PEC,
    SIGN,
    CONSERVATION
}