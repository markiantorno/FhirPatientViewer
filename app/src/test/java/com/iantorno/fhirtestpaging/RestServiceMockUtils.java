package com.iantorno.fhirtestpaging;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by miantorno on 1/13/17.
 */
public class RestServiceMockUtils {

    public static final long CONNECTION_TIMEOUT_SHORT = 2;
    public static final long CONNECTION_TIMEOUT_MED = 5;
    public static final long CONNECTION_TIMEOUT_LONG = 10;

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(ClassLoader loader, String filePath) {
        final InputStream stream = loader.getResourceAsStream(filePath);

        String ret = null;
        try {
            ret = convertStreamToString(stream);
            //Make sure you close all streams.
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


}