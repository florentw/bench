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
package io.amaze.bench.runtime.agent;

import com.google.common.testing.NullPointerTester;
import com.typesafe.config.ConfigException;
import io.amaze.bench.Endpoint;
import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.actor.ActorClusterClient;
import io.amaze.bench.cluster.agent.AgentClusterClient;
import io.amaze.bench.cluster.agent.AgentRegistrySender;
import io.amaze.bench.cluster.registry.ActorRegistryClusterClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.agent.AgentBootstrap.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created on 4/10/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentBootstrapTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    @Mock
    private AgentRegistrySender agentRegistrySender;

    private DummyClientFactory clientFactory;

    @Before
    public void before() {
        AgentClusterClient agentClusterClient = mock(AgentClusterClient.class);
        when(agentClusterClient.agentRegistrySender()).thenReturn(agentRegistrySender);
        clientFactory = new DummyClientFactory(mock(Endpoint.class),
                                               agentClusterClient,
                                               mock(ActorClusterClient.class),
                                               mock(ActorRegistryClusterClient.class),
                                               mock(ClusterConfigFactory.class),
                                               null);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicStaticMethods(AgentBootstrap.class);
    }

    @Test
    public void invalid_arguments_show_usage() {
        main(new String[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_file_throws_illegal_argument_exception() {
        main(new String[]{""});
    }

    @Test(expected = ConfigException.class)
    public void invalid_config_throws_config_exception() {
        main(new String[]{"/dummy.file"});
    }

    @Test
    public void install_shutdown_hook() {
        Agent agent = createAgent(clientFactory);

        Thread shutdownHook = registerShutdownHook(agent);

        assertNotNull(shutdownHook);
        boolean removed = Runtime.getRuntime().removeShutdownHook(shutdownHook);
        assertThat(removed, is(true));
    }

    @Test
    public void shutdown_hook_closes_agent() throws Exception {
        Agent agent = spy(createAgent(clientFactory));
        Thread shutdownHook = registerShutdownHook(agent);

        shutdownHook.run();

        verify(agent).close();
    }

    @Test
    public void shutdown_hook_closes_agent_and_close_fail_does_nothing() throws Exception {
        Agent agent = spy(createAgent(clientFactory));
        doThrow(new IllegalArgumentException()).when(agent).close();
        Thread shutdownHook = registerShutdownHook(agent);

        shutdownHook.run();

        verify(agent).close();
    }

    @Test
    public void can_create_agent() {
        Agent agent = createAgent(clientFactory);

        assertNotNull(agent);
        assertNotNull(agent.getKey());
    }

}