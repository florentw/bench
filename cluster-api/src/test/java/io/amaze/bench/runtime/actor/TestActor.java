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
package io.amaze.bench.runtime.actor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.amaze.bench.api.*;
import io.amaze.bench.cluster.actor.ActorConfig;
import io.amaze.bench.cluster.actor.DeployConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;

/**
 * Created on 3/3/16.
 */
@io.amaze.bench.api.Actor
public class TestActor implements Reactor<String> {

    public static final ActorKey DUMMY_ACTOR = new ActorKey("test-actor");
    public static final String DUMMY_JSON_CONFIG = "{}";
    public static final String REPLY_MESSAGE = "REPLY_TO";

    static final String FAIL_MSG = "DO_FAIL";
    static final String RECOVERABLE_EXCEPTION_MSG = "THROW_RECOVERABLE";
    static final String RUNTIME_EXCEPTION_MSG = "THROW_RUNTIME";
    static final String TERMINATE_MSG = "TERMINATE";
    private static final Map<ActorKey, TestActor> runningActors = new HashMap<>();
    private static final Logger log = LogManager.getLogger();
    private static final DeployConfig DUMMY_DEPLOY_CONFIG = createDeployConfig(false);
    public static final ActorConfig DUMMY_CONFIG = createActorConfig();
    private final CountDownLatch messageReceived = new CountDownLatch(1);
    private final Sender sender;
    private final Map<String, List<String>> receivedMessages = new HashMap<>();
    private final Config config;
    private boolean beforeCalled = false;
    private boolean afterCalled = false;

    public TestActor(final ActorKey actorKey, final Sender sender, final Config config) {
        this.sender = sender;
        this.config = config;

        synchronized (runningActors) {
            runningActors.put(actorKey, this);
        }
    }

    protected TestActor(final Sender sender) {
        this(DUMMY_ACTOR, sender, ConfigFactory.parseString(DUMMY_JSON_CONFIG));
    }

    public static Map<ActorKey, TestActor> runningActors() {
        synchronized (runningActors) {
            return runningActors;
        }
    }

    public static ActorConfig configForActor(Class<?> clazz) {
        return new ActorConfig(DUMMY_ACTOR, clazz.getName(), DUMMY_DEPLOY_CONFIG, DUMMY_JSON_CONFIG);
    }

    public static ActorConfig configForActor(final Class<?> clazz, final boolean forked) {
        return new ActorConfig(DUMMY_ACTOR, clazz.getName(), createDeployConfig(forked), DUMMY_JSON_CONFIG);
    }

    @Before
    public void before() {
        beforeCalled = true;
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final String message) throws ReactorException {
        log.debug("{} received message from {}: {}", this, from, message);

        if (replyToMessage(message)) {
            return;
        }

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

        synchronized (receivedMessages) {
            List<String> messages = receivedMessages.computeIfAbsent(from, k -> new ArrayList<>());
            messages.add(message);

            messageReceived.countDown();
        }
    }

    @After
    public void after() {
        afterCalled = true;
    }

    public Map<String, List<String>> awaitFirstAndReturnMessages() {
        awaitUninterruptibly(messageReceived);
        synchronized (receivedMessages) {
            return new HashMap<>(receivedMessages);
        }
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

    Config getConfig() {
        return config;
    }

    private static DeployConfig createDeployConfig(final boolean forked) {
        return new DeployConfig(forked, Collections.emptyList());
    }

    private static ActorConfig createActorConfig() {
        return configForActor(TestActor.class);
    }

    private boolean replyToMessage(final @NotNull String message) {
        if (message.startsWith(REPLY_MESSAGE)) {
            String replyAddress = message.split(":")[1];
            sender.send(replyAddress, "hello");
            return true;
        }
        return false;
    }
}