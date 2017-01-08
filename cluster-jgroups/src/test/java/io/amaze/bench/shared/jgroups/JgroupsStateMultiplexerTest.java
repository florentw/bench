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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created on 10/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsStateMultiplexerTest {

    private static final JgroupsStateKey STATE_KEY = new JgroupsStateKey("key");
    private static final String STATE_VALUE = "value";

    @Mock
    private JgroupsStateHolder<String> stateHolder;

    private JgroupsStateMultiplexer stateMultiplexer;

    @Before
    public void init() {
        stateMultiplexer = new JgroupsStateMultiplexer();
        when(stateHolder.getKey()).thenReturn(STATE_KEY);
        when(stateHolder.getState()).thenReturn(STATE_VALUE);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(JgroupsStateMultiplexer.class);
        tester.testAllPublicInstanceMethods(stateMultiplexer);
    }

    @Test
    public void add_state_holder_registers_it() {
        stateMultiplexer.addStateHolder(stateHolder);

        assertThat(stateMultiplexer.getStateHolderObjects().size(), is(1));
        assertThat(stateMultiplexer.getStateHolderObjects().get(STATE_KEY), is(stateHolder));
        verify(stateHolder).getKey();
        verifyNoMoreInteractions(stateHolder);
    }

    @Test(expected = IllegalStateException.class)
    public void add_state_holder_with_same_key_twice_throws() {
        stateMultiplexer.addStateHolder(stateHolder);

        stateMultiplexer.addStateHolder(stateHolder);
    }

    @Test
    public void remove_state_holder_unregisters_it() {
        stateMultiplexer.addStateHolder(stateHolder);

        stateMultiplexer.removeStateHolder(STATE_KEY);

        assertThat(stateMultiplexer.getStateHolderObjects().size(), is(0));
        verify(stateHolder).getKey();
        verifyZeroInteractions(stateHolder);
    }

    @Test
    public void remove_holder_with_unknown_key_does_not_throw() {
        stateMultiplexer.removeStateHolder(STATE_KEY);

        assertThat(stateMultiplexer.getStateHolderObjects().size(), is(0));
        verifyZeroInteractions(stateHolder);
    }

    @Test
    public void gather_state_calls_getState_on_state_holder() throws Exception {
        stateMultiplexer.addStateHolder(stateHolder);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            stateMultiplexer.gatherStateFrom(outputStream);

            JgroupsSharedState sharedState = readStateFromOutputStream(outputStream);
            assertThat(sharedState.getStatesByKey().size(), is(1));
            assertThat(sharedState.getStatesByKey().get(STATE_KEY), is(STATE_VALUE));
        }

        assertThat(stateMultiplexer.getStateHolderObjects().size(), is(1));
        verify(stateHolder, times(2)).getKey();
        verify(stateHolder).getState();
        verifyNoMoreInteractions(stateHolder);
    }


    @Test
    public void gather_state_when_no_state_holder_does_not_throw() throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            stateMultiplexer.gatherStateFrom(outputStream);

            JgroupsSharedState sharedState = readStateFromOutputStream(outputStream);
            assertThat(sharedState.getStatesByKey().size(), is(0));
        }
    }

    @Test
    public void write_state_calls_setState_on_state_holder() throws Exception {
        stateMultiplexer.addStateHolder(stateHolder);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedSharedState())) {
            stateMultiplexer.writeStateTo(inputStream);
        }

        verify(stateHolder).getKey();
        verify(stateHolder).setState(STATE_VALUE);
        verifyNoMoreInteractions(stateHolder);
    }

    @Test
    public void write_state_when_no_state_holder_does_not_throw() throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedSharedState())) {
            stateMultiplexer.writeStateTo(inputStream);
        }
        verifyZeroInteractions(stateHolder);
    }

    private byte[] serializedSharedState() throws IOException {
        Map<JgroupsStateKey, Serializable> statesByKey = new HashMap<>();
        statesByKey.put(STATE_KEY, STATE_VALUE);
        JgroupsSharedState expectedSharedState = new JgroupsSharedState(statesByKey);

        byte[] serializedState;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(expectedSharedState);
            serializedState = outputStream.toByteArray();
        }
        return serializedState;
    }

    private JgroupsSharedState readStateFromOutputStream(final ByteArrayOutputStream outputStream)
            throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
             ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream)) {
            return (JgroupsSharedState) inputStream.readObject();
        }
    }
}