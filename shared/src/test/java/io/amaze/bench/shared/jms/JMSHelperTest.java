package io.amaze.bench.shared.jms;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.io.IOException;
import java.io.Serializable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 3/27/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class JMSHelperTest {

    private static final String DUMMY = "TEST";

    public static BytesMessage createTestBytesMessage(final byte[] data) throws JMSException {
        BytesMessage msg = mock(BytesMessage.class);
        when(msg.getBodyLength()).thenReturn((long) data.length);
        when(msg.readBytes(any(byte[].class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                byte[] out = (byte[]) invocation.getArguments()[0];
                System.arraycopy(data, 0, out, 0, out.length);
                return null;
            }
        }).thenReturn(data.length);
        return msg;
    }

    @Test
    public void serialize_deserialize() throws IOException {
        String expected = DUMMY;
        byte[] data = JMSHelper.convertToBytes(expected);
        Serializable actual = JMSHelper.convertFromBytes(data);

        assertThat((String) actual, is(expected));
    }

    @Test
    public void serialize_deserialize_through_bytes_message() throws IOException, JMSException {
        String expected = DUMMY;
        byte[] data = JMSHelper.convertToBytes(expected);
        BytesMessage bytesMessage = createTestBytesMessage(data);
        Serializable actual = JMSHelper.objectFromMsg(bytesMessage);

        assertThat((String) actual, is(expected));
    }

    @Test(expected = IOException.class)
    public void serialize_deserialize_corrupted_data_through_bytes_message() throws IOException, JMSException {
        BytesMessage bytesMessage = createTestBytesMessage(new byte[3]);
        JMSHelper.objectFromMsg(bytesMessage);
    }

    @Test
    public void serialize_deserialize_null() throws IOException {
        byte[] data = JMSHelper.convertToBytes(null);
        Serializable actual = JMSHelper.convertFromBytes(data);

        assertThat((String) actual, is((String) null));
    }

    @Test(expected = IOException.class)
    public void deserialize_corrupted_data_throws() throws IOException {
        JMSHelper.convertFromBytes(new byte[3]);
    }

}