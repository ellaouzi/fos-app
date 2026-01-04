package com.fosagri.application.security;

/**
 * Role constants for access control
 */
public final class Roles {

    private Roles() {
        // Prevent instantiation
    }

    /**
     * Administrator role - full access to all features
     */
    public static final String ADMIN = "ADMIN";

    /**
     * Regular user role - access to adherent portal and personal features
     */
    public static final String USER = "USER";

    /**
     * Manager role - can manage prestations and demandes
     */
    public static final String MANAGER = "MANAGER";
}
