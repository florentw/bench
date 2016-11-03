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
package io.amaze.bench.runtime.actor;

import com.google.common.base.Throwables;
import com.google.common.testing.NullPointerTester;
import io.amaze.bench.cluster.ClusterConfigFactory;
import io.amaze.bench.cluster.actor.ActorConfig;
import io.amaze.bench.cluster.actor.DeployConfig;
import io.amaze.bench.cluster.actor.ValidationException;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.shared.util.Files;
import io.amaze.bench.util.AgentClusterRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.joinUninterruptibly;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.amaze.bench.cluster.agent.AgentUtil.DUMMY_AGENT;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Created on 3/16/16.
 */
@Category(IntegrationTest.class)
@RunWith(Theories.class)
public final class ForkedActorManagerTest {

    @DataPoints
    public static final AgentClusterRule[] agentClusters = new AgentClusterRule[]{ //
            AgentClusterRule.newJmsAgentCluster(), //
            AgentClusterRule.newJgroupsAgentCluster() //
    };

    private static final Logger log = LogManager.getLogger();
    private static final int MAX_TIMEOUT_SEC = 30;

    @Rule
    public final Timeout globalTimeout = new Timeout(MAX_TIMEOUT_SEC, TimeUnit.SECONDS);
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private ForkedActorManager actorManager;
    private ClusterConfigFactory clusterConfigFactory;
    private AgentClusterRule agentCluster;

