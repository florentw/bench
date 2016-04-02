package io.amaze.bench.shared.helper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created on 3/16/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class FileHelper {

    private FileHelper() {
        // Should not be instantiated
    }

    public static String readFileAndDelete(final String path) throws IOException {
        String content = readFile(path);

        File tmpConfigFile = new File(path);
        tmpConfigFile.deleteOnExit();
        if (!tmpConfigFile.delete()) {
            throw new IOException("Could not delete temporary file \"" + path + "\".");
        }
        return content;
    }

    public static String readFile(final String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.forName("UTF-8"));
    }

    public static void writeToFile(File dest, String content) throws IOException {
        try (PrintWriter writer = new PrintWriter(dest, "UTF-8")) {
            writer.print(content);
        }
    }
}
