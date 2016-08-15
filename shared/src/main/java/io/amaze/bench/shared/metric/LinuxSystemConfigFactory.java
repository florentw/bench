/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amaze.bench.shared.metric;

import com.google.common.base.Splitter;
import io.amaze.bench.shared.helper.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/20/16.
 */
final class LinuxSystemConfigFactory extends AbstractSystemConfigFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LinuxSystemConfigFactory.class);

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

    LinuxSystemConfigFactory() {
        this(CPUINFO_DEFAULT_PATH, MEMINFO_DEFAULT_PATH);
    }

    LinuxSystemConfigFactory(String cpuInfoFilePath, String memInfoFilePath) {
        checkNotNull(cpuInfoFilePath);
        checkNotNull(memInfoFilePath);

        cpuInfoContent = readFileContent(cpuInfoFilePath);
        memInfoContent = readFileContent(memInfoFilePath);
    }

    @Override
    public SystemConfig create() {
        return new SystemConfig(getHostName(),
                                getNbProcs(),
                                getOsArch(),
                                getOsName(),
                                getOsVersion(),
                                createMemoryConfig(),
                                createProcessorConfigList());
    }

    private String readFileContent(final String filePath) {
        try {
            return FileHelper.readFile(filePath);
        } catch (IOException e) {
            LOG.info("Could not read file: " + filePath, e);
            return null;
        }
    }

    private List<ProcessorConfig> createProcessorConfigList() {
        List<Map<String, String>> splitCpuContent;
        if (cpuInfoContent != null) {
            // Get a map of parser CPU properties for each detected CPU.
            splitCpuContent = splitCpuContent(cpuInfoContent);
        } else {
            splitCpuContent = Collections.emptyList();
        }

        // Use the parsed CPU properties to create ProcessorConfig objects
        return createProcessorsInfoList(splitCpuContent);
    }

    private List<ProcessorConfig> createProcessorsInfoList(final List<Map<String, String>> splitCpuContent) {
        List<ProcessorConfig> processors = new ArrayList<>();

        for (Map<String, String> cpuProperties : splitCpuContent) {
            String cores = cpuProperties.get(CPUINFO_PROP_CPU_CORES);
            int coresCount = readIntProperty(cores);

            String modelName = cpuProperties.get(CPUINFO_PROP_MODEL_NAME);
            modelName = readStringProperty(modelName);

            String cacheSize = cpuProperties.get(CPUINFO_PROP_CACHE_SIZE);
            cacheSize = readStringProperty(cacheSize);

            String frequency = cpuProperties.get(CPUINFO_PROP_CPU_MHZ);
            frequency = readStringProperty(frequency);

            processors.add(new ProcessorConfig(modelName, coresCount, frequency, cacheSize, cpuProperties));
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

    private List<Map<String, String>> splitCpuContent(final String cpuInfoContent) {
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

    private Map<String, String> splitMemoryContent() {
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

    private long parseTotalMemoryKb(final Map<String, String> memoryProperties) {
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

    private MemoryConfig createMemoryConfig() {
        Map<String, String> props = splitMemoryContent();
        long totalMemoryKb = parseTotalMemoryKb(props);
        return new MemoryConfig(totalMemoryKb, props);
    }

}
