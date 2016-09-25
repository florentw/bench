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
package io.amaze.bench.cluster;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.client.runtime.actor.metric.MetricValue;
import io.amaze.bench.client.runtime.actor.metric.MetricValuesMessage;
import io.amaze.bench.client.runtime.message.Message;
import io.amaze.bench.shared.jms.JMSClient;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSHelper;
import io.amaze.bench.shared.jms.JMSServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.BytesMessage;
import javax.jms.MessageListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static io.amaze.bench.client.runtime.agent.Constants.METRICS_TOPIC;
import static io.amaze.bench.shared.jms.JMSHelperTest.createTestBytesMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 9/18/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class MetricsRepositoryTest {

    private static final String ACTOR_NAME = "actor";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private JMSServer server;
    @Mock
    private JMSClient jmsClient;
    private MetricsRepository metricsRepository;

    private MessageListener jmsListener;

    @Before
    public void initMetricsRepository() throws JMSException {
        doAnswer(invocation -> jmsListener = (MessageListener) invocation.getArguments()[1]) //
                .when(jmsClient).addTopicListener(eq(METRICS_TOPIC), any(MessageListener.class));

        metricsRepository = new MetricsRepository(server, jmsClient);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(MetricsRepository.class);
        tester.testAllPublicInstanceMethods(metricsRepository);
    }

    @Test
    public void creating_MetricsRepository_starts_jms_listener() throws JMSException {

        verify(server).createTopic(METRICS_TOPIC);
        verify(jmsClient).addTopicListener(eq(METRICS_TOPIC), any(MessageListener.class));
        verify(jmsClient).startListening();
        verifyNoMoreInteractions(server);
        verifyNoMoreInteractions(jmsClient);
    }

    @Test
    public void jms_exception_is_propagated_when_starting_listener() throws JMSException {
        JMSException expected = new JMSException(new RuntimeException());
        doThrow(expected).when(jmsClient).startListening();

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(expected));
        new MetricsRepository(server, jmsClient);
    }

    @Test
    public void no_exception_thrown_on_invalid_jms_message() throws JMSException {
        jmsListener.onMessage(mock(javax.jms.Message.class));
    }

    @Test
    public void metrics_are_added_on_message() throws javax.jms.JMSException, IOException {
        BytesMessage jmsMessage = jmsMetricsMessage(new ArrayList<>());

        jmsListener.onMessage(jmsMessage);

        MetricValuesMessage metricValuesMessage = metricsRepository.valuesFor(ACTOR_NAME);
        assertThat(metricValuesMessage.metrics().size(), is(1));
    }

    @Test
    public void all_metrics_return_copy() throws javax.jms.JMSException, IOException {
        BytesMessage jmsMessage = jmsMetricsMessage(new ArrayList<>());

        jmsListener.onMessage(jmsMessage);

        Map<String, MetricValuesMessage> allMetrics = metricsRepository.allValues();
        MetricValuesMessage metricValues = allMetrics.get(ACTOR_NAME);
        assertNotNull(metricValues);
        assertThat(metricValues.metrics().size(), is(1));
    }

    @Test
    public void metrics_are_merged_on_second_message() throws javax.jms.JMSException, IOException {
        BytesMessage jmsMessage = jmsMetricsMessage(new ArrayList<>());
        ArrayList<MetricValue> values = new ArrayList<>();
        values.add(new MetricValue(1));
        BytesMessage secondJmsMessage = jmsMetricsMessage(values);

        jmsListener.onMessage(jmsMessage);
        jmsListener.onMessage(secondJmsMessage);

        MetricValuesMessage metricValuesMessage = metricsRepository.valuesFor(ACTOR_NAME);
        assertThat(metricValuesMessage.metrics().size(), is(1));
        assertThat(metricValuesMessage.metrics().values().iterator().next().size(), is(1));
    }

    @Test
    public void expected_metrics_is_set_if_it_already_exists()
            throws IOException, javax.jms.JMSException, ExecutionException {
        BytesMessage jmsMessage = jmsMetricsMessage(new ArrayList<>());
        jmsListener.onMessage(jmsMessage);

        Future<MetricValuesMessage> future = metricsRepository.expectValuesFor(ACTOR_NAME);

        assertThat(getUninterruptibly(future).metrics().size(), is(1));
    }

    @Test
    public void expected_metrics_futures_are_set_when_metrics_are_received()
            throws IOException, javax.jms.JMSException, ExecutionException {
        Future<MetricValuesMessage> firstFuture = metricsRepository.expectValuesFor(ACTOR_NAME);
        Future<MetricValuesMessage> secondFuture = metricsRepository.expectValuesFor(ACTOR_NAME);
        BytesMessage jmsMessage = jmsMetricsMessage(new ArrayList<>());

        jmsListener.onMessage(jmsMessage);

        assertThat(getUninterruptibly(firstFuture).metrics().size(), is(1));
        assertThat(getUninterruptibly(secondFuture).metrics().size(), is(1));
    }

    private BytesMessage jmsMetricsMessage(final List<MetricValue> values) throws IOException, javax.jms.JMSException {
        Map<Metric, List<MetricValue>> metricValues = new HashMap<>();
        metricValues.put(Metric.metric("metric", "sec").build(), values);
        MetricValuesMessage valuesMessage = new MetricValuesMessage(metricValues);
        Message message = new Message<>(ACTOR_NAME, valuesMessage);
        final byte[] data = JMSHelper.convertToBytes(message);
        return createTestBytesMessage(data);
    }

}