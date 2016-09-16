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
package io.amaze.bench.actor;

import io.amaze.bench.client.runtime.actor.ActorConfig;
import io.amaze.bench.client.runtime.actor.DeployConfig;
import io.amaze.bench.client.runtime.agent.Agent;
import io.amaze.bench.orchestrator.ActorSender;
import io.amaze.bench.orchestrator.Actors;
import io.amaze.bench.orchestrator.MetricsRepository;
import io.amaze.bench.orchestrator.io.amaze.bench.util.BenchRule;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.test.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.amaze.bench.client.runtime.actor.ActorInputMessage.*;
import static java.util.Collections.emptyList;

/**
 * Created on 9/11/16.
 */
@Category(IntegrationTest.class)
public final class WatcherActorsIntegrationTest {

    private static final String SYSTEM_WATCHER = "SystemWatcher";
    private static final String PROCESS_WATCHER = "ProcessWatcher";
    private static final DeployConfig EMBEDDED = new DeployConfig(true, emptyList());
    private static final ActorConfig SYSTEM_WATCHER_CONFIG = new ActorConfig(SYSTEM_WATCHER,
                                                                             SystemWatcherActor.class.getName(),
                                                                             EMBEDDED,
                                                                             "{}");
    private static final ActorConfig PROCESSES_WATCHER_CONFIG = new ActorConfig(PROCESS_WATCHER,
                                                                                ProcessWatcherActor.class.getName(),
                                                                                EMBEDDED,
                                                                                "{}");

    @Rule
    public final BenchRule benchRule = new BenchRule();

    private Agent agent;

    @Before
    public void initAgent() throws ExecutionException {
        agent = getUninterruptibly(benchRule.agents().create("test-agent-1"));
    }

    @After
    public void closeAgent() throws Exception {
        agent.close();
    }

    @Test
    public void create_watcher_actors() throws ExecutionException {

        Actors.ActorHandle systemWatcher = benchRule.actors().create(SYSTEM_WATCHER_CONFIG);
        Actors.ActorHandle processesWatcher = benchRule.actors().create(PROCESSES_WATCHER_CONFIG);

        getUninterruptibly(systemWatcher.actorCreation());
        getUninterruptibly(processesWatcher.actorCreation());
    }

    @Test
    public void close_watcher_actors() throws ExecutionException {
        Actors.ActorHandle systemWatcher = benchRule.actors().create(SYSTEM_WATCHER_CONFIG);
        Actors.ActorHandle processesWatcher = benchRule.actors().create(PROCESSES_WATCHER_CONFIG);
        getUninterruptibly(systemWatcher.actorCreation());
        getUninterruptibly(processesWatcher.actorCreation());

        benchRule.actorSender().sendToActor(SYSTEM_WATCHER, close());
        benchRule.actorSender().sendToActor(PROCESS_WATCHER, close());

        getUninterruptibly(systemWatcher.actorTermination());
        getUninterruptibly(processesWatcher.actorTermination());
    }

    @Test
    public void start_system_monitoring() throws ExecutionException {
        try (JMSClient metricsClient = benchRule.createClient()) {
            MetricsRepository metricsRepository = new MetricsRepository(benchRule.jmsServer(), metricsClient);
            metricsRepository.startListener();

            ActorSender sender = benchRule.actorSender();
            Actors.ActorHandle systemWatcher = benchRule.actors().create(SYSTEM_WATCHER_CONFIG);
            getUninterruptibly(systemWatcher.actorCreation());

            sender.sendToActor(SYSTEM_WATCHER,
                               sendMessage(WatcherActorsIntegrationTest.class.getName(), SystemWatcherInput.start(1)));

            sleepUninterruptibly(5, TimeUnit.SECONDS);

            sender.sendToActor(SYSTEM_WATCHER, dumpMetrics());
            sender.sendToActor(SYSTEM_WATCHER, close());
            getUninterruptibly(systemWatcher.actorTermination());
        }
    }
}
