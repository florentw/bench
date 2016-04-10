package io.amaze.bench.client.runtime.actor;

import com.google.common.util.concurrent.Uninterruptibles;
import io.amaze.bench.client.runtime.agent.AgentTest;
import io.amaze.bench.client.runtime.agent.DummyClientFactory;
import io.amaze.bench.client.runtime.agent.RecorderOrchestratorClient;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClient;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import io.amaze.bench.shared.helper.FileHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created on 3/14/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
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
        OrchestratorClient client = new RecorderOrchestratorClient();
        OrchestratorClientFactory factory = new DummyClientFactory(null, client);
        actorBootstrap = new ActorBootstrap(DUMMY, factory);
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
        Actor actor = actorBootstrap.createActor(TestActor.DUMMY_ACTOR,
                                                 TestActor.class.getName(),
                                                 TestActor.DUMMY_JSON_CONFIG);
        actor = Mockito.spy(actor);

        ActorBootstrap.ActorShutdownThread thread = new ActorBootstrap.ActorShutdownThread(actor);
        thread.start();

        Uninterruptibles.joinUninterruptibly(thread);
        verify(actor).close();
    }

    @Test
    public void install_shutdown_hook() {
        Actor actor = mock(Actor.class);

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
        FileHelper.writeToFile(tmpConfigFile, "{}");

        try {
            ActorBootstrap.main(new String[]{AgentTest.DUMMY_AGENT, TestActor.DUMMY_ACTOR, DUMMY, DUMMY_HOST, DUMMY_PORT, tmpConfigFile.getAbsolutePath()});
        } catch (ValidationException ignore) {
        }

        assertThat(tmpConfigFile.exists(), is(false));
    }

    @Test(expected = ValidationException.class)
    public void main_invalid_class_throws() throws IOException, ValidationException {
        File tmpConfigFile = folder.newFile();
        FileHelper.writeToFile(tmpConfigFile, TestActor.DUMMY_JSON_CONFIG);
        ActorBootstrap.main(new String[]{AgentTest.DUMMY_AGENT, TestActor.DUMMY_ACTOR, DUMMY, DUMMY_HOST, DUMMY_PORT, tmpConfigFile.getAbsolutePath()});
    }

    @Test(expected = RuntimeException.class)
    public void main_orchestrator_client_throws() throws IOException, ValidationException {
        File tmpConfigFile = folder.newFile();
        FileHelper.writeToFile(tmpConfigFile, TestActor.DUMMY_JSON_CONFIG);
        ActorBootstrap.main(new String[]{AgentTest.DUMMY_AGENT, TestActor.DUMMY_ACTOR, TestActor.class.getName(), DUMMY_HOST, DUMMY_PORT, tmpConfigFile.getAbsolutePath()});
    }
}