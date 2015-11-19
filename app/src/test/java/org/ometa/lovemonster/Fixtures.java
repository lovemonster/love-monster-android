package org.ometa.lovemonster;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utility class which loads json fixture files and returns json objects.
 */
public class Fixtures {

    /**
     * The root fixture directory which contains all the fixtures.
     */
    private static final String FIXTURE_ROOT_DIR = "fixtures/";

    /**
     * Loads the data from the specified fixture file.
     * @param fixturePath
     *      the relative path to the fixture file
     * @return
     *      a parsed JSONObject from the fixture
     */
    public static JSONObject getJsonObject(final String fixturePath) {
        final InputStream fixtureStream = Fixtures.class.getClassLoader().getResourceAsStream(FIXTURE_ROOT_DIR + fixturePath);
        if (fixtureStream == null) {
            throw new IllegalArgumentException("fixture file " + fixturePath + " cannot be found");
        }

        final BufferedReader reader = new BufferedReader(new InputStreamReader(fixtureStream));
        final StringBuilder builder = new StringBuilder();

        String line = "";
        try {
            do {
                builder.append(line);
                line = reader.readLine();
            } while(line != null);

            fixtureStream.close();

            return new JSONObject(builder.toString());
        } catch (final IOException|JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a {@link Calendar} in UTC for the specified date and time.
     * @param year
     *      the year for the date
     * @param month
     *      the month for the date
     * @param dayOfMonth
     *      the dayOfMonth of the month for the date
     * @param hour
     *      the hour for the time
     * @param minute
     *      the minute for the time
     * @param second
     *      the second for the time
     * @return
     *      the datetime
     */
    public static Calendar getDatetime(final int year, final int month, final int dayOfMonth, final int hour, final int minute, final int second) {
        final Calendar datetime = Calendar.getInstance();

        datetime.clear();
        datetime.setTimeZone(TimeZone.getTimeZone("UTC"));
        datetime.set(year, month - 1, dayOfMonth, hour, minute, second);

        return datetime;
    }
}