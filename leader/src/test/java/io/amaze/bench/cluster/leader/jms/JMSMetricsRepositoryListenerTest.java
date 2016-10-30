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
package io.amaze.bench.cluster.leader.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.api.metric.Metric;
import io.amaze.bench.cluster.leader.registry.MetricsRepositoryListener;
import io.amaze.bench.cluster.metric.MetricValue;
import io.amaze.bench.cluster.metric.MetricValuesMessage;
import io.amaze.bench.shared.jms.JMSException;
import io.amaze.bench.shared.jms.JMSHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.BytesMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.amaze.bench.runtime.actor.TestActor.DUMMY_ACTOR;
import static io.amaze.bench.shared.jms.JMSHelperTest.createTestBytesMessage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created on 10/3/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSMetricsRepositoryListenerTest {

    @Mock
    private MetricsRepositoryListener metricsListener;

    private JMSMetricsRepositoryListener jmsListener;

    @Before
    public void initMetricsRepository() throws JMSException {
        jmsListener = new JMSMetricsRepositoryListener(metricsListener);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JMSMetricsRepositoryListener.class);
        tester.testAllPublicInstanceMethods(jmsListener);
    }

    @Test
    public void no_exception_thrown_on_invalid_jms_message() throws JMSException {
        jmsListener.onMessage(mock(javax.jms.Message.class));
    }

    @Test
    public void onMetricValues_is_called_on_message() throws javax.jms.JMSException, IOException {
        MetricValuesMessage valuesMessage = metricValuesMessage(new ArrayList<>());
        metricValuesMessage(new ArrayList<>());
        BytesMessage jmsMessage = jmsMetricsMessage(valuesMessage);

        jmsListener.onMessage(jmsMessage);

        verify(metricsListener).onMetricValues(valuesMessage);
    }

    private BytesMessage jmsMetricsMessage(final MetricValuesMessage valuesMessage)
            throws IOException, javax.jms.JMSException {
        final byte[] data = JMSHelper.convertToBytes(valuesMessage);
        return createTestBytesMessage(data);
    }

    private MetricValuesMessage metricValuesMessage(final List<MetricValue> values) {
        Map<Metric, List<MetricValue>> metricValues = new HashMap<>();
        metricValues.put(Metric.metric("metric", "sec").build(), values);
        return new MetricValuesMessage(DUMMY_ACTOR, metricValues);
    }

}