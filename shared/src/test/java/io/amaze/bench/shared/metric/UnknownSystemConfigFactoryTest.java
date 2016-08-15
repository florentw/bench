package io.amaze.bench.shared.metric;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/20/16.
 */
public final class UnknownSystemConfigFactoryTest {

    @Test
    public void call_to_create_populates_system_info() {
        UnknownSystemConfigFactory infoFactory = new UnknownSystemConfigFactory();
        SystemConfig sysConfig = infoFactory.create();

        assertFalse(sysConfig.getHostName().isEmpty());
        assertFalse(sysConfig.getOsName().isEmpty());
        assertFalse(sysConfig.getOsVersion().isEmpty());
        assertFalse(sysConfig.getProcArch().isEmpty());
        assertTrue(sysConfig.getProcCount() > 0);
        assertTrue(sysConfig.getProcessors().isEmpty());
    }

}