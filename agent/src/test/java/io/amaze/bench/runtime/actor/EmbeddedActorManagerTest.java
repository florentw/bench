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
package io.amaze.bench.runtime.actor;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.agent.DummyClientFactory;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_CONFIG;
import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


/**
 * Created on 3/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class EmbeddedActorManagerTest {

    @Mock
    private ActorClusterClient client;
    private ActorManager actorManager;
    private Actors actors;

    @Before
    public void before() {
        DummyClientFactory factory = new DummyClientFactory(null, client);
        actors = new Actors(factory);
        actorManager = new EmbeddedActorManager(DUMMY_AGENT, actors);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(Actors.class, actors);

        tester.testAllPublicConstructors(EmbeddedActorManager.class);
        tester.testAllPublicInstanceMethods(actorManager);
    }

    @Test
    public void creating_actor_starts_listener() throws ValidationException, InterruptedException {
        ManagedActor actor = actorManager.createActor(DUMMY_CONFIG);
        assertNotNull(actor);

        verify(client).startActorListener(any(RuntimeActor.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void closing_actor_closes_client() throws Exception {
        ManagedActor actor = actorManager.createActor(DUMMY_CONFIG);
        actor.close();

        verify(client).startActorListener(any(RuntimeActor.class));
        verify(client).close();
    }

    @Test
    public void closing_manager_does_nothing() {
        actorManager.close();

        verifyZeroInteractions(client);
    }
}