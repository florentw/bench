package io.amaze.bench.shared.metric;

import com.google.common.testing.NullPointerTester;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static io.amaze.bench.shared.helper.FileHelper.writeToFile;
import static io.amaze.bench.shared.metric.LinuxSystemConfigFactory.UNKNOWN_INT_VALUE;
import static io.amaze.bench.shared.metric.LinuxSystemConfigFactory.UNKNOWN_STRING_VALUE;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/20/16.
 */
public final class LinuxSystemConfigFactoryTest {

    private static final String TEST_VALID_CPUINFO_RESOURCE = "/valid-cpuinfo.txt";
    private static final String TEST_VALID_MEMINFO_RESOURCE = "/valid-meminfo.txt";

    private static final String TEST_INVALID_MEMINFO_CONTENT = //
            "MemFree:          956548 kB\n" + //
                    "Buffers:          192272 kB\n" + //
                    "Cached\n";

    private static final String TEST_CORRUPTED_MEMINFO_CONTENT = //
            "MemTotal:          956 548 kB\n" + //
                    "Buffers:          192272 kB\n" + //
                    "\n";

    private static final String TEST_INVALID_CPUINFO_CONTENT = //
            "processor\t: 3\n" + //
                    "vendor_id\t: GenuineIntel\n" + //
                    "cpu family\t: 6\n" + //
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
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(LinuxSystemConfigFactory.class);
        tester.testAllPublicInstanceMethods(validLinuxSystemConfigFactory());
    }

    @Test
    public void call_to_create_populates_system_config() {
        LinuxSystemConfigFactory configFactory = validLinuxSystemConfigFactory();
        SystemConfig sysConfig = configFactory.create();

        assertFalse(sysConfig.getHostName().isEmpty());
        assertFalse(sysConfig.getOsName().isEmpty());
        assertFalse(sysConfig.getOsVersion().isEmpty());
        assertFalse(sysConfig.getProcArch().isEmpty());
        assertTrue(sysConfig.getProcCount() > 0);
    }

    @Test
    public void create_from_valid_cpu_info_file() {
        LinuxSystemConfigFactory configFactory = validLinuxSystemConfigFactory();
        SystemConfig sysConfig = configFactory.create();

        assertThat(sysConfig.getProcessors().size(), is(4));

        for (ProcessorConfig processorConfig : sysConfig.getProcessors()) {
            assertThat(processorConfig.getModelName(), is("Intel(R) Core(TM) i5-2410M CPU @ 2.30GHz"));
            assertThat(processorConfig.getCacheSize(), is("3072 KB"));
            assertThat(processorConfig.getFrequency(), is("800.000"));
            assertThat(processorConfig.getCores(), is(2));
            assertThat(processorConfig.getProperties().size(), is(24));
        }
    }

    @Test
    public void create_from_incomplete_cpu_info_file() throws IOException {
        File file = folder.newFile();
        writeToFile(file, TEST_INVALID_CPUINFO_CONTENT);

        LinuxSystemConfigFactory configFactory = new LinuxSystemConfigFactory(file.getAbsolutePath(), validMemInfoPath);
        SystemConfig sysConfig = configFactory.create();

        assertThat(sysConfig.getProcessors().size(), is(1));

        ProcessorConfig processorConfig = sysConfig.getProcessors().get(0);
        assertThat(processorConfig.getModelName(), is(UNKNOWN_STRING_VALUE));
        assertThat(processorConfig.getCacheSize(), is(UNKNOWN_STRING_VALUE));
        assertThat(processorConfig.getFrequency(), is(UNKNOWN_STRING_VALUE));
        assertThat(processorConfig.getCores(), is(UNKNOWN_INT_VALUE));
        assertThat(processorConfig.getProperties().size(), is(4));
    }

    @Test
    public void create_from_nonexistent_cpu_info_file() throws IOException {
        LinuxSystemConfigFactory configFactory = new LinuxSystemConfigFactory(DUMMY_FILE_NAME, validMemInfoPath);
        SystemConfig sysConfig = configFactory.create();

        assertThat(sysConfig.getProcessors().size(), is(0));
    }

    @Test
    public void create_from_incomplete_mem_info_file() throws IOException {
        File file = folder.newFile();
        writeToFile(file, TEST_INVALID_MEMINFO_CONTENT);

        LinuxSystemConfigFactory configFactory = new LinuxSystemConfigFactory(validCpuInfoPath, file.getAbsolutePath());
        SystemConfig sysConfig = configFactory.create();

        assertThat(sysConfig.getMemoryConfig().getTotalMemoryKb(), is((long) UNKNOWN_INT_VALUE));
        assertThat(sysConfig.getMemoryConfig().getMemoryProperties().size(), is(2));
    }

    @Test
    public void create_from_nonexistent_mem_info_file() throws IOException {
        LinuxSystemConfigFactory configFactory = new LinuxSystemConfigFactory(validCpuInfoPath, DUMMY_FILE_NAME);
        SystemConfig sysConfig = configFactory.create();

        assertThat(sysConfig.getMemoryConfig().getTotalMemoryKb(), is((long) UNKNOWN_INT_VALUE));
        assertThat(sysConfig.getMemoryConfig().getMemoryProperties().isEmpty(), is(true));
    }

    @Test
    public void create_from_corrupted_mem_info_file() throws IOException {
        File file = folder.newFile();
        writeToFile(file, TEST_CORRUPTED_MEMINFO_CONTENT);

        LinuxSystemConfigFactory configFactory = new LinuxSystemConfigFactory(validCpuInfoPath, file.getAbsolutePath());
        SystemConfig sysConfig = configFactory.create();

        assertThat(sysConfig.getMemoryConfig().getTotalMemoryKb(), is((long) UNKNOWN_INT_VALUE));
        assertThat(sysConfig.getMemoryConfig().getMemoryProperties().size(), is(2));
    }

    private LinuxSystemConfigFactory validLinuxSystemConfigFactory() {
        return new LinuxSystemConfigFactory(validCpuInfoPath, validMemInfoPath);
    }

}