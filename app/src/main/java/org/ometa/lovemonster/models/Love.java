package org.ometa.lovemonster.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Represents a Love which can be sent from one {@link User} to another {@link User}.
 */
public class Love {
    /**
     * The reason note for why this love was sent.  Required field.
     */
    @NonNull
    public String reason;

    /**
     * An optional personalized message from the sender.  Optional field.
     */
    @Nullable
    public String message;

    /**
     * Whether this love was sent privately or not.  True if this is a private Love, and false otherwise.
     * Defaults to false.  Required field.
     */
    @NonNull
    public boolean isPrivate;

    /**
     * The user who sent this love.  Required field.
     */
    @NonNull
    public User sender;

    /**
     * The user who received this love.  Required field.
     */
    @NonNull
    public User receiver;

    /**
     * Instantiates a {@code Love} instance with the minimum required fields.
     *
     *  @param reason
     *      the reason for why this love was sent
     * @param sender
     *      the {@link User} sending the love
     * @param receiver
     *      the {@link User} receiving the love
     * @throws IllegalArgumentException
     *      if the specified reason, sender, or receiver are {@code null}
     */
    public Love(@NonNull final String reason, @NonNull final User sender, @NonNull final User receiver) throws IllegalArgumentException {
        if (reason == null) {
            throw new IllegalArgumentException("argument `reason` cannot be null");
        }

        if (sender == null) {
            throw new IllegalArgumentException("argument `sender` cannot be null");
        }

        if (receiver == null) {
            throw new IllegalArgumentException("argument `receiver` cannot be null");
        }

        this.reason = reason;
        this.sender = sender;
        this.receiver = receiver;
        this.isPrivate = false;
    }
}
