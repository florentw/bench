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
package io.amaze.bench.shared.jgroups;

import com.google.common.testing.NullPointerTester;
import org.jgroups.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Created on 10/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsListenerMultiplexerTest {

    private static final String DUMMY_PAYLOAD = "payload";

    @Mock
    private JgroupsListener<String> listener;
    @Mock
    private Message dummyMessage;

    private JgroupsListenerMultiplexer listenerMultiplexer;

    @Before
    public void init() {
        listenerMultiplexer = new JgroupsListenerMultiplexer();
        when(dummyMessage.getObject()).thenReturn(DUMMY_PAYLOAD);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JgroupsListenerMultiplexer.class);
        tester.testAllPublicInstanceMethods(listenerMultiplexer);
    }

    @Test
    public void add_listener_should_register_listener() {
        listenerMultiplexer.addListener(String.class, listener);

        assertThat(listenerMultiplexer.getListeners().size(), is(1));
        assertThat(listenerMultiplexer.getListeners().get(String.class.getName()).iterator().next(), is(listener));
        verifyZeroInteractions(listener);
    }

    @Test(expected = IllegalStateException.class)
    public void add_listener_on_same_object_twice_throws() {
        listenerMultiplexer.addListener(String.class, listener);

        listenerMultiplexer.addListener(String.class, listener);
    }

    @Test
    public void remove_single_key_listener_should_unregister_listener_and_delete_key() {
        listenerMultiplexer.addListener(String.class, listener);

        listenerMultiplexer.removeListener(listener);

        assertThat(listenerMultiplexer.getListeners().isEmpty(), is(true));
        verifyZeroInteractions(listener);
    }

    @Test
    public void remove_listener_should_unregister_listener() {
        listenerMultiplexer.addListener(String.class, listener);
        listenerMultiplexer.addListener(Integer.class, (msg, payload) -> {
        });

        listenerMultiplexer.removeListener(listener);

        assertThat(listenerMultiplexer.getListeners().size(), is(1));
        assertNull(listenerMultiplexer.getListeners().get(String.class.getName()));
        verifyZeroInteractions(listener);
    }

    @Test
    public void remove_unknown_listener_does_not_throw() {
        listenerMultiplexer.removeListener(listener);

        assertThat(listenerMultiplexer.getListeners().size(), is(0));
        verifyZeroInteractions(listener);
    }

    @Test
    public void dispatch_sends_message_to_registered_listener() {
        listenerMultiplexer.addListener(String.class, listener);

        listenerMultiplexer.dispatch(dummyMessage);

        verify(listener).onMessage(same(dummyMessage), eq(DUMMY_PAYLOAD));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void dispatch_sends_message_to_two_registered_listener() {
        JgroupsListener<String> listener2 = mock(JgroupsListener.class);
        listenerMultiplexer.addListener(String.class, listener);
        listenerMultiplexer.addListener(String.class, listener2);

        listenerMultiplexer.dispatch(dummyMessage);

        verify(listener).onMessage(same(dummyMessage), eq(DUMMY_PAYLOAD));
        verify(listener2).onMessage(same(dummyMessage), eq(DUMMY_PAYLOAD));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void dispatch_sends_message_to_one_of_two_registered_listeners() {
        JgroupsListener<Integer> listener2 = mock(JgroupsListener.class);
        listenerMultiplexer.addListener(String.class, listener);
        listenerMultiplexer.addListener(Integer.class, listener2);

        listenerMultiplexer.dispatch(dummyMessage);

        verify(listener).onMessage(same(dummyMessage), eq(DUMMY_PAYLOAD));
        verifyNoMoreInteractions(listener);
        verifyZeroInteractions(listener2);
    }

    @Test
    public void dispatch_when_no_registered_listeners_does_not_throw() {

        listenerMultiplexer.dispatch(dummyMessage);

        verifyZeroInteractions(listener);
    }

    @Test
    public void error_while_notifying_listener_does_not_throw() {
        doThrow(new IllegalArgumentException()).when(listener).onMessage(dummyMessage, DUMMY_PAYLOAD);
        listenerMultiplexer.addListener(String.class, listener);

        listenerMultiplexer.dispatch(dummyMessage);

        verify(listener).onMessage(same(dummyMessage), eq(DUMMY_PAYLOAD));
        verifyZeroInteractions(listener);
    }
}