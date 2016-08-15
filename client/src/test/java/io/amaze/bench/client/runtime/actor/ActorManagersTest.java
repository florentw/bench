package io.amaze.bench.client.runtime.actor;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.client.runtime.orchestrator.OrchestratorClientFactory;
import org.junit.Before;
import org.junit.Test;

import static io.amaze.bench.client.runtime.agent.AgentTest.DUMMY_AGENT;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created on 4/9/16.
 */
public final class ActorManagersTest {

    private ActorManagers actorManagers;

    @Before
    public void before() {
        actorManagers = new ActorManagers();
    }

    @Test
    public void null_parameters_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(ActorManagers.class);
        tester.testAllPublicInstanceMethods(actorManagers);
    }

    @Test
    public void can_supply_embedded_manager() {
        try (ActorManager embedded = actorManagers.createEmbedded(DUMMY_AGENT, mock(OrchestratorClientFactory.class))) {
            assertNotNull(embedded);
            assertTrue(embedded instanceof EmbeddedActorManager);
        }
    }

    @Test
    public void can_supply_forked_manager() {
        try (ActorManager forked = actorManagers.createForked(DUMMY_AGENT)) {
            assertNotNull(forked);
            assertTrue(forked instanceof ForkedActorManager);
        }
    }


}