package io.amaze.bench.shared.metric;

import io.amaze.bench.shared.helper.SystemHelper;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class SystemInfos {

    private static final SystemInfo SYSTEM_INFO;

    static {
        SystemInfoFactory factory;
        if (SystemHelper.isLinux()) {
            factory = new LinuxSystemInfoFactory();
        } else {
            factory = new UnknownSystemInfoFactory();
        }

        SYSTEM_INFO = factory.create();
    }

    private SystemInfos() {
        // Should not be instantiated
    }

    public static SystemInfo get() {
        return SYSTEM_INFO;
    }

}
