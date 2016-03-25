package io.amaze.bench.shared.helper;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class SystemHelper {

    private static final String OS_NAME_LINUX = "Linux";

    public static boolean isLinux() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        return osBean.getName().contains(OS_NAME_LINUX);
    }

}
