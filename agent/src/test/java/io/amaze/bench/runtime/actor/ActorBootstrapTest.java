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

import com.google.common.testing.NullPointerTester;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import io.amaze.bench.Endpoint;
import io.amaze.bench.runtime.agent.DummyClientFactory;
import io.amaze.bench.runtime.cluster.ActorClusterClient;
import io.amaze.bench.runtime.cluster.ActorRegistrySender;
import io.amaze.bench.runtime.cluster.ClusterClientFactory;
import io.amaze.bench.runtime.cluster.actor.RuntimeActor;
import io.amaze.bench.runtime.cluster.actor.ValidationException;
import io.amaze.bench.runtime.cluster.registry.ActorRegistryClusterClient;
import io.amaze.bench.shared.util.Files;
import io.amaze.bench.util.ClusterConfigs;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static com.google.common.util.concurrent.Uninterruptibles.joinUninterruptibly;
import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created on 3/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ActorBootstrapTest {

    private static final String CONF_PREFIX = "actor";
    private static final String CONF_SUFFIX = ".json";
    private static final String DUMMY = "dummy";

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private ActorClusterClient client;
    @Mock
    private ActorRegistryClusterClient actorRegistryClient;

    private ActorBootstrap actorBootstrap;

    @Before
    public void before() throws IOException, ValidationException {
        when(client.localEndpoint()).thenReturn(mock(Endpoint.class));
        when(client.actorRegistrySender()).thenReturn(mock(ActorRegistrySender.class));
        ClusterClientFactory factory = new DummyClientFactory(null, null, client, actorRegistryClient, null, null);
        actorBootstrap = spy(new ActorBootstrap(factory));
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ActorBootstrap.class);
        tester.testAllPublicStaticMethods(ActorBootstrap.class);
    }

    @Test(expected = ValidationException.class)
    public void create_actor_empty_config_throws() throws IOException, ValidationException {
        File actorConf = File.createTempFile(CONF_PREFIX, CONF_SUFFIX);
        actorBootstrap.createActor(DUMMY_ACTOR, TestActor.class.getName(), actorConf.getAbsolutePath());
    }

    @Test(expected = ValidationException.class)
    public void create_invalid_actor_throws() throws IOException, ValidationException {
        File actorConf = File.createTempFile(CONF_PREFIX, CONF_SUFFIX);
        actorBootstrap.createActor(DUMMY_ACTOR, String.class.getName(), actorConf.getAbsolutePath());
    }

    @Test
    public void shutdown_thread_closes_actor_bootstrap() throws IOException, ValidationException {
        RuntimeActor actor = actorBootstrap.createActor(DUMMY_ACTOR,
                                                        TestActor.class.getName(),
                                                        TestActor.DUMMY_JSON_CONFIG);
        actor = spy(actor);

        ActorBootstrap.ActorShutdownThread thread = new ActorBootstrap.ActorShutdownThread(actorBootstrap, actor);
        thread.start();

        joinUninterruptibly(thread);
        verify(actorBootstrap).close();
    }

    @Test
    public void closing_bootstrap_twice_does_not_throw() {
        actorBootstrap.close();
        actorBootstrap.close();
    }

    @Test
    public void install_shutdown_hook() {
        Thread thread = ActorBootstrap.installShutdownHook(actorBootstrap, mock(RuntimeActor.class));

        assertNotNull(thread);
        boolean removed = Runtime.getRuntime().removeShutdownHook(thread);
        assertThat(removed, is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void main_wrong_usage_throws() throws IOException, ValidationException {
        ActorBootstrap.main(new String[]{"", ""});
    }

    @Test
    public void main_reads_and_deletes_temporary_config_files() throws IOException, ValidationException {
        File clusterConfigFile = writeClusterConfigFile();
        File actorConfigFile = writeActorConfig();

        try {
            ActorBootstrap.main(new String[]{DUMMY_ACTOR.getName(), DUMMY, clusterConfigFile.getAbsolutePath(), actorConfigFile.getAbsolutePath()});
        } catch (ValidationException ignore) {
        }

        assertThat(clusterConfigFile.exists(), is(false));
        assertThat(actorConfigFile.exists(), is(false));
    }

    @Test(expected = ValidationException.class)
    public void main_invalid_class_throws() throws IOException, ValidationException {
        File actorConfigFile = writeActorConfig();
        File clusterConfigFile = writeClusterConfigFile();

        ActorBootstrap.main(new String[]{DUMMY_ACTOR.getName(), DUMMY, clusterConfigFile.getAbsolutePath(), actorConfigFile.getAbsolutePath()});
    }

    @Test(expected = ValidationException.class)
    public void main_cluster_client_throws_ValidationException_on_invalid_config()
            throws IOException, ValidationException {
        File tmpConfigFile = writeActorConfig();
        File clusterConfigFile = folder.newFile();

        ActorBootstrap.main(new String[]{DUMMY_ACTOR.getName(), TestActor.class.getName(), clusterConfigFile.getAbsolutePath(), tmpConfigFile.getAbsolutePath()});
    }

    private File writeActorConfig() throws IOException {
        File actorConfigFile = folder.newFile();
        Files.writeTo(actorConfigFile, TestActor.DUMMY_JSON_CONFIG);
        return actorConfigFile;
    }

    private File writeClusterConfigFile() throws IOException {
        Config clusterConfig = ClusterConfigs.defaultConfig();
        File clusterConfigFile = folder.newFile();
        Files.writeTo(clusterConfigFile, clusterConfig.root().render(ConfigRenderOptions.concise()));
        return clusterConfigFile;
    }
}