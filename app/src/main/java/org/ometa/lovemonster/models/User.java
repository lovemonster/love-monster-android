package org.ometa.lovemonster.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Represents a User which can send and receive loves.
 */
public class User {

    /**
     * The email address for this user.  Required field.
     */
    @NonNull
    public String email;

    /**
     * The name for this user.  Optional field.
     */
    @Nullable
    public String name;

    /**
     * The username for this user. Required field.
     */
    @NonNull
    public String username;

    /**
     * Instantiates a {@code User} instance with the minimum required fields.
     *
     * @param email
     *      the email address for the user
     * @param username
     *      the username for the user
     * @throws IllegalArgumentException
     *      if the specified email or username are {@code null}
     */
    public User(@NonNull final String email, @NonNull final String username) throws IllegalArgumentException {
        if (email == null) {
            throw new IllegalArgumentException("argument `email` cannot be null");
        }

        if (username == null) {
            throw new IllegalArgumentException("argument `username` cannot be null");
        }

        this.email = email;
        this.username = username;
    }
}
