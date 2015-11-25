package org.ometa.lovemonster;

import android.support.annotation.NonNull;
import android.util.Log;

import cz.msebera.android.httpclient.annotation.ThreadSafe;


/**
 * Provides a thin wrapper over the logging functionality in Android.
 */
@ThreadSafe
public class Logger {

    /**
     * Used to enable/disable debug logging.  Set to false to disable logging.
     */
    public static boolean isDebugEnabled = true;

    /**
     * The tag to use on logging messages.
     */
    protected final String tag;

    /**
     * Constructs a new logger to be used inside the specified class.
     *
     * @param loggedClass
     *      the class in which logging will occur
     */
    public Logger(@NonNull final Class loggedClass) {
        this.tag = loggedClass.getSimpleName();
    }


    /**
     * Logs the message at DEBUG level.
     *
     * @param message
     *      the message to log
     */
    public void debug(final String message) {
        debug(message, null);
    }

    /**
     * Logs the message and throwable at DEBUG level.
     *
     * @param message
     *      the message to log
     * @param throwable
     *      the throwable to log
     */
    public void debug(String message, final Throwable throwable) {
        if (!isDebugEnabled) {
            return;
        }

        if (throwable != null) {
            message = new StringBuilder(message)
                    .append(" throwableClass=")
                    .append(throwable.getClass().getSimpleName())
                    .append(" throwableMessage=")
                    .append(throwable.getMessage())
                    .toString();
        }

        Log.d(tag, message);
    }

}
