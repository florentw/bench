package io.amaze.bench.shared.metric;

import io.amaze.bench.shared.helper.SystemHelper;

/**
 * Provides an instance of {@link SystemConfig}, populated with the configuration of the current box.
 */
public final class SystemConfigs {

    private static final SystemConfig SYSTEM_INFO = createSystemInfoInstance();

    private SystemConfigs() {
        // Should not be instantiated
    }

    public static SystemConfig get() {
        return SYSTEM_INFO;
    }

    private static SystemConfig createSystemInfoInstance() {
        SystemConfigFactory factory;
        if (SystemHelper.isLinux()) {
            factory = new LinuxSystemConfigFactory();
        } else {
            factory = new UnknownSystemConfigFactory();
        }

        return factory.create();
    }

}
