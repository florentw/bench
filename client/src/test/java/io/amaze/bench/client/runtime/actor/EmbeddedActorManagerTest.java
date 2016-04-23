package io.amaze.bench.client.runtime.actor;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.agent.DummyClientFactory;
import io.amaze.bench.client.runtime.agent.RecorderOrchestratorActor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_CONFIG;
import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;


/**
 * Created on 3/14/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class EmbeddedActorManagerTest {

    private RecorderOrchestratorActor client;
    private ActorManager actorManager;
    private ActorFactory actorFactory;

    @Before
    public void before() {
        client = Mockito.spy(new RecorderOrchestratorActor());

        DummyClientFactory factory = new DummyClientFactory(null, client);
        actorFactory = new ActorFactory(DUMMY_AGENT, factory);
        actorManager = new EmbeddedActorManager(DUMMY_AGENT, actorFactory);
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorFactory.class, actorFactory);
        tester.testAllPublicConstructors(EmbeddedActorManager.class);
        tester.testAllPublicInstanceMethods(actorManager);
    }

    @Test
    public void create_actor() throws ValidationException, InterruptedException {
        ManagedActor actor = actorManager.createActor(DUMMY_CONFIG);
        assertNotNull(actor);
        verify(client).startActorListener(any(Actor.class));
    }

    @Test
    public void create_close_actor() throws Exception {
        ManagedActor actor = actorManager.createActor(DUMMY_CONFIG);
        actor.close();
        verify(client).close();
    }

    @Test
    public void close_manager() {
        actorManager.close();
    }
}