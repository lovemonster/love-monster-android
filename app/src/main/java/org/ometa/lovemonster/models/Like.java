package org.ometa.lovemonster.models;

import android.support.annotation.NonNull;

/**
 * Represents a Like on a {@link Love}.
 */
public class Like {

    /**
     * The {@link User} who liked the love. Required field.
     */
    @NonNull
    public User liker;

    /**
     * The {@link Love} which was liked. Required field.
     */
    @NonNull
    public Love love;

    /**
     * Instantiates a {@code Like} instance with the minimum required fields.
     *
     * @param liker
     *      the user who liked the love
     * @param love
     *      the love which was liked
     * @throws IllegalArgumentException
     *      if the specified liker or love are {@code null}
     */
    public Like(@NonNull final User liker, @NonNull final Love love) throws IllegalArgumentException {
        if (liker == null) {
            throw new IllegalArgumentException("argument `liker` cannot be null");
        }

        if (love == null) {
            throw new IllegalArgumentException("argument `love` cannot be null");
        }
        this.liker = liker;
        this.love = love;
    }
}
