/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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
package io.amaze.bench.cluster.jgroups;

import io.amaze.bench.shared.jgroups.JgroupsEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.util.Util;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Throwables.propagate;
import static java.util.Objects.requireNonNull;

/**
 * Created on 10/1/16.
 */
public class JgroupsSender {

    private static final Logger log = LogManager.getLogger();

    private final JChannel channel;

    public JgroupsSender(@NotNull final JChannel channel) {
        this.channel = requireNonNull(channel);
    }

    public void broadcast(@NotNull final Serializable message) {
        requireNonNull(message);
        log.debug("Broadcasting {} on {}", message, channel);

        try {
            channel.send(null, Util.objectToByteBuffer(message));
        } catch (Exception e) { // NOSONAR - No choice here
            throw propagate(e);
        }
    }

    public void sendToEndpoint(JgroupsEndpoint endpoint, Serializable message) {
        requireNonNull(endpoint);
        requireNonNull(message);

        try {
            channel.send(endpoint.getAddress(), Util.objectToByteBuffer(message));
        } catch (Exception e) { // NOSONAR - No choice here
            throw propagate(e);
        }
    }

}
