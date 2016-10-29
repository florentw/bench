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

import com.google.common.base.Throwables;
import io.amaze.bench.cluster.actor.ActorConfig;
import io.amaze.bench.cluster.actor.ActorDeployInfo;
import io.amaze.bench.cluster.actor.ActorKey;
import io.amaze.bench.cluster.actor.DeployConfig;
import io.amaze.bench.cluster.agent.AgentKey;
import io.amaze.bench.cluster.metric.MetricValuesMessage;
import io.amaze.bench.leader.cluster.Actors;
import io.amaze.bench.leader.cluster.registry.MetricsRepository;
import io.amaze.bench.runtime.agent.Agent;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.util.BenchRule;
import org.junit.After;
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
import static io.amaze.bench.actor.ProcessWatcherActorInput.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

/**
 * Created on 9/11/16.
 */
@Category(IntegrationTest.class)
@RunWith(Theories.class)
public final class WatcherActorsIntegrationTest {

    @DataPoints
    public static final boolean[] forked = {false, true};
    @DataPoints
    public static final BenchRule[] benchRules = new BenchRule[]{ //
            BenchRule.newJmsCluster(), //
            BenchRule.newJgroupsCluster() //
    };

    private static final String METRIC_KEY = "metric_key";
    private static final ActorKey SYSTEM_WATCHER = new ActorKey("SystemWatcher");
    private static final ActorKey PROCESS_WATCHER = new ActorKey("ProcessWatcher");

    @Rule
    public final Timeout globalTimeout = new Timeout(60, TimeUnit.SECONDS);

    private Agent agent;
    private MetricsRepository metricsRepository;
    private BenchRule benchRule;

    @After
    public void after() throws Exception {
        agent.close();
        benchRule.after();
    }

    @Theory
    public void create_and_initialize_watcher_actors(boolean forked, final BenchRule benchRule)
            throws ExecutionException {
        before(benchRule);
        Actors.ActorHandle systemWatcher = createSystemWatcher(forked);
        Actors.ActorHandle processesWatcher = createProcessWatcher(forked);

        getUninterruptibly(systemWatcher.initialize());
        getUninterruptibly(processesWatcher.initialize());
    }

    @Theory
    public void close_watcher_actors(boolean forked, final BenchRule benchRule) throws ExecutionException {
        before(benchRule);
        Actors.ActorHandle systemWatcher = createSystemWatcher(forked);
        Actors.ActorHandle processesWatcher = createProcessWatcher(forked);
        getUninterruptibly(systemWatcher.initialize());
        getUninterruptibly(processesWatcher.initialize());

        getUninterruptibly(systemWatcher.close());
        getUninterruptibly(processesWatcher.close());
    }

    @Theory
    public void start_system_monitoring(boolean forked, final BenchRule benchRule) throws ExecutionException {
        before(benchRule);
        Actors.ActorHandle systemWatcher = createAndInitSystemWatcher(forked);

        systemWatcher.send(WatcherActorsIntegrationTest.class.getName(), SystemWatcherInput.start(1));

        sleepUninterruptibly(2, TimeUnit.SECONDS);

        systemWatcher.send(WatcherActorsIntegrationTest.class.getName(), SystemWatcherInput.stop());

        systemWatcher.dumpMetrics();
        Future<MetricValuesMessage> metrics = metricsRepository.expectValuesFor(SYSTEM_WATCHER);
        getUninterruptibly(systemWatcher.close());
        sleepUninterruptibly(1, TimeUnit.SECONDS);
        assertThat(getUninterruptibly(metrics).metrics().size(), is(4));
    }

    /**
     * Here we monitor the {@link SystemWatcherActor} process using the {@link ProcessWatcherActor}
     *
     * @throws ExecutionException No expected
     */
    @Theory
    public void stopWatch_process_monitoring(boolean forked, final BenchRule benchRule) throws ExecutionException {
        before(benchRule);
        MetricsRepository metricsRepository = benchRule.metricsRepository();
        Future<MetricValuesMessage> metrics = metricsRepository.expectValuesFor(PROCESS_WATCHER);

        Actors.ActorHandle systemWatcher = createAndInitSystemWatcher(forked);
        Actors.ActorHandle processesWatcher = createProcessWatcher(forked);
        ActorDeployInfo deployInfo = getUninterruptibly(processesWatcher.initialize());

        processesWatcher.send(PROCESS_WATCHER.getName(), startStopwatch(deployInfo.getPid(), METRIC_KEY));

        sleepUninterruptibly(2, TimeUnit.SECONDS);

        processesWatcher.send(PROCESS_WATCHER.getName(), stopStopwatch(deployInfo.getPid(), METRIC_KEY));

        processesWatcher.dumpMetrics();
        sleepUninterruptibly(1, TimeUnit.SECONDS);
        assertThat(getUninterruptibly(metrics).metrics().size(), is(13));
        getUninterruptibly(systemWatcher.close());
        getUninterruptibly(processesWatcher.close());
    }

    @Theory
    public void sampling_process_monitoring(boolean forked, final BenchRule benchRule) throws ExecutionException {
        before(benchRule);
        Future<MetricValuesMessage> metrics = metricsRepository.expectValuesFor(PROCESS_WATCHER);

        Actors.ActorHandle systemWatcher = createAndInitSystemWatcher(forked);
        Actors.ActorHandle processesWatcher = createProcessWatcher(forked);
        ActorDeployInfo deployInfo = getUninterruptibly(processesWatcher.initialize());

        processesWatcher.send(PROCESS_WATCHER.getName(), startSampling(deployInfo.getPid(), 1, METRIC_KEY));

        sleepUninterruptibly(2, TimeUnit.SECONDS);

        processesWatcher.send(PROCESS_WATCHER.getName(), stopSampling(deployInfo.getPid()));

        processesWatcher.dumpMetrics();
        sleepUninterruptibly(1, TimeUnit.SECONDS);
        assertTrue(getUninterruptibly(metrics).metrics().size() > 0);
        getUninterruptibly(systemWatcher.close());
        getUninterruptibly(processesWatcher.close());
    }

    private void before(final BenchRule benchRule) {
        this.benchRule = benchRule;
        benchRule.before();
        try {
            agent = getUninterruptibly(this.benchRule.agents().create(new AgentKey("test-agent-1")));
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
        metricsRepository = benchRule.metricsRepository();
    }

    private Actors.ActorHandle createSystemWatcher(final boolean forked) throws ExecutionException {
        Actors.ActorHandle systemWatcher = benchRule.actors().create(systemActorConfig(forked));
        getUninterruptibly(systemWatcher.actorCreation());
        return systemWatcher;
    }

    private Actors.ActorHandle createProcessWatcher(final boolean forked) throws ExecutionException {
        Actors.ActorHandle processesWatcher = benchRule.actors().create(processActorConfig(forked));
        getUninterruptibly(processesWatcher.actorCreation());
        return processesWatcher;
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
        Actors.ActorHandle systemWatcher = createSystemWatcher(forked);
        getUninterruptibly(systemWatcher.initialize());
        return systemWatcher;
    }
}
