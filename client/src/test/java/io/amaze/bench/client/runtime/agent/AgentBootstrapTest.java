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
package io.amaze.bench.client.runtime.agent;

import io.amaze.bench.client.runtime.cluster.ActorClusterClient;
import io.amaze.bench.client.runtime.cluster.AgentClusterClient;
import io.amaze.bench.shared.jms.JMSEndpoint;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static io.amaze.bench.client.runtime.agent.AgentBootstrap.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created on 4/10/16.
 */
public final class AgentBootstrapTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private DummyClientFactory clientFactory;
    private JMSEndpoint masterEndpoint;

    @Before
    public void before() {
        clientFactory = new DummyClientFactory(mock(AgentClusterClient.class), mock(ActorClusterClient.class));
        masterEndpoint = new JMSEndpoint("noSuchHost", 1337);
    }

    @Test
    public void invalid_arguments_show_usage() {
        main(new String[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_arguments_throw() {
        main(new String[]{"dummy", "not_an_int"});
    }

    @Test
    public void install_shutdown_hook() {
        Agent agent = createAgent(masterEndpoint, clientFactory);

        Thread shutdownHook = registerShutdownHook(agent);

        assertNotNull(shutdownHook);
        boolean removed = Runtime.getRuntime().removeShutdownHook(shutdownHook);
        assertThat(removed, is(true));
    }

    @Test
    public void shutdown_hook_closes_agent() throws Exception {
        Agent agent = spy(createAgent(masterEndpoint, clientFactory));
        Thread shutdownHook = registerShutdownHook(agent);

        shutdownHook.run();

        verify(agent).close();
    }

    @Test
    public void shutdown_hook_closes_agent_and_close_fail_does_nothing() throws Exception {
        Agent agent = spy(createAgent(masterEndpoint, clientFactory));
        doThrow(new IllegalArgumentException()).when(agent).close();
        Thread shutdownHook = registerShutdownHook(agent);

        shutdownHook.run();

        verify(agent).close();
    }

    @Test
    public void can_create_agent() {
        Agent agent = createAgent(masterEndpoint, clientFactory);

        assertNotNull(agent);
        assertNotNull(agent.getName());
    }

}