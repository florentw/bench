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
import io.amaze.bench.client.runtime.actor.ActorDeployInfo;
import io.amaze.bench.client.runtime.actor.DeployConfig;
import io.amaze.bench.client.runtime.agent.Agent;
import io.amaze.bench.orchestrator.ActorMetricValues;
import io.amaze.bench.orchestrator.Actors;
import io.amaze.bench.orchestrator.MetricsRepository;
import io.amaze.bench.orchestrator.io.amaze.bench.util.BenchRule;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.test.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.amaze.bench.actor.ProcessWatcherActorInput.startStopwatch;
import static io.amaze.bench.actor.ProcessWatcherActorInput.stopStopwatch;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertTrue;

/**
 * Created on 9/11/16.
 */
@Category(IntegrationTest.class)
@RunWith(Theories.class)
public final class WatcherActorsIntegrationTest {

    @DataPoints
    public static final boolean[] forked = {false, true};

    private static final String METRIC_KEY = "metric_key";
    private static final String SYSTEM_WATCHER = "SystemWatcher";
    private static final String PROCESS_WATCHER = "ProcessWatcher";

    @Rule
    public final BenchRule benchRule = new BenchRule();
    @Rule
    public Timeout globalTimeout = new Timeout(15, TimeUnit.SECONDS);

    private Agent agent;
    private JMSClient metricsClient;

    @Before
    public void initAgent() throws ExecutionException {
        agent = getUninterruptibly(benchRule.agents().create("test-agent-1"));
        metricsClient = benchRule.createClient();
    }

    @After
    public void closeAgent() throws Exception {
        metricsClient.close();
        agent.close();
    }

    @Theory
    public void create_watcher_actors(boolean forked) throws ExecutionException {

        Actors.ActorHandle systemWatcher = benchRule.actors().create(systemActorConfig(forked));
        Actors.ActorHandle processesWatcher = benchRule.actors().create(processActorConfig(forked));

        getUninterruptibly(systemWatcher.actorCreation());
        getUninterruptibly(processesWatcher.actorCreation());
    }

    @Theory
    public void close_watcher_actors(boolean forked) throws ExecutionException {
        Actors.ActorHandle systemWatcher = benchRule.actors().create(systemActorConfig(forked));
        Actors.ActorHandle processesWatcher = benchRule.actors().create(processActorConfig(forked));
        getUninterruptibly(systemWatcher.actorCreation());
        getUninterruptibly(processesWatcher.actorCreation());
        getUninterruptibly(systemWatcher.initialize());
        getUninterruptibly(processesWatcher.initialize());

        getUninterruptibly(systemWatcher.close());
        getUninterruptibly(processesWatcher.close());
    }

    @Theory
    public void start_system_monitoring(boolean forked) throws ExecutionException {
        MetricsRepository metricsRepository = new MetricsRepository(benchRule.jmsServer(), metricsClient);

        Actors.ActorHandle systemWatcher = createAndInitSystemWatcher(forked);

        systemWatcher.send(WatcherActorsIntegrationTest.class.getName(), SystemWatcherInput.start(1));

        sleepUninterruptibly(5, TimeUnit.SECONDS);

        systemWatcher.send(WatcherActorsIntegrationTest.class.getName(), SystemWatcherInput.stop());

        systemWatcher.dumpMetrics();
        Future<ActorMetricValues> metrics = metricsRepository.expectValuesFor(SYSTEM_WATCHER);
        getUninterruptibly(systemWatcher.close());

        assertTrue(getUninterruptibly(metrics).metrics().size() == 4);
    }

    /**
     * Here we monitor the {@link SystemWatcherActor} process using the {@link ProcessWatcherActor}
     *
     * @throws ExecutionException No expected
     */
    @Theory
    public void start_process_monitoring(boolean forked) throws ExecutionException {
        MetricsRepository metricsRepository = new MetricsRepository(benchRule.jmsServer(), metricsClient);
        Future<ActorMetricValues> metrics = metricsRepository.expectValuesFor(PROCESS_WATCHER);

        Actors.ActorHandle systemWatcher = createAndInitSystemWatcher(forked);
        Actors.ActorHandle processesWatcher = benchRule.actors().create(processActorConfig(forked));
        getUninterruptibly(processesWatcher.actorCreation());
        ActorDeployInfo deployInfo = getUninterruptibly(processesWatcher.initialize());

        processesWatcher.send(PROCESS_WATCHER, startStopwatch(deployInfo.getPid(), METRIC_KEY));

        sleepUninterruptibly(2, TimeUnit.SECONDS);

        processesWatcher.send(PROCESS_WATCHER, stopStopwatch(deployInfo.getPid(), METRIC_KEY));

        processesWatcher.dumpMetrics();

        sleepUninterruptibly(3, TimeUnit.SECONDS);

        assertTrue(getUninterruptibly(metrics).metrics().size() == 13);
        getUninterruptibly(systemWatcher.close());
        getUninterruptibly(processesWatcher.close());
    }

    private ActorConfig systemActorConfig(boolean forked) {
        return new ActorConfig(SYSTEM_WATCHER,
                               SystemWatcherActor.class.getName(),
                               new DeployConfig(forked, emptyList()),
                               "{}");
    }

    private ActorConfig processActorConfig(boolean forked) {
        return new ActorConfig(PROCESS_WATCHER,
                               ProcessWatcherActor.class.getName(),
                               new DeployConfig(forked, emptyList()),
                               "{}");
    }

    private Actors.ActorHandle createAndInitSystemWatcher(boolean forked) throws ExecutionException {
        Actors.ActorHandle systemWatcher = benchRule.actors().create(systemActorConfig(forked));
        getUninterruptibly(systemWatcher.actorCreation());
        getUninterruptibly(systemWatcher.initialize());
        return systemWatcher;
    }
}
