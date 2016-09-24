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
package io.amaze.bench.util;

import io.amaze.bench.client.runtime.actor.ActorLifecycleMessage;
import io.amaze.bench.client.runtime.agent.AgentOutputMessage;
import io.amaze.bench.client.runtime.message.Message;
import org.mockito.ArgumentMatcher;

import java.io.Serializable;

import static org.junit.Assert.assertNotNull;

/**
 * Created on 9/24/16.
 */
public final class Matchers {

    public static ArgumentMatcher<ActorLifecycleMessage> isLifecyclePhase(ActorLifecycleMessage.Phase phase) {
        return new ArgumentMatcher<ActorLifecycleMessage>() {
            @Override
            public boolean matches(final Object argument) {
                ActorLifecycleMessage actual = (ActorLifecycleMessage) argument;
                if (ActorLifecycleMessage.Phase.FAILED == actual.getPhase()) {
                    assertNotNull(actual.getThrowable());
                }

                return actual.getPhase() == phase;
            }
        };
    }

    public static ArgumentMatcher<Serializable> isAgentLifecycle(final AgentOutputMessage input) {
        return new ArgumentMatcher<Serializable>() {
            @Override
            public boolean matches(final Object argument) {
                Message<AgentOutputMessage> msg = (Message<AgentOutputMessage>) argument;
                return msg.data().equals(input);
            }
        };
    }

    public static ArgumentMatcher<Serializable> isActorLifecycle(final String from, final ActorLifecycleMessage input) {
        return new ArgumentMatcher<Serializable>() {
            @Override
            public boolean matches(final Object argument) {
                Message<AgentOutputMessage> msg = (Message<AgentOutputMessage>) argument;
                return msg.data().getData().equals(input) && msg.from().equals(from);
            }
        };
    }
}
