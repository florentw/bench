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
package io.amaze.bench.shared.jms;

import com.google.common.annotations.VisibleForTesting;

import javax.jms.BytesMessage;
import javax.validation.constraints.NotNull;
import java.io.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/3/16.
 */
public final class JMSHelper {

    private JMSHelper() {
        // Helper class
    }

    /**
     * Deserialize a JMS message containing a Java serialized abject.
     *
     * @param message JMS input message with a serialized payload
     * @param <T>     Type of the serialized object
     * @return The de-serialized object
     */
    public static <T extends Serializable> T objectFromMsg(@NotNull final BytesMessage message) throws IOException {
        checkNotNull(message);

        try {
            byte[] rawData = new byte[(int) message.getBodyLength()];
            message.readBytes(rawData);
            return (T) convertFromBytes(rawData); // NOSONAR
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Serialize an object to a byte buffer (payload to be de-serialized with {@link #objectFromMsg(BytesMessage)}).
     *
     * @param object A serialized object
     * @return The byte buffer containing the serialized object
     */
    public static byte[] convertToBytes(@NotNull final Serializable object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    @VisibleForTesting
    static <T extends Serializable> T convertFromBytes(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return (T) in.readObject(); // NOSONAR
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
