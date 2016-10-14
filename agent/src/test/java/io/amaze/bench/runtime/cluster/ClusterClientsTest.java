package io.amaze.bench.runtime.cluster;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.util.ClusterConfigs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static io.amaze.bench.util.ClusterConfigs.CLIENT_FACTORY_CLASS;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Created on 10/14/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ClusterClientsTest {

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(ClusterClients.class);
    }

    @Test
    public void factory_is_created_from_cluster_config() {
        ClusterClientFactory factory = ClusterClients.newFactory(ClusterConfigs.clusterConfig());

        assertNotNull(factory);
        assertThat(factory, instanceOf(CLIENT_FACTORY_CLASS));
    }

    @Test(expected = RuntimeException.class)
    public void factory_propagates_class_not_found() {
        ClusterClients.newFactory(ClusterConfigs.invalidClassClusterConfig());
    }

}