package io.amaze.bench.shared.metric;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
abstract class AbstractSystemConfigFactory implements SystemConfigFactory {

    static final String UNKNOWN_STRING_VALUE = "[Unknown]";
    static final int UNKNOWN_INT_VALUE = -1;

    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    final int getNbProcs() {
        return osBean.getAvailableProcessors();
    }

    final String getOsArch() {
        return osBean.getArch();
    }

    final String getOsName() {
        return osBean.getName();
    }

    final String getOsVersion() {
        return osBean.getVersion();
    }

    final String getHostName() {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignore) { // NOSONAR
            hostName = UNKNOWN_STRING_VALUE;
        }
        return hostName;
    }


}
