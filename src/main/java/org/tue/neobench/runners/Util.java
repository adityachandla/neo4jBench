package org.tue.neobench.runners;

import java.io.InputStream;
import java.util.Objects;

public class Util {

    public static InputStream getResourceInputStream(String fileName) {
        var inputStream = Util.class.getClassLoader()
                .getResourceAsStream(fileName);
        Objects.requireNonNull(inputStream);
        return inputStream;
    }
}
