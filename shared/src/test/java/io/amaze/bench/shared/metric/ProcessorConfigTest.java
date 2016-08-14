package io.amaze.bench.shared.metric;

import com.google.common.testing.NullPointerTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

/**
 * Created on 8/15/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public final class ProcessorConfigTest {

    @Test
    public void null_parameters_are_invalid() {
        ProcessorConfig processorConfig = new ProcessorConfig("", 1, "", "", new HashMap<String, String>());
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ProcessorConfig.class);
        tester.testAllPublicInstanceMethods(processorConfig);
    }

}