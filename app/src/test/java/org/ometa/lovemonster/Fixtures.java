package org.ometa.lovemonster;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
}