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