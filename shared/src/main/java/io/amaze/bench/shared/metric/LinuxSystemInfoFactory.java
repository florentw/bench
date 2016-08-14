package io.amaze.bench.shared.metric;

import com.google.common.base.Splitter;
import io.amaze.bench.shared.helper.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class LinuxSystemInfoFactory extends AbstractSystemInfoFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LinuxSystemInfoFactory.class);

    private static final String MEMINFO_DEFAULT_PATH = "/proc/meminfo";
    private static final String MEMINFO_PROP_MEM_TOTAL = "MemTotal";

    private static final String CPUINFO_DEFAULT_PATH = "/proc/cpuinfo";
    private static final String CPUINFO_PROP_CPU_CORES = "cpu cores";
    private static final String CPUINFO_PROP_MODEL_NAME = "model name";
    private static final String CPUINFO_PROP_CACHE_SIZE = "cache size";
    private static final String CPUINFO_PROP_CPU_MHZ = "cpu MHz";

    private static final String EOL_PATTERN = "\\r?\\n";

    private final String cpuInfoContent;
    private final String memInfoContent;

    LinuxSystemInfoFactory() {
        this(CPUINFO_DEFAULT_PATH, MEMINFO_DEFAULT_PATH);
    }

    LinuxSystemInfoFactory(String cpuInfoFilePath, String memInfoFilePath) {
        cpuInfoContent = readFileContent(cpuInfoFilePath);
        memInfoContent = readFileContent(memInfoFilePath);
    }

    @Override
    public SystemInfo create() {
        return new SystemInfo(getHostName(),
                              getNbProcs(),
                              getOsArch(),
                              getOsName(),
                              getOsVersion(),
                              getMemoryInfo(),
                              getProcessorsInfo());
    }

    private String readFileContent(final String filePath) {
        try {
            return FileHelper.readFile(filePath);
        } catch (IOException e) {
            LOG.info("Could not read file: " + filePath, e);
            return null;
        }
    }

    private List<ProcessorInfo> getProcessorsInfo() {
        List<Map<String, String>> splitCpuContent;
        if (cpuInfoContent != null) {
            // Get a map of parser CPU properties for each detected CPU.
            splitCpuContent = getSplitCpuContent(cpuInfoContent);
        } else {
            splitCpuContent = Collections.emptyList();
        }

        // Use the parsed CPU properties to create ProcessorInfo objects
        return createProcessorsInfoList(splitCpuContent);
    }

    private List<ProcessorInfo> createProcessorsInfoList(final List<Map<String, String>> splitCpuContent) {
        List<ProcessorInfo> processors = new ArrayList<>();

        for (Map<String, String> cpuProperties : splitCpuContent) {
            String cores = cpuProperties.get(CPUINFO_PROP_CPU_CORES);
            int coresCount = readIntProperty(cores);

            String modelName = cpuProperties.get(CPUINFO_PROP_MODEL_NAME);
            modelName = readStringProperty(modelName);

            String cacheSize = cpuProperties.get(CPUINFO_PROP_CACHE_SIZE);
            cacheSize = readStringProperty(cacheSize);

            String frequency = cpuProperties.get(CPUINFO_PROP_CPU_MHZ);
            frequency = readStringProperty(frequency);

            processors.add(new ProcessorInfo(modelName, coresCount, frequency, cacheSize, cpuProperties));
        }

        return processors;
    }

    private String readStringProperty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNKNOWN_STRING_VALUE;
        }
        return value;
    }

    private int readIntProperty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNKNOWN_INT_VALUE;
        }
        return Integer.parseInt(value);
    }

    private List<Map<String, String>> getSplitCpuContent(final String cpuInfoContent) {
        Iterable<String> lineSplitter = Splitter.onPattern(EOL_PATTERN).trimResults().split(cpuInfoContent);
        List<Map<String, String>> splitCpuContent = new ArrayList<>();

        Map<String, String> currentProcessor = new TreeMap<>();
        for (String line : lineSplitter) {
            if (line.isEmpty()) {
                if (currentProcessor.isEmpty()) {
                    continue;
                }

                splitCpuContent.add(currentProcessor);
                currentProcessor = new HashMap<>();
            } else {
                String[] splitLine = line.split(":");
                if (splitLine.length == 2) {
                    currentProcessor.put(splitLine[0].trim(), splitLine[1].trim());
                }
            }
        }
        return splitCpuContent;
    }

    private Map<String, String> getSplitMemoryContent() {
        if (memInfoContent == null) {
            return Collections.emptyMap();
        }

        Iterable<String> lines = Splitter.onPattern(EOL_PATTERN).trimResults().split(memInfoContent);
        Map<String, String> properties = new HashMap<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] split = line.split(":");
            if (split.length == 2) {
                properties.put(split[0].trim(), split[1].trim());
            }
        }
        return properties;
    }

    private long getTotalMemoryKb(final Map<String, String> memoryProperties) {
        String memTotal = memoryProperties.get(MEMINFO_PROP_MEM_TOTAL);
        if (memTotal == null || memTotal.trim().isEmpty()) {
            return UNKNOWN_INT_VALUE;
        } else {
            String[] split = memTotal.split(" ");
            if (split.length == 2) {
                return Long.parseLong(split[0]);
            }
            return UNKNOWN_INT_VALUE;
        }
    }

    private MemoryInfo getMemoryInfo() {
        Map<String, String> props = getSplitMemoryContent();
        long totalMemoryKb = getTotalMemoryKb(props);
        return new MemoryInfo(totalMemoryKb, props);
    }

}
