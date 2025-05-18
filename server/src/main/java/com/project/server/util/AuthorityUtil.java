package com.project.server.util;

public class AuthorityUtil {
    public static final String

    STUDENT = "hasRole('STUDENT')",
    LECTURER = "hasRole('LECTURER')",
            STAFF = "hasAnyRole('LECTURER', 'ADMINISTRATOR')",
    ADMINISTRATOR = "hasRole('ADMINISTRATOR')";
}
