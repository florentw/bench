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
package io.amaze.bench.runtime.cluster.jms;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.runtime.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.jms.JMSEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created on 8/16/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JMSClusterClientFactoryTest {

    @Mock
    private ActorRegistry actorRegistry;

    @Test
    public void null_parameters_are_invalid() {
        JMSClusterClientFactory clientFactory = new JMSClusterClientFactory(new JMSEndpoint("", 1), actorRegistry);
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JMSClusterClientFactory.class);
        tester.testAllPublicInstanceMethods(clientFactory);
    }

}