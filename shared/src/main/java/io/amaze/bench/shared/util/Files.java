/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

/**
 * Created on 3/16/16.
 */
public final class Files {

    private Files() {
        // Should not be instantiated
    }

    public static String readAndDelete(final String path) {
        String content;
        try {
            content = read(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        File fileToDelete = new File(path);
        if (!fileToDelete.delete()) {
            throw new IllegalStateException("Could not delete temporary file \"" + path + "\".");
        }
        return content;
    }

    public static String checkFilePath(@NotNull final String filePath) {
        requireNonNull(filePath);
        if (filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Path is empty.");
        }
        try {
            Path path = Paths.get(filePath);
            return path.toAbsolutePath().toString();
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("File path is invalid: \"" + filePath + "\"", e);
        }
    }

    public static String read(@NotNull final String path) throws IOException {
        requireNonNull(path);
        byte[] encoded = java.nio.file.Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.forName("UTF-8"));
    }

    public static void writeTo(@NotNull final File dest, @NotNull final String content) throws IOException {
        requireNonNull(dest);
        requireNonNull(content);

        try (PrintWriter writer = new PrintWriter(dest, "UTF-8")) {
            writer.print(content);
        }
    }
}
