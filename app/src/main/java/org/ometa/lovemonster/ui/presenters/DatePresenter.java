package org.ometa.lovemonster.ui.presenters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by devin on 12/2/15.
 * Inspired by http://stackoverflow.com/questions/19409035/custom-format-for-relative-time-span
 */
public class DatePresenter {

    public static String shortRelativeElapsedFrom(Calendar then) {
        Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        now.setTime(new Date());

        // Get the represented date in milliseconds
        long nowMs = now.getTimeInMillis();
        long thenMs = then.getTimeInMillis();

        // Calculate difference in milliseconds
        long diff = nowMs - thenMs;

        // Calculate difference in seconds
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);

        // HACK: I cannot get the timezone conversion to work correctly yet. For new loves, the
        // time they were created is always greater than the "current" time reported in android.
        // If we have negative time, set it to 1 for now until we fix it.
        // todo: fix this situation
        if (diffMinutes < 0) {
            diffMinutes = 1;
        }

        if (diffMinutes < 60) {
            return diffMinutes + "m";
        } else if (diffHours < 24) {
            return diffHours + "h";
        } else if (diffDays < 7) {
            return diffDays + "d";
        } else {
            SimpleDateFormat toDate = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
            return toDate.format(then.getTime());
        }
    }
}