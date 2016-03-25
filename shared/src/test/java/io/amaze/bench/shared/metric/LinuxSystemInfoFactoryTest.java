package io.amaze.bench.shared.metric;

import io.amaze.bench.shared.helper.FileHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static io.amaze.bench.shared.metric.LinuxSystemInfoFactory.UNKNOWN_INT_VALUE;
import static io.amaze.bench.shared.metric.LinuxSystemInfoFactory.UNKNOWN_STRING_VALUE;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class LinuxSystemInfoFactoryTest {

    private static final String TEST_VALID_CPUINFO_RESOURCE = "/valid-cpuinfo.txt";
    private static final String TEST_VALID_MEMINFO_RESOURCE = "/valid-meminfo.txt";

    private static final String TEST_INVALID_MEMINFO_CONTENT = //
            "MemFree:          956548 kB\n" +
                    "Buffers:          192272 kB\n" +
                    "Cached\n";

    private static final String TEST_CORRUPTED_MEMINFO_CONTENT = //
            "MemTotal:          956 548 kB\n" +
                    "Buffers:          192272 kB\n" +
                    "\n";

    private static final String TEST_INVALID_CPUINFO_CONTENT = //
            "processor\t: 3\n" +
                    "vendor_id\t: GenuineIntel\n" +
                    "cpu family\t: 6\n" +
                    "model\t\t: 42\n";
    private static final String DUMMY_FILE_NAME = "-DUMMY-";

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private String validCpuInfoPath;
    private String validMemInfoPath;

    @Before
    public void setUpTestResources() {
        validCpuInfoPath = this.getClass().getResource(TEST_VALID_CPUINFO_RESOURCE).getFile();
        validMemInfoPath = this.getClass().getResource(TEST_VALID_MEMINFO_RESOURCE).getFile();
    }

    @Test
    public void call_to_create_populates_system_info() {
        LinuxSystemInfoFactory infoFactory = new LinuxSystemInfoFactory(validCpuInfoPath, validMemInfoPath);
        SystemInfo sysInfo = infoFactory.create();

        assertFalse(sysInfo.getHostName().isEmpty());
        assertFalse(sysInfo.getOsName().isEmpty());
        assertFalse(sysInfo.getOsVersion().isEmpty());
        assertFalse(sysInfo.getProcArch().isEmpty());
        assertTrue(sysInfo.getProcCount() > 0);
    }

    @Test
    public void create_from_valid_cpu_info_file() {
        LinuxSystemInfoFactory infoFactory = new LinuxSystemInfoFactory(validCpuInfoPath, validMemInfoPath);
        SystemInfo sysInfo = infoFactory.create();

        assertThat(sysInfo.getProcessors().size(), is(4));

        for (ProcessorInfo processorInfo : sysInfo.getProcessors()) {
            assertThat(processorInfo.getModelName(), is("Intel(R) Core(TM) i5-2410M CPU @ 2.30GHz"));
            assertThat(processorInfo.getCacheSize(), is("3072 KB"));
            assertThat(processorInfo.getFrequency(), is("800.000"));
            assertThat(processorInfo.getCores(), is(2));
            assertThat(processorInfo.getProperties().size(), is(24));
        }
    }

    @Test
    public void create_from_incomplete_cpu_info_file() throws IOException {
        File file = folder.newFile();
        FileHelper.writeToFile(file, TEST_INVALID_CPUINFO_CONTENT);

        LinuxSystemInfoFactory infoFactory = new LinuxSystemInfoFactory(file.getAbsolutePath(), validMemInfoPath);
        SystemInfo sysInfo = infoFactory.create();

        assertThat(sysInfo.getProcessors().size(), is(1));

        ProcessorInfo processorInfo = sysInfo.getProcessors().get(0);
        assertThat(processorInfo.getModelName(), is(UNKNOWN_STRING_VALUE));
        assertThat(processorInfo.getCacheSize(), is(UNKNOWN_STRING_VALUE));
        assertThat(processorInfo.getFrequency(), is(UNKNOWN_STRING_VALUE));
        assertThat(processorInfo.getCores(), is(UNKNOWN_INT_VALUE));
        assertThat(processorInfo.getProperties().size(), is(4));
    }

    @Test
    public void create_from_nonexistent_cpu_info_file() throws IOException {
        LinuxSystemInfoFactory infoFactory = new LinuxSystemInfoFactory(DUMMY_FILE_NAME, validMemInfoPath);
        SystemInfo sysInfo = infoFactory.create();

        assertThat(sysInfo.getProcessors().size(), is(0));
    }

    @Test
    public void create_from_incomplete_mem_info_file() throws IOException {
        File file = folder.newFile();
        FileHelper.writeToFile(file, TEST_INVALID_MEMINFO_CONTENT);

        LinuxSystemInfoFactory infoFactory = new LinuxSystemInfoFactory(validCpuInfoPath, file.getAbsolutePath());
        SystemInfo sysInfo = infoFactory.create();

        assertThat(sysInfo.getMemoryInfo().getTotalMemoryKb(), is((long) UNKNOWN_INT_VALUE));
        assertThat(sysInfo.getMemoryInfo().getMemoryProperties().size(), is(2));
    }

    @Test
    public void create_from_nonexistent_mem_info_file() throws IOException {
        LinuxSystemInfoFactory infoFactory = new LinuxSystemInfoFactory(validCpuInfoPath, DUMMY_FILE_NAME);
        SystemInfo sysInfo = infoFactory.create();

        assertThat(sysInfo.getMemoryInfo().getTotalMemoryKb(), is((long) UNKNOWN_INT_VALUE));
        assertThat(sysInfo.getMemoryInfo().getMemoryProperties().isEmpty(), is(true));
    }

    @Test
    public void create_from_corrupted_mem_info_file() throws IOException {
        File file = folder.newFile();
        FileHelper.writeToFile(file, TEST_CORRUPTED_MEMINFO_CONTENT);

        LinuxSystemInfoFactory infoFactory = new LinuxSystemInfoFactory(validCpuInfoPath, file.getAbsolutePath());
        SystemInfo sysInfo = infoFactory.create();

        assertThat(sysInfo.getMemoryInfo().getTotalMemoryKb(), is((long) UNKNOWN_INT_VALUE));
        assertThat(sysInfo.getMemoryInfo().getMemoryProperties().size(), is(2));
    }

}