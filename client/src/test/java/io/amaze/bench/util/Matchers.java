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
import io.amaze.bench.client.runtime.agent.AgentLifecycleMessage;
import io.amaze.bench.client.runtime.message.Message;
import org.mockito.ArgumentMatcher;

import java.io.Serializable;

import static org.junit.Assert.assertNotNull;

/**
 * Mockito matchers for {@link io.amaze.bench.client.runtime.LifecycleMessage} instances matching.
 */
public final class Matchers {

    public static ArgumentMatcher<ActorLifecycleMessage> isActorState(ActorLifecycleMessage.State state) {
        return new ArgumentMatcher<ActorLifecycleMessage>() {
            @Override
            public boolean matches(final Object argument) {
                ActorLifecycleMessage actual = (ActorLifecycleMessage) argument;
                if (ActorLifecycleMessage.State.FAILED == actual.getState()) {
                    assertNotNull(actual.getThrowable());
                }

                return actual.getState() == state;
            }
        };
    }

    public static ArgumentMatcher<Serializable> isAgentLifecycle(final String from, final AgentLifecycleMessage input) {
        return new ArgumentMatcher<Serializable>() {
            @Override
            public boolean matches(final Object argument) {
                Message<AgentLifecycleMessage> msg = (Message<AgentLifecycleMessage>) argument;
                return msg.data().equals(input) && msg.from().equals(from);
            }
        };
    }

    public static ArgumentMatcher<Serializable> isActorLifecycle(final String from, final ActorLifecycleMessage input) {
        return new ArgumentMatcher<Serializable>() {
            @Override
            public boolean matches(final Object argument) {
                Message<ActorLifecycleMessage> msg = (Message<ActorLifecycleMessage>) argument;
                return msg.data().equals(input) && msg.from().equals(from);
            }
        };
    }

    public static ArgumentMatcher<AgentLifecycleMessage> isAgentState(final AgentLifecycleMessage.State state) {
        return new ArgumentMatcher<AgentLifecycleMessage>() {
            @Override
            public boolean matches(final Object argument) {
                AgentLifecycleMessage message = (AgentLifecycleMessage) argument;
                return message.getState() == state;
            }
        };
    }
}
