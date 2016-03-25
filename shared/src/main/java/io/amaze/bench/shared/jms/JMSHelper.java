package io.amaze.bench.shared.jms;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.validation.constraints.NotNull;
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

    public static Serializable objectFromMsg(@NotNull final BytesMessage message) throws JMSException, IOException, ClassNotFoundException {
        byte[] rawData = new byte[(int) message.getBodyLength()];
        message.readBytes(rawData);
        return convertFromBytes(rawData);
    }

    public static byte[] convertToBytes(@NotNull final Serializable object) throws IOException, ClassNotFoundException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    private static Serializable convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return (Serializable) in.readObject();
        }
    }
}
