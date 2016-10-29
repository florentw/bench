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
package io.amaze.bench.runtime.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.actor.ActorDeployInfo;
import io.amaze.bench.runtime.actor.ActorInputMessage;
import io.amaze.bench.runtime.actor.ActorKey;
import io.amaze.bench.runtime.agent.AgentKey;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.runtime.cluster.registry.RegisteredActor;
import io.amaze.bench.shared.jgroups.JgroupsEndpoint;
import org.jgroups.Address;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.NoSuchElementException;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.runtime.cluster.registry.RegisteredActor.created;
import static io.amaze.bench.runtime.cluster.registry.RegisteredActor.initialized;
import static org.mockito.Mockito.*;

/**
 * Created on 10/19/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsActorSenderTest {

    private static final AgentKey AGENT = new AgentKey("agent");

    @Mock
    private JgroupsSender jgroupsSender;
    @Mock
    private ActorRegistry actorRegistry;
    @Mock
    private Address address;

    private JgroupsEndpoint endpoint;

    private JgroupsActorSender sender;

    @Before
    public void init() {
        sender = new JgroupsActorSender(jgroupsSender, actorRegistry);
        endpoint = new JgroupsEndpoint(address);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(JgroupsSender.class, jgroupsSender);
        tester.setDefault(ActorInputMessage.class, ActorInputMessage.dumpMetrics());
        tester.setDefault(ActorKey.class, new ActorKey("dummy"));

        tester.testAllPublicConstructors(JgroupsActorSender.class);
        tester.testAllPublicInstanceMethods(sender);
    }

    @Test
    public void send_to_actor_resolves_endpoint_through_registry_and_sends() throws Exception {
        RegisteredActor agent = created(DUMMY_ACTOR, AGENT);
        when(actorRegistry.byKey(DUMMY_ACTOR)).thenReturn(initialized(agent, new ActorDeployInfo(endpoint, 10)));
        ActorInputMessage message = ActorInputMessage.sendMessage("other", "hello");

        sender.send(DUMMY_ACTOR, message);

        verify(jgroupsSender).sendToEndpoint(eq(endpoint), any(JgroupsActorMessage.class));
        verifyNoMoreInteractions(jgroupsSender);
    }

    @Test(expected = NoSuchElementException.class)
    public void sending_to_unknown_actor_throws_NoSuchElementException() {
        sender.send(DUMMY_ACTOR, ActorInputMessage.sendMessage("other", "hello"));
    }

    @Test(expected = NoSuchElementException.class)
    public void sending_to_uninitialized_actor_throws_NoSuchElementException() {
        when(actorRegistry.byKey(DUMMY_ACTOR)).thenReturn(created(DUMMY_ACTOR, AGENT));

        sender.send(DUMMY_ACTOR, ActorInputMessage.sendMessage("other", "hello"));
    }

}