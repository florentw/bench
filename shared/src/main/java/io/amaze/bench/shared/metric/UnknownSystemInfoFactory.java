package io.amaze.bench.shared.metric;

import java.util.Collections;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
class UnknownSystemInfoFactory extends AbstractSystemInfoFactory {

    @Override
    public SystemInfo create() {
        return new SystemInfo(getHostName(),
                              getNbProcs(),
                              getOsArch(),
                              getOsName(),
                              getOsVersion(),
                              getMemoryInfo(),
                              Collections.<ProcessorInfo>emptyList());
    }

    private MemoryInfo getMemoryInfo() {
        return new MemoryInfo(UNKNOWN_INT_VALUE, Collections.<String, String>emptyMap());
    }

}
