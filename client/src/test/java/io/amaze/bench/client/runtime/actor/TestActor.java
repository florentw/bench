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
package io.amaze.bench.client.runtime.actor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.amaze.bench.api.*;
import io.amaze.bench.shared.jms.JMSEndpoint;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Created on 3/3/16.
 */
@io.amaze.bench.api.Actor
public class TestActor implements Reactor<String> {

    public static final String DUMMY_ACTOR = "test-actor";
    public static final String DUMMY_JSON_CONFIG = "{}";
    static final String FAIL_MSG = "DO_FAIL";
    static final String RECOVERABLE_EXCEPTION_MSG = "THROW_RECOVERABLE";
    static final String RUNTIME_EXCEPTION_MSG = "THROW_RUNTIME";
    static final String TERMINATE_MSG = "TERMINATE";

    private static final DeployConfig DUMMY_DEPLOY_CONFIG = createDeployConfig(false);
    public static final ActorConfig DUMMY_CONFIG = createActorConfig();

    private final Sender sender;
    private final Map<String, List<String>> receivedMessages = new HashMap<>();
    private final Config config;
    private boolean beforeCalled = false;
    private boolean afterCalled = false;

    public TestActor(final Sender sender) {
        this.sender = sender;
        this.config = ConfigFactory.parseString(DUMMY_JSON_CONFIG);
    }

    public TestActor(final Sender sender, final Config config) {
        this.sender = sender;
        this.config = config;
    }

    public static ActorConfig configForActor(Class<?> clazz) {
        return new ActorConfig(DUMMY_ACTOR, clazz.getName(), DUMMY_DEPLOY_CONFIG, DUMMY_JSON_CONFIG);
    }

    public static ActorConfig configForActor(final Class<?> clazz, final boolean forked) {
        return new ActorConfig(DUMMY_ACTOR, clazz.getName(), createDeployConfig(forked), DUMMY_JSON_CONFIG);
    }

    private static DeployConfig createDeployConfig(final boolean forked) {
        return new DeployConfig(new JMSEndpoint("dummy", 1337), forked, Collections.emptyList());
    }

    private static ActorConfig createActorConfig() {
        return configForActor(TestActor.class);
    }

    @Before
    public void before() {
        beforeCalled = true;
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final String message) throws ReactorException {
        switch (message) {
            case FAIL_MSG:
                throw new IrrecoverableException("Provoked failure.");
            case RECOVERABLE_EXCEPTION_MSG:
                throw new RecoverableException("Provoked recoverable exception.", new IllegalArgumentException());
            case TERMINATE_MSG:
                throw new TerminationException();
            case RUNTIME_EXCEPTION_MSG:
                throw new IllegalArgumentException();
            default:
        }

        List<String> msgs = receivedMessages.get(from);
        if (msgs == null) {
            msgs = new ArrayList<>();
            receivedMessages.put(from, msgs);
        }
        msgs.add(message);
    }

    @After
    public void after() {
        afterCalled = true;
    }

    boolean isBeforeCalled() {
        return beforeCalled;
    }

    boolean isAfterCalled() {
        return afterCalled;
    }

    Sender getSender() {
        return sender;
    }

    Map<String, List<String>> getReceivedMessages() {
        return receivedMessages;
    }

    Config getConfig() {
        return config;
    }
}