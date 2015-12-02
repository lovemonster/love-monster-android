package org.ometa.lovemonster.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ometa.lovemonster.ui.presenters.DatePresenter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
     * An optional personalized message from the lover.  Optional field.
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
     * The {@link User} who sent this love.  Required field.
     */
    @NonNull
    public final User lover;

    /**
     * The {@link User} who received this love.  Required field.
     */
    @NonNull
    public final User lovee;

    /**
     * The {@link Like}s this love has received. May be empty, but cannot be null.
     * This field cannot be reassigned, but the contents may be modified freely by any thread (not threadsafe).
     */
    @NonNull
    public final List<Like> likes;

    /**
     * The timestamp for when this love was created.  Required field.
     */
    @NonNull
    public final Calendar createdAt;

    /**
     * Instantiates a {@code Love} instance with the minimum required fields.
     *
     *  @param reason
     *      the reason for why this love was sent
     * @param lover
     *      the {@link User} sending the love
     * @param lovee
     *      the {@link User} receiving the love
     * @throws IllegalArgumentException
     *      if the specified reason, lover, lovee, or createdAt are {@code null}
     */
    public Love(@NonNull final String reason, @NonNull final User lover, @NonNull final User lovee) throws IllegalArgumentException {
        this(reason, lover, lovee, Calendar.getInstance());
    }

    /**
     * Instantiates a {@code Love} instance with the minimum required fields.
     *
     *  @param reason
     *      the reason for why this love was sent
     * @param lover
     *      the {@link User} sending the love
     * @param lovee
     *      the {@link User} receiving the love
     * @param createdAt
     *      when this love was created
     * @throws IllegalArgumentException
     *      if the specified reason, lover, lovee, or createdAt are {@code null}
     */
    public Love(@NonNull final String reason, @NonNull final User lover, @NonNull final User lovee, @NonNull final Calendar createdAt) throws IllegalArgumentException {
        if (reason == null) {
            throw new IllegalArgumentException("argument `reason` cannot be null");
        }

        if (lover == null) {
            throw new IllegalArgumentException("argument `lover` cannot be null");
        }

        if (lovee == null) {
            throw new IllegalArgumentException("argument `lovee` cannot be null");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("argument `createdAt` cannot be null");
        }

        this.reason = reason;
        this.lover = lover;
        this.lovee = lovee;
        this.createdAt = createdAt;
        this.isPrivate = false;
        this.likes = new ArrayList<>();
    }

    /**
     * Checks whether the {@code Love} has a non-null, non-blank message.
     *
     * @return true if the message is not null and not all spaces
     */
    public boolean hasMessage() {
        if (message == null) {
            return false;
        }

        return message.replaceAll(" ", "") != "";
    }

    /**
     * Return the relative "time ago" of the {@code Love}.
     *
     * @return a string of the relative "time ago" of the {@code Love}.
     */
    public String timeAgo() {
        return DatePresenter.shortRelativeElapsedFrom(createdAt);
    }
}
