package io.amaze.bench.shared.metric;

import com.google.common.testing.NullPointerTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class MemoryConfigTest {

    @Test
    public void null_parameters_are_invalid() {
        MemoryConfig memoryConfig = new MemoryConfig(1L, new HashMap<String, String>());
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(MemoryConfig.class);
        tester.testAllPublicInstanceMethods(memoryConfig);
    }

}