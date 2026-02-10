package org.sully.d2.util;

import java.io.IOException;
import java.io.InputStream;

public class IOUtils {


    public static void readFully(InputStream in, byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = 0;
            try {
                count = in.read(b, off + n, len - n);
            } catch (Exception e) {
                throw new RuntimeException("Failed when reading input stream...", e);
            }

            if (count < 0) {
                throw new RuntimeException("End of Stream");
            }
            n += count;
        }
    }
}
