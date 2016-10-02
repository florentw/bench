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
import io.amaze.bench.shared.jms.JMSEndpoint;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.shared.test.JMSServerRule;
import io.amaze.bench.shared.util.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.joinUninterruptibly;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Created on 3/16/16.
 */
@Category(IntegrationTest.class)
public final class ForkedActorManagerTest {

    private static final Logger log = LogManager.getLogger();

    private static final int MAX_TIMEOUT_SEC = 30;

    @Rule
    public final Timeout globalTimeout = new Timeout(5, TimeUnit.SECONDS);

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final JMSServerRule server = new JMSServerRule();

    private ForkedActorManager actorManager;
    private JMSEndpoint masterEndpoint;

    @Before
    public void before() throws JMSException, IOException {
        masterEndpoint = server.getEndpoint();

        actorManager = new ForkedActorManager(DUMMY_AGENT, masterEndpoint, new File("target/logs"));

        server.getServer().createQueue(DUMMY_ACTOR.getName());
    }

    @After
    public void after() {
        actorManager.close();
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ForkedActorManager.class);
        tester.testAllPublicInstanceMethods(actorManager);
    }

    @Test
    public void create_actor_init_called() throws ValidationException, IOException, InterruptedException {
        File rdvFile = folder.newFile();
        ActorConfig actorConfig = configWithInitRdv(TestActorWriter.class.getName(), rdvFile);

        ManagedActor actor = actorManager.createActor(actorConfig);

        assertNotNull(actor);
        assertThat(actor.getKey(), is(DUMMY_ACTOR));
        verifyFileContentWithin(rdvFile, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_actor_twice_throws() throws ValidationException, IOException, InterruptedException {
        File rdvFile = folder.newFile();
        ActorConfig actorConfig = configWithInitRdv(TestActorWriter.class.getName(), rdvFile);
        actorManager.createActor(actorConfig);
        verifyFileContentWithin(rdvFile, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        actorManager.createActor(actorConfig);
    }

    @Test
    public void close_actor_and_watchdog_detects() throws ValidationException, IOException, InterruptedException {
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

        actorManager.close();
    }

    @Test
    public void actor_process_is_killed_and_watchdog_detects()
            throws ValidationException, IOException, InterruptedException {

        File rdvFileInit = folder.newFile();
        ActorConfig actorConfig = suicideConfig(TestActorWriter.class.getName(), rdvFileInit);
        actorManager.createActor(actorConfig);

        ProcessWatchDogThread watchDogThread = actorManager.getProcesses().get(DUMMY_ACTOR);
        verifyFileContentWithin(rdvFileInit, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        // We do not check the fact that the shutdown hook is actually called here
        joinUninterruptibly(watchDogThread);

        assertThat(actorManager.getProcesses().size(), is(0));
        assertThat(watchDogThread.hasProcessExited(), is(true));

        actorManager.close();
    }

    @Test
    public void close_actor_twice() throws ValidationException, IOException, InterruptedException {
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

        actorManager.close();
    }

    @Test
    public void close_manager_closes_actor() throws ValidationException, IOException, InterruptedException {
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

    @Test(expected = IllegalStateException.class)
    public void create_manager_but_cant_create_local_log_dir() throws IOException {
        File folder = this.folder.newFolder();
        File localLogDir = spy(folder);
        doReturn(false).when(localLogDir).mkdirs(); // NOSONAR
        doReturn(false).when(localLogDir).exists(); // NOSONAR

        actorManager = new ForkedActorManager(DUMMY_AGENT, masterEndpoint, localLogDir);
    }

    @Test
    public void watchdog_thread_is_interrupted_while_waitfor() throws InterruptedException {
        Process mockedProcess = mock(Process.class);
        when(mockedProcess.waitFor()).thenThrow(new InterruptedException());
        ProcessWatchDogThread watchdog = new ProcessWatchDogThread(DUMMY_ACTOR.getName(), mockedProcess, actorManager);
        watchdog.start();
        watchdog.awaitUntilStarted();
        watchdog.close();

        joinUninterruptibly(watchdog);

        assertThat(watchdog.hasProcessExited(), is(false));
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
            String content = null;
            try {
                content = Files.read(file.getAbsolutePath());
            } catch (IOException e) {
                Throwables.propagate(e);
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