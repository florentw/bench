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
package io.amaze.bench.cluster.leader.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.cluster.agent.AgentInputMessage;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.runtime.actor.TestActor;
import io.amaze.bench.runtime.cluster.jgroups.JgroupsSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created on 10/23/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsResourceManagerClusterClientTest {

    @Mock
    private JgroupsSender jgroupsSender;

    private JgroupsResourceManagerClusterClient clusterClient;

    @Before
    public void init() {
        clusterClient = new JgroupsResourceManagerClusterClient(jgroupsSender);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JgroupsResourceManagerClusterClient.class);
        tester.testAllPublicInstanceMethods(clusterClient);
    }

    @Test
    public void send_to_agent_broadcasts_message() {
        AgentInputMessage msg = AgentInputMessage.closeActor(new AgentKey("agent"), TestActor.DUMMY_ACTOR);

        clusterClient.sendToAgent(msg);

        verify(jgroupsSender).broadcast(msg);
        verifyNoMoreInteractions(jgroupsSender);
    }

    @Test
    public void init_for_actor_does_nothing() {
        clusterClient.initForActor(TestActor.DUMMY_ACTOR);

        verifyNoMoreInteractions(jgroupsSender);
    }

    @Test
    public void close_for_actor_does_nothing() {
        clusterClient.closeForActor(TestActor.DUMMY_ACTOR);

        verifyNoMoreInteractions(jgroupsSender);
    }

    @Test
    public void close_does_nothing() {
        clusterClient.close();

        verifyNoMoreInteractions(jgroupsSender);
    }

}