package io.amaze.bench.shared.metric;

import com.google.common.testing.NullPointerTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 8/15/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public final class SystemConfigTest {

    private static final MemoryConfig MEMORY_CONFIG = new MemoryConfig(0, new HashMap<String, String>());
    public static final SystemConfig DUMMY_CONFIG = new SystemConfig("",
                                                                     1,
                                                                     "",
                                                                     "",
                                                                     "",
                                                                     MEMORY_CONFIG,
                                                                     new ArrayList<ProcessorConfig>());

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.setDefault(MemoryConfig.class, MEMORY_CONFIG);
        tester.testAllPublicConstructors(SystemConfig.class);
        tester.testAllPublicInstanceMethods(DUMMY_CONFIG);
    }

}