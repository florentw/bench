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
package io.amaze.bench.shared.helper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 3/19/16.
 */
public final class FileHelperTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void write_read_file() throws IOException {
        File file = folder.newFile();

        String expected = "Test";
        FileHelper.writeToFile(file, expected);

        String actual = FileHelper.readFile(file.getPath());
        assertThat(actual, is(expected));
    }

    @Test
    public void read_file_and_delete() throws IOException {
        File file = folder.newFile();

        String expected = "Test";
        FileHelper.writeToFile(file, expected);

        String actual = FileHelper.readFileAndDelete(file.getPath());
        assertThat(actual, is(expected));
        assertThat(file.exists(), is(false));
    }

}