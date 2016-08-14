package io.amaze.bench.shared.metric;

import java.util.Collections;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class UnknownSystemConfigFactory extends AbstractSystemConfigFactory {

    @Override
    public SystemConfig create() {
        return new SystemConfig(getHostName(),
                                getNbProcs(),
                                getOsArch(),
                                getOsName(),
                                getOsVersion(),
                                getMemoryInfo(),
                                Collections.<ProcessorConfig>emptyList());
    }

    private MemoryConfig getMemoryInfo() {
        return new MemoryConfig(UNKNOWN_INT_VALUE, Collections.<String, String>emptyMap());
    }

}
