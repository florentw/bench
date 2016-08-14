package io.amaze.bench.shared.metric;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * Created on 3/25/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class SystemInfosTest {

    @Test
    public void creates_non_null_system_info() {
        SystemInfo systemInfo = SystemInfos.get();
        assertNotNull(systemInfo);
    }

    @Test
    public void second_call_returns_same_instance() {
        SystemInfo first = SystemInfos.get();
        SystemInfo second = SystemInfos.get();
        assertSame(first, second);
    }

}