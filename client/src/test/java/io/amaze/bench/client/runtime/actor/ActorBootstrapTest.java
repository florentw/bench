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

import com.google.common.testing.NullPointerTester;
import com.google.common.util.concurrent.Uninterruptibles;
import io.amaze.bench.client.runtime.agent.DummyClientFactory;
import io.amaze.bench.client.runtime.agent.RecorderOrchestratorActor;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorActor;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import io.amaze.bench.shared.util.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created on 3/14/16.
 */
public final class ActorBootstrapTest {

    private static final String DUMMY_HOST = "dummyhost";
    private static final String DUMMY_PORT = "1337";
    private static final String CONF_PREFIX = "actor";
    private static final String CONF_SUFFIX = ".json";
    private static final String DUMMY = "dummy";

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private ActorBootstrap actorBootstrap;

    @Before
    public void before() {
        OrchestratorActor client = new RecorderOrchestratorActor();
        OrchestratorClientFactory factory = new DummyClientFactory(null, client);
        actorBootstrap = new ActorBootstrap(factory);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(ActorBootstrap.class);
        tester.testAllPublicStaticMethods(ActorBootstrap.class);
        tester.testAllPublicInstanceMethods(actorBootstrap);
    }

    @Test(expected = ValidationException.class)
    public void create_actor_empty_config_throws() throws IOException, ValidationException {
        File actorConf = File.createTempFile(CONF_PREFIX, CONF_SUFFIX);
        actorBootstrap.createActor(TestActor.DUMMY_ACTOR, TestActor.class.getName(), actorConf.getAbsolutePath());
    }

    @Test(expected = ValidationException.class)
    public void create_invalid_actor_throws() throws IOException, ValidationException {
        File actorConf = File.createTempFile(CONF_PREFIX, CONF_SUFFIX);
        actorBootstrap.createActor(TestActor.DUMMY_ACTOR, String.class.getName(), actorConf.getAbsolutePath());
    }

    @Test
    public void shutdown_thread_closes_actor() throws IOException, ValidationException {
        RuntimeActor actor = actorBootstrap.createActor(TestActor.DUMMY_ACTOR,
                                                        TestActor.class.getName(),
                                                        TestActor.DUMMY_JSON_CONFIG);
        actor = spy(actor);

        ActorBootstrap.ActorShutdownThread thread = new ActorBootstrap.ActorShutdownThread(actor);
        thread.start();

        Uninterruptibles.joinUninterruptibly(thread);
        verify(actor).close();
    }

    @Test
    public void install_shutdown_hook() {
        RuntimeActor actor = mock(RuntimeActor.class);

        Thread thread = ActorBootstrap.installShutdownHook(actor);

        assertNotNull(thread);
        boolean removed = Runtime.getRuntime().removeShutdownHook(thread);
        assertThat(removed, is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void main_wrong_usage_throws() throws IOException, ValidationException {
        ActorBootstrap.main(new String[]{"", ""});
    }

    @Test
    public void main_reads_temporary_config_file_and_deletes() throws IOException, ValidationException {
        File tmpConfigFile = folder.newFile();
        Files.writeTo(tmpConfigFile, "{}");

        try {
            ActorBootstrap.main(new String[]{TestActor.DUMMY_ACTOR, DUMMY, DUMMY_HOST, DUMMY_PORT, tmpConfigFile.getAbsolutePath()});
        } catch (ValidationException ignore) {
        }

        assertThat(tmpConfigFile.exists(), is(false));
    }

    @Test(expected = ValidationException.class)
    public void main_invalid_class_throws() throws IOException, ValidationException {
        File tmpConfigFile = folder.newFile();
        Files.writeTo(tmpConfigFile, TestActor.DUMMY_JSON_CONFIG);
        ActorBootstrap.main(new String[]{TestActor.DUMMY_ACTOR, DUMMY, DUMMY_HOST, DUMMY_PORT, tmpConfigFile.getAbsolutePath()});
    }

    @Test(expected = RuntimeException.class)
    public void main_orchestrator_client_throws() throws IOException, ValidationException {
        File tmpConfigFile = folder.newFile();
        Files.writeTo(tmpConfigFile, TestActor.DUMMY_JSON_CONFIG);
        ActorBootstrap.main(new String[]{TestActor.DUMMY_ACTOR, TestActor.class.getName(), DUMMY_HOST, DUMMY_PORT, tmpConfigFile.getAbsolutePath()});
    }
}