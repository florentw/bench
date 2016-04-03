package io.amaze.bench.shared.metric;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class SystemInfo implements Serializable {

    private final String hostName;
    private final int procCount;
    private final String procArch;
    private final String osName;
    private final String osVersion;

    private final List<ProcessorInfo> processors;
    private final MemoryInfo memoryInfo;

    SystemInfo(@NotNull final String hostName,
               @NotNull final int procCount,
               @NotNull final String procArch,
               @NotNull final String osName,
               @NotNull final String osVersion,
               @NotNull final MemoryInfo memoryInfo,
               @NotNull final List<ProcessorInfo> processors) {

        this.hostName = hostName;
        this.procCount = procCount;
        this.procArch = procArch;
        this.osName = osName;
        this.osVersion = osVersion;
        this.memoryInfo = memoryInfo;
        this.processors = processors;
    }

    public String getHostName() {
        return hostName;
    }

    public int getProcCount() {
        return procCount;
    }

    public String getProcArch() {
        return procArch;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public MemoryInfo getMemoryInfo() {
        return memoryInfo;
    }

    public List<ProcessorInfo> getProcessors() {
        return processors;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "hostName='" + hostName + '\'' +
                ", procCount=" + procCount +
                ", procArch='" + procArch + '\'' +
                ", osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", processors=" + processors +
                ", memoryInfo=" + memoryInfo +
                '}';
    }
}
