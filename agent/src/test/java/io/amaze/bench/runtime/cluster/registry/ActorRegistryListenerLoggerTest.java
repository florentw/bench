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
package io.amaze.bench.runtime.cluster.registry;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.cluster.actor.ActorDeployInfo;
import io.amaze.bench.runtime.cluster.actor.ActorKey;
import io.amaze.bench.runtime.cluster.agent.AgentKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created on 3/29/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorRegistryListenerLoggerTest {

    @Mock
    private Endpoint endpoint;
    @Mock
    private ActorRegistryListener delegateListener;

    private ActorRegistryListenerLogger loggerListener;

    @Before
    public void before() {
        loggerListener = new ActorRegistryListenerLogger(delegateListener);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorDeployInfo.class, new ActorDeployInfo(endpoint, 10));
        tester.setDefault(ActorKey.class, DUMMY_ACTOR);
        tester.setDefault(AgentKey.class, DUMMY_AGENT);

        tester.testAllPublicConstructors(ActorRegistryListenerLogger.class);
        tester.testAllPublicInstanceMethods(loggerListener);
    }

    @Test
    public void actor_created() {
        loggerListener.onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);

        verify(delegateListener).onActorCreated(DUMMY_ACTOR, DUMMY_AGENT);
        verifyNoMoreInteractions(delegateListener);
    }

    @Test
    public void actor_started() {
        ActorDeployInfo deployInfo = new ActorDeployInfo(endpoint, 10);
        loggerListener.onActorInitialized(DUMMY_ACTOR, deployInfo);

        verify(delegateListener).onActorInitialized(DUMMY_ACTOR, deployInfo);
        verifyNoMoreInteractions(delegateListener);
    }

    @Test
    public void actor_failed() throws Exception {
        IllegalArgumentException throwable = new IllegalArgumentException();
        loggerListener.onActorFailed(DUMMY_ACTOR, throwable);

        verify(delegateListener).onActorFailed(DUMMY_ACTOR, throwable);
        verifyNoMoreInteractions(delegateListener);
    }

    @Test
    public void actor_closed() throws Exception {
        loggerListener.onActorClosed(DUMMY_ACTOR);

        verify(delegateListener).onActorClosed(DUMMY_ACTOR);
        verifyNoMoreInteractions(delegateListener);
    }
}