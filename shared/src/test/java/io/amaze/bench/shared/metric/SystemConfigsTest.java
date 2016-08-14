package io.amaze.bench.shared.metric;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * Created on 3/25/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class SystemConfigsTest {

    @Test
    public void creates_non_null_system_info() {
        SystemConfig systemConfig = SystemConfigs.get();
        assertNotNull(systemConfig);
    }

    @Test
    public void second_call_returns_same_instance() {
        SystemConfig first = SystemConfigs.get();
        SystemConfig second = SystemConfigs.get();
        assertSame(first, second);
    }

}