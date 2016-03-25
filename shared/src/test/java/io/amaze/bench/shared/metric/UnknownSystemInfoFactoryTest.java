package io.amaze.bench.shared.metric;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class UnknownSystemInfoFactoryTest {

    @Test
    public void call_to_create_populates_system_info() {
        UnknownSystemInfoFactory infoFactory = new UnknownSystemInfoFactory();
        SystemInfo sysInfo = infoFactory.create();

        assertFalse(sysInfo.getHostName().isEmpty());
        assertFalse(sysInfo.getOsName().isEmpty());
        assertFalse(sysInfo.getOsVersion().isEmpty());
        assertFalse(sysInfo.getProcArch().isEmpty());
        assertTrue(sysInfo.getProcCount() > 0);
        assertTrue(sysInfo.getProcessors().isEmpty());
    }

}