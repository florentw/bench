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

import com.google.common.testing.NullPointerTester;
import org.junit.Test;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import static io.amaze.bench.shared.jms.JMSHelper.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 3/27/16.
 */
public final class JMSHelperTest {

    private static final String DUMMY = "TEST";

    public static BytesMessage createTestBytesMessage(final byte[] data) throws JMSException {
        BytesMessage msg = mock(BytesMessage.class);
        when(msg.getBodyLength()).thenReturn((long) data.length);
        when(msg.readBytes(any(byte[].class))).thenAnswer(invocation -> {
            byte[] out = (byte[]) invocation.getArguments()[0];
            System.arraycopy(data, 0, out, 0, out.length);
            return null;
        }).thenReturn(data.length);
        return msg;
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicStaticMethods(NullPointerTester.class);
    }

    @Test
    public void serialize_deserialize() {
        String expected = DUMMY;
        byte[] data = convertToBytes(expected);
        String actual = convertFromBytes(data);

        assertThat(actual, is(expected));
    }

    @Test
    public void serialize_deserialize_through_bytes_message() throws JMSException {
        String expected = DUMMY;
        byte[] data = convertToBytes(expected);
        BytesMessage bytesMessage = createTestBytesMessage(data);
        String actual = objectFromMsg(bytesMessage);

        assertThat(actual, is(expected));
    }

    @Test(expected = RuntimeException.class)
    public void serialize_deserialize_corrupted_data_through_bytes_message() throws JMSException {
        BytesMessage bytesMessage = createTestBytesMessage(new byte[3]);
        objectFromMsg(bytesMessage);
    }

    @Test
    public void serialize_deserialize_null() {
        byte[] data = convertToBytes(null);
        String actual = convertFromBytes(data);

        assertThat(actual, is((String) null));
    }

    @Test(expected = RuntimeException.class)
    public void deserialize_corrupted_data_throws() {
        convertFromBytes(new byte[3]);
    }

}