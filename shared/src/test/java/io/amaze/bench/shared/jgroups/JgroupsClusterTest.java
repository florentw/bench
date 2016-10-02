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
package io.amaze.bench.shared.jgroups;

import com.google.common.testing.NullPointerTester;
import org.jgroups.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created on 10/3/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsClusterTest {

    @Mock
    private JgroupsListenerMultiplexer listenerMultiplexer;
    @Mock
    private JgroupsStateMultiplexer stateMultiplexer;
    @Mock
    private OutputStream outputStream;
    @Mock
    private InputStream inputStream;

    private JgroupsCluster jgroupsCluster;

    @Before
    public void init() {
        jgroupsCluster = new JgroupsCluster(listenerMultiplexer, stateMultiplexer);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JgroupsCluster.class);
        tester.testAllPublicInstanceMethods(jgroupsCluster);
    }

    @Test
    public void public_constructor_creates_multiplexers() {
        JgroupsCluster cluster = new JgroupsCluster();

        assertNotNull(cluster.listenerMultiplexer());
        assertNotNull(cluster.stateMultiplexer());
    }

    @Test
    public void calling_receive_dispatches_message_to_listeners() {
        Message dummyMessage = new Message();

        jgroupsCluster.receive(dummyMessage);

        verify(listenerMultiplexer).dispatch(dummyMessage);
        verifyZeroInteractions(stateMultiplexer);
    }

    @Test
    public void get_state_calls_gather_state_on_multiplexer() throws IOException {
        jgroupsCluster.getState(outputStream);

        verify(stateMultiplexer).gatherStateFrom(outputStream);
        verifyZeroInteractions(listenerMultiplexer);
    }

    @Test
    public void set_state_calls_write_state_on_multiplexer() throws IOException {
        jgroupsCluster.setState(inputStream);

        verify(stateMultiplexer).writeStateTo(inputStream);
        verifyZeroInteractions(listenerMultiplexer);
    }

}