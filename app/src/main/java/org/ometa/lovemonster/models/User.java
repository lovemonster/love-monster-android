package org.ometa.lovemonster.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a User which can send and receive {@link Love}s.
 */
public class User implements Parcelable {

    /**
     * The key used to reference this User when parceling it.
     */
    public static final String PARCELABLE_KEY = User.class.getName();

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
     * The URL for the profile image.
     */
    public String profileImageUrl;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User that = (User)o;
        return this.username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return this.username.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.email);
        dest.writeString(this.name);
        dest.writeString(this.username);
        dest.writeList(this.receivedLoves);
        dest.writeList(this.sentLoves);
    }

    protected User(Parcel in) {
        this.email = in.readString();
        this.name = in.readString();
        this.username = in.readString();
        this.receivedLoves = new ArrayList<Love>();
        in.readList(this.receivedLoves, List.class.getClassLoader());
        this.sentLoves = new ArrayList<Love>();
        in.readList(this.sentLoves, List.class.getClassLoader());
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
