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
package io.amaze.bench.leader.cluster.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.leader.cluster.registry.MetricsRepositoryListener;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.MessageListener;

import static io.amaze.bench.runtime.cluster.agent.Constants.METRICS_TOPIC;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 10/3/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSMetricsRepositoryClusterClientTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private JMSClient jmsClient;
    @Mock
    private MetricsRepositoryListener metricsListener;

    private JMSMetricsRepositoryClusterClient clusterClient;

    @Before
    public void init() {
        clusterClient = new JMSMetricsRepositoryClusterClient(jmsClient);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JMSMetricsRepositoryClusterClient.class);
        tester.testAllPublicInstanceMethods(clusterClient);
    }

    @Test
    public void startMetricsListener_starts_jms_listener() throws JMSException {
        clusterClient.startMetricsListener(metricsListener);

        verify(jmsClient).addTopicListener(eq(METRICS_TOPIC), any(MessageListener.class));
        verify(jmsClient).startListening();
        verifyNoMoreInteractions(jmsClient);
        verifyZeroInteractions(metricsListener);
    }

    @Test
    public void jms_exception_is_propagated_when_starting_listener() throws JMSException {
        JMSException expected = new JMSException(new RuntimeException());
        doThrow(expected).when(jmsClient).startListening();

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(expected));
        clusterClient.startMetricsListener(metricsListener);
    }

}