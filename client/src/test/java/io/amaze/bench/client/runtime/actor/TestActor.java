package io.amaze.bench.client.runtime.actor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.amaze.bench.client.api.ReactorException;
import io.amaze.bench.client.api.actor.After;
import io.amaze.bench.client.api.actor.Before;
import io.amaze.bench.client.api.actor.Reactor;
import io.amaze.bench.client.api.actor.Sender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 3/3/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@io.amaze.bench.client.api.actor.Actor
public class TestActor implements Reactor<String> {

    public static final String DUMMY_CONFIG = "{}";
    public static final String DUMMY_ACTOR = "test-actor";
    public static final String FAIL_MSG = "DO_FAIL";

    private final Sender sender;
    private final Map<String, List<String>> receivedMessages = new HashMap<>();
    private final Config config;
    private boolean beforeCalled = false;
    private boolean afterCalled = false;

    public TestActor(Sender sender) {
        this.sender = sender;
        this.config = ConfigFactory.parseString(DUMMY_CONFIG);
    }

    public TestActor(Sender sender, final Config config) {
        this.sender = sender;
        this.config = config;
    }

    @Before
    public void before() {
        beforeCalled = true;
    }

    @Override
    public void onMessage(final String from, final String message) throws ReactorException {
        if (message.equals(FAIL_MSG)) {
            throw new ReactorException("Provoked failure.");
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