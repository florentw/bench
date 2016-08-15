package io.amaze.bench.client.runtime.actor;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.Test;

import static io.amaze.bench.client.runtime.actor.TestActor.DUMMY_CONFIG;
import static io.amaze.bench.client.runtime.actor.TestActor.configForActor;

/**
 * Created on 4/6/16.
 */
public final class ActorConfigTest {

    @Test
    public void equality() {
        EqualsTester equalsTester = new EqualsTester();
        equalsTester.addEqualityGroup(DUMMY_CONFIG, configForActor(TestActor.class));
        equalsTester.addEqualityGroup(DUMMY_CONFIG.getDeployConfig(),
                                      configForActor(TestActor.class).getDeployConfig());
        equalsTester.addEqualityGroup(DUMMY_CONFIG.hashCode(), configForActor(TestActor.class).hashCode());
        equalsTester.testEquals();
    }

    @Test
    public void null_parameters_for_constructor() {
        NullPointerTester test = new NullPointerTester();
        test.setDefault(DeployConfig.class, DUMMY_CONFIG.getDeployConfig());
        test.testAllPublicConstructors(ActorConfig.class);
        test.testAllPublicConstructors(DeployConfig.class);
    }

}