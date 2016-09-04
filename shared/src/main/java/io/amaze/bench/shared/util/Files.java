/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amaze.bench.shared.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;

/**
 * Created on 3/16/16.
 */
public final class Files {

    private Files() {
        // Should not be instantiated
    }

    public static String readAndDelete(final String path) throws IOException {
        String content = read(path);

        File fileToDelete = new File(path);
        fileToDelete.deleteOnExit();
        if (!fileToDelete.delete()) {
            throw new IOException("Could not delete temporary file \"" + path + "\".");
        }
        return content;
    }

    public static String read(final String path) throws IOException {
        byte[] encoded = java.nio.file.Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.forName("UTF-8"));
    }

    public static void writeTo(File dest, String content) throws IOException {
        try (PrintWriter writer = new PrintWriter(dest, "UTF-8")) {
            writer.print(content);
        }
    }
}
