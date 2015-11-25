package org.ometa.lovemonster.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a User which can send and receive {@link Love}s.
 */
public class User {

    /**
     * Represents the association type between a love and a user.
     */
    public enum UserLoveAssociation {
        all, lover, lovee;
    }

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
     * The {@link Love}s this user has received.  May be empty, but cannot be null.
     * This field cannot be reassigned, but the contents may be modified freely by any thread (not threadsafe).
     */
    @NonNull
    public final List<Love> receivedLoves;

    /**
     * The {@link Love}s this user has sent.  May be empty, but cannot be null.
     * This field cannot be reassigned, but the contents may be modified freely by any thread (not threadsafe).
     */
    @NonNull
    public final List<Love> sentLoves;

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
        this.receivedLoves = new ArrayList<>();
        this.sentLoves = new ArrayList<>();
    }
}
