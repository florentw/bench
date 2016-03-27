package io.amaze.bench.client.runtime.actor;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;
import io.amaze.bench.client.runtime.agent.AgentTest;
import io.amaze.bench.client.runtime.agent.Constants;
import io.amaze.bench.shared.helper.FileHelper;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.test.IntegrationTest;
import io.amaze.bench.shared.test.JMSServerRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_ACTOR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Created on 3/16/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@Category(IntegrationTest.class)
public class ForkedActorManagerTest {

    private static final Logger LOG = LoggerFactory.getLogger(ForkedActorManagerTest.class);
    private static final int MAX_TIMEOUT_SEC = 30;

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final JMSServerRule server = new JMSServerRule();

    private ForkedActorManager actorManager;

    private static void verifyFileContentWithin(File file, String expectedContent, long timeout, TimeUnit unit) {

        long timeoutMs = unit.toMillis(timeout);
        long t0 = System.currentTimeMillis();
        long current = t0;

        while ((current - t0) < timeoutMs) {
            String content = null;
            try {
                content = FileHelper.readFile(file.getAbsolutePath());
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            if (expectedContent.equals(content)) {
                return;
            }
            LOG.debug("Condition not met yet, sleeping for 500ms");
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
            current = System.currentTimeMillis();
        }

        fail("Condition not verified on time: " + file.getAbsolutePath() + " did not contain " + expectedContent + " within " + timeoutMs + "ms");
    }

    @Before
    public void before() throws JMSException {
        AgentTest.RecorderOrchestratorClient client = spy(new AgentTest.RecorderOrchestratorClient());

        AgentTest.DummyClientFactory factory = new AgentTest.DummyClientFactory(null, client);
        actorManager = new ForkedActorManager(new ActorFactory(factory));

        server.getServer().createQueue(Constants.MASTER_ACTOR_NAME);
        server.getServer().createQueue(DUMMY_ACTOR);
    }

    @Test
    public void create_actor_init_called() throws ValidationException, IOException, InterruptedException {
        File rdvFile = folder.newFile();
        String jsonConfig = "{" + //
                "\"master\":{\"host\":\"" + server.getHost() + "\",\"port\":" + server.getPort() + "}," + //
                "\"" + TestActorWriter.INIT_FILE_CONFIG + "\":\"" + rdvFile.getAbsolutePath() + "\"" + //
                "}";

        ManagedActor actor = actorManager.createActor(DUMMY_ACTOR, TestActorWriter.class.getName(), jsonConfig);

        assertNotNull(actor);
        assertThat(actor.name(), is(DUMMY_ACTOR));

        verifyFileContentWithin(rdvFile, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);
        actorManager.close();
    }

    @Test
    public void close_actor_after_called_and_watchdog_detects() throws ValidationException, IOException, InterruptedException {
        File rdvFileInit = folder.newFile();
        File rdvFileAfter = folder.newFile();

        String jsonConfig = "{" + //
                "\"master\":{\"host\":\"" + server.getHost() + "\",\"port\":" + server.getPort() + "}," + //
                "\"" + TestActorWriter.INIT_FILE_CONFIG + "\":\"" + rdvFileInit.getAbsolutePath() + "\"," + // For sync purposes
                "\"" + TestActorWriter.AFTER_FILE_CONFIG + "\":\"" + rdvFileAfter.getAbsolutePath() + "\"" + // To actually test
                "}";

        ManagedActor actor = actorManager.createActor(DUMMY_ACTOR, TestActorWriter.class.getName(), jsonConfig);

        ForkedActorWatchDogThread watchDogThread = actorManager.getProcesses().get(DUMMY_ACTOR);

        // Sync with process
        verifyFileContentWithin(rdvFileInit, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        actor.close();

        verifyFileContentWithin(rdvFileAfter, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        // Here we sleep a little bit to let time for the forked JVM to die
        Thread.sleep(2000); // NOSONAR

        assertThat(actorManager.getProcesses().size(), is(0));
        assertThat(watchDogThread.hasExited(), is(true));

        actorManager.close();
    }

    @Test
    public void close_actor_twice() throws ValidationException, IOException, InterruptedException {
        File rdvFileInit = folder.newFile();
        File rdvFileAfter = folder.newFile();

        String jsonConfig = "{" + //
                "\"master\":{\"host\":\"" + server.getHost() + "\",\"port\":" + server.getPort() + "}," + //
                "\"" + TestActorWriter.INIT_FILE_CONFIG + "\":\"" + rdvFileInit.getAbsolutePath() + "\"," + // For sync purposes
                "\"" + TestActorWriter.AFTER_FILE_CONFIG + "\":\"" + rdvFileAfter.getAbsolutePath() + "\"" + // To actually test
                "}";

        ManagedActor actor = actorManager.createActor(DUMMY_ACTOR, TestActorWriter.class.getName(), jsonConfig);

        verifyFileContentWithin(rdvFileInit, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        actor.close();
        actor.close();

        verifyFileContentWithin(rdvFileAfter, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        actorManager.close();
    }

    @Test
    public void close_manager_closes_actor() throws ValidationException, IOException, InterruptedException {
        File rdvFileInit = folder.newFile();
        File rdvFileAfter = folder.newFile();

        String jsonConfig = "{" + //
                "\"master\":{\"host\":\"" + server.getHost() + "\",\"port\":" + server.getPort() + "}," + //
                "\"" + TestActorWriter.INIT_FILE_CONFIG + "\":\"" + rdvFileInit.getAbsolutePath() + "\"," + // For sync purposes
                "\"" + TestActorWriter.AFTER_FILE_CONFIG + "\":\"" + rdvFileAfter.getAbsolutePath() + "\"" + // To actually test
                "}";

        ManagedActor actor = actorManager.createActor(DUMMY_ACTOR, TestActorWriter.class.getName(), jsonConfig);
        assertNotNull(actor);

        verifyFileContentWithin(rdvFileInit, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);

        actorManager.close();

        verifyFileContentWithin(rdvFileAfter, TestActorWriter.OK, MAX_TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    @Test(expected = IllegalStateException.class)
    public void create_manager_but_cant_create_local_log_dir() throws IOException {
        AgentTest.DummyClientFactory factory = new AgentTest.DummyClientFactory(null, null);
        File folder = this.folder.newFolder();
        File localLogDir = spy(folder);
        doReturn(false).when(localLogDir).mkdirs(); // NOSONAR
        doReturn(false).when(localLogDir).exists(); // NOSONAR

        actorManager = new ForkedActorManager(new ActorFactory(factory), localLogDir);
    }

}