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
package io.amaze.bench.cluster.registry;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.cluster.Endpoint;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.agent.AgentRegistrationMessage;
import io.amaze.bench.cluster.agent.AgentUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created on 3/30/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AgentRegistryListenerLoggerTest {

    @Mock
    private AgentRegistryListener delegateListener;
    @Mock
    private Endpoint endpoint;

    private AgentRegistryListenerLogger loggerListener;

    @Before
    public void before() {
        loggerListener = new AgentRegistryListenerLogger(delegateListener);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(AgentKey.class, AgentUtil.DUMMY_AGENT);

        tester.testAllPublicConstructors(AgentRegistryListenerLogger.class);
        tester.testAllPublicInstanceMethods(loggerListener);
    }

    @Test
    public void agent_creation() {
        AgentRegistrationMessage regMsg = AgentRegistrationMessage.create(AgentUtil.DUMMY_AGENT, endpoint);
        loggerListener.onAgentRegistration(regMsg);

        verify(delegateListener).onAgentRegistration(regMsg);
        verifyNoMoreInteractions(delegateListener);
    }

    @Test
    public void agent_sign_off() {
        loggerListener.onAgentSignOff(AgentUtil.DUMMY_AGENT);

        verify(delegateListener).onAgentSignOff(AgentUtil.DUMMY_AGENT);
        verifyNoMoreInteractions(delegateListener);
    }

}