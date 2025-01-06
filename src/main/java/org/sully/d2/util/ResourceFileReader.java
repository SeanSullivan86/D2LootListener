package org.sully.d2.util;

import java.io.InputStream;

public class ResourceFileReader {

    public static InputStream getProjectResourceFileAsInputStream(String path) {
        InputStream inputStream = ResourceFileReader.class
                .getClassLoader()
                .getResourceAsStream(path);

        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot open resource file : " + path);
        }
        return inputStream;
    }
}