    @Theory
    public void null_parameters_invalid(final AgentClusterRule agentCluster) {
        before(agentCluster);
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ForkedActorManager.class);
        tester.testAllPublicInstanceMethods(actorManager);
    }

    @Theory
    public void create_actor_init_called(final AgentClusterRule agentCluster)
            throws ValidationException, IOException, InterruptedException {
        before(agentCluster);
        File rdvFile = folder.newFile();
        ActorConfig actorConfig = configWithInitRdv(TestActorWriter.class.getName(), rdvFile);

        ManagedActor actor = actorManager.createActor(actorConfig);

        assertNotNull(actor);
        assertThat(actor.getKey(), is(DUMMY_ACTOR));
        verifyFileContentWithin(rdvFile, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    @Theory
    public void create_actor_twice_throws(final AgentClusterRule agentCluster)
            throws ValidationException, IOException, InterruptedException {
        before(agentCluster);
        File rdvFile = folder.newFile();
        ActorConfig actorConfig = configWithInitRdv(TestActorWriter.class.getName(), rdvFile);
        ManagedActor actor = actorManager.createActor(actorConfig);
        verifyFileContentWithin(rdvFile, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        try {
            expectedException.expect(IllegalArgumentException.class);
            actorManager.createActor(actorConfig);
        } finally {
            actor.close();
        }
    }

    @Theory
    public void close_actor_and_watchdog_detects(final AgentClusterRule agentCluster)
            throws ValidationException, IOException, InterruptedException {
        before(agentCluster);
        File rdvFileInit = folder.newFile();
        ActorConfig actorConfig = configWithInitRdv(TestActorWriter.class.getName(), rdvFileInit);
        ManagedActor actor = actorManager.createActor(actorConfig);

        ProcessWatchDogThread watchDogThread = actorManager.getProcesses().get(DUMMY_ACTOR);
        assertNotNull(watchDogThread);

        // Sync with process
        verifyFileContentWithin(rdvFileInit, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        actor.close();

        // We do not check the fact that the shutdown hook is actually called here
        joinUninterruptibly(watchDogThread);

        assertThat(actorManager.getProcesses().size(), is(0));
        assertThat(watchDogThread.hasProcessExited(), is(true));
    }

    @Theory
    public void actor_process_is_killed_and_watchdog_detects(final AgentClusterRule agentCluster)
            throws ValidationException, IOException, InterruptedException {
        before(agentCluster);
        File rdvFileInit = folder.newFile();
        ActorConfig actorConfig = suicideConfig(TestActorWriter.class.getName(), rdvFileInit);
        actorManager.createActor(actorConfig);

        ProcessWatchDogThread watchDogThread = actorManager.getProcesses().get(DUMMY_ACTOR);
        verifyFileContentWithin(rdvFileInit, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        // We do not check the fact that the shutdown hook is actually called here
        joinUninterruptibly(watchDogThread);

        assertThat(actorManager.getProcesses().size(), is(0));
        assertThat(watchDogThread.hasProcessExited(), is(true));
    }

    @Theory
    public void close_actor_twice(final AgentClusterRule agentCluster)
            throws ValidationException, IOException, InterruptedException {
        before(agentCluster);
        File rdvFileInit = folder.newFile();
        ActorConfig actorConfig = configWithInitRdv(TestActorWriter.class.getName(), rdvFileInit);
        ManagedActor actor = actorManager.createActor(actorConfig);

        ProcessWatchDogThread watchDogThread = actorManager.getProcesses().get(DUMMY_ACTOR);

        verifyFileContentWithin(rdvFileInit, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        actor.close();
        actor.close();

        // We do not check the fact that the shutdown hook is actually called here
        joinUninterruptibly(watchDogThread);

        assertThat(actorManager.getProcesses().size(), is(0));
        assertThat(watchDogThread.hasProcessExited(), is(true));
    }

    @Theory
    public void close_manager_closes_actor(final AgentClusterRule agentCluster)
            throws ValidationException, IOException, InterruptedException {
        before(agentCluster);
        File rdvFileInit = folder.newFile();
        ActorConfig actorConfig = configWithInitRdv(TestActorWriter.class.getName(), rdvFileInit);
        ManagedActor actor = actorManager.createActor(actorConfig);
        assertNotNull(actor);

        ProcessWatchDogThread watchDogThread = actorManager.getProcesses().get(DUMMY_ACTOR);

        verifyFileContentWithin(rdvFileInit, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        actorManager.close();

        // We do not check the fact that the shutdown hook is actually called here
        joinUninterruptibly(watchDogThread);

        assertThat(actorManager.getProcesses().size(), is(0));
        assertThat(watchDogThread.hasProcessExited(), is(true));
    }

    @Theory
    public void create_manager_but_cant_create_local_log_dir(final AgentClusterRule agentCluster) throws IOException {
        before(agentCluster);
        File folder = this.folder.newFolder();
        File localLogDir = spy(folder);
        doReturn(false).when(localLogDir).mkdirs(); // NOSONAR
        doReturn(false).when(localLogDir).exists(); // NOSONAR

        expectedException.expect(IllegalStateException.class);
        actorManager = new ForkedActorManager(DUMMY_AGENT, clusterConfigFactory, localLogDir);
    }

    @Theory
    public void watchdog_thread_is_interrupted_while_waitfor(final AgentClusterRule agentCluster)
            throws InterruptedException {
        before(agentCluster);
        Process mockedProcess = mock(Process.class);
        when(mockedProcess.waitFor()).thenThrow(new InterruptedException());
        ProcessWatchDogThread watchdog = new ProcessWatchDogThread(DUMMY_ACTOR.getName(), mockedProcess, actorManager);
        watchdog.start();
        watchdog.awaitUntilStarted();
        watchdog.close();

        joinUninterruptibly(watchdog);

        assertThat(watchdog.hasProcessExited(), is(false));
    }

    @After
    public void after() {
        actorManager.close();
        agentCluster.after();
    }

    private void before(final AgentClusterRule agentCluster) {
        this.agentCluster = agentCluster;
        this.agentCluster.before();
        clusterConfigFactory = agentCluster.clusterConfigFactory();
        actorManager = new ForkedActorManager(DUMMY_AGENT, clusterConfigFactory, new File("target/logs"));
    }

    private ActorConfig suicideConfig(final String className, final File rdvFileInit) {

        DeployConfig deployConfig = new DeployConfig(true, Collections.emptyList());

        String jsonConfig = "{\"" + TestActorWriter.INIT_FILE_CONFIG + "\":\"" + rdvFileInit.getAbsolutePath() + "\"," + //
                " \"" + TestActorWriter.SUICIDE_AFTER_MS + "\":500}";

        return new ActorConfig(DUMMY_ACTOR, className, deployConfig, jsonConfig);
    }

    private ActorConfig configWithInitRdv(final String className, final File rdvFile) {

        DeployConfig deployConfig = new DeployConfig(true, Collections.emptyList());

        String jsonConfig = "{\"" + TestActorWriter.INIT_FILE_CONFIG + "\":\"" + rdvFile.getAbsolutePath() + "\"}";

        return new ActorConfig(DUMMY_ACTOR, className, deployConfig, jsonConfig);
    }

    private void verifyFileContentWithin(final File file,
                                         final String expectedContent,
                                         final long timeout,
                                         final TimeUnit unit) {

        long timeoutMs = unit.toMillis(timeout);
        long t0 = System.currentTimeMillis();

        while ((System.currentTimeMillis() - t0) < timeoutMs) {
            String content;
            try {
                content = Files.read(file.getAbsolutePath());
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
            if (expectedContent.equals(content)) {
                return;
            }
            log.debug("Condition not met yet, sleeping for 500ms");
            sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
        }

        fail("Condition not verified on time: " + file.getAbsolutePath() + " did not contain " + expectedContent + " within " + timeoutMs + "ms");
    }
}