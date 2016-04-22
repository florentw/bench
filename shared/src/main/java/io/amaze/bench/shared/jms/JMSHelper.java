package io.amaze.bench.shared.jms;

import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.NotNull;

import javax.jms.BytesMessage;
import java.io.*;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class JMSHelper {

    private JMSHelper() {
        // Helper class
    }

    public static Serializable objectFromMsg(@NotNull final BytesMessage message) throws IOException {
        try {
            byte[] rawData = new byte[(int) message.getBodyLength()];
            message.readBytes(rawData);
            return convertFromBytes(rawData);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static byte[] convertToBytes(@NotNull final Serializable object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    @VisibleForTesting
    static Serializable convertFromBytes(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return (Serializable) in.readObject();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
