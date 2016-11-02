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
package io.amaze.bench.cluster.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.cluster.actor.ActorInputMessage;
import io.amaze.bench.cluster.actor.ActorKey;
import io.amaze.bench.cluster.registry.ActorRegistry;
import io.amaze.bench.shared.jgroups.JgroupsEndpoint;
import io.amaze.bench.shared.util.Network;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.Util;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.UnknownHostException;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created on 10/9/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsSenderTest {

    private static final String MESSAGE = "hello";

    @Mock
    private JChannel jChannel;
    @Mock
    private ActorRegistry actorRegistry;

    private JgroupsEndpoint endpoint;
    private JgroupsSender sender;
    private Address address;

    @Before
    public void init() throws UnknownHostException {
        sender = new JgroupsSender(jChannel);
        address = new IpAddress(Network.LOCALHOST, 1337);
        endpoint = new JgroupsEndpoint(address);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ActorKey.class, new ActorKey("dummy"));
        tester.setDefault(ActorInputMessage.class, ActorInputMessage.dumpMetrics());
        tester.setDefault(JgroupsEndpoint.class, endpoint);

        tester.testAllPublicConstructors(JgroupsSender.class);
        tester.testAllPublicInstanceMethods(sender);
    }

    @Test
    public void broadcast_send_message_with_null_recipient() throws Exception {
        sender.broadcast(MESSAGE);

        verify(jChannel).send(eq(null), aryEq(Util.objectToByteBuffer(MESSAGE)));
        verifyNoMoreInteractions(jChannel);
    }

    @Test
    public void send_to_endpoint_sends_on_channel() throws Exception {
        sender.sendToEndpoint(endpoint, MESSAGE);

        verify(jChannel).send(eq(address), aryEq(Util.objectToByteBuffer(MESSAGE)));
        verifyNoMoreInteractions(jChannel);
    }

}