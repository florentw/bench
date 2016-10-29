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
package io.amaze.bench.shared.jgroups;

import com.google.common.testing.NullPointerTester;
import io.amaze.bench.shared.util.Network;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.ViewId;
import org.jgroups.stack.IpAddress;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

/**
 * Created on 10/9/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class JgroupsViewMultiplexerTest {

    @Mock
    private JChannel jChannel;
    @Mock
    private JgroupsViewListener listener;

    private View initialView;
    private JgroupsViewMultiplexer viewMultiplexer;
    private IpAddress initialAddress;

    @Before
    public void init() throws Exception {
        initialAddress = new IpAddress(Network.LOCALHOST, 1337);
        viewMultiplexer = viewMultiplexer();
        initialView = new View(new ViewId(), new Address[]{initialAddress});
        when(jChannel.view()).thenReturn(initialView);
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(JgroupsViewMultiplexer.class);
        tester.testAllPublicInstanceMethods(viewMultiplexer);
    }

    @Test
    public void adding_listener_registers_it_and_provides_initial_view() {

        viewMultiplexer.addListener(listener);

        assertThat(viewMultiplexer.getViewListeners().size(), is(1));
        assertThat(viewMultiplexer.getViewListeners().get(listener), is(initialView));
        verify(jChannel).view();
        verify(listener).initialView(initialView.getMembers());
        verifyNoMoreInteractions(jChannel);
        verifyNoMoreInteractions(listener);
    }

    @Test(expected = IllegalStateException.class)
    public void adding_same_listener_twice_throws_IllegalStateException() {
        viewMultiplexer.addListener(listener);

        viewMultiplexer.addListener(listener);
    }

    @Test
    public void removing_listener_unregisters_it() {
        viewMultiplexer.addListener(listener);
        viewMultiplexer.removeListener(listener);

        assertThat(viewMultiplexer.getViewListeners().size(), is(0));
        verify(listener).initialView(initialView.getMembers());
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void removing_unknown_listener_does_not_throw() {
        viewMultiplexer.removeListener(listener);

        viewMultiplexer.removeListener(listener);

        assertThat(viewMultiplexer.getViewListeners().size(), is(0));
    }

    @Test
    public void view_update_is_propagated_to_registered_listeners() throws Exception {
        viewMultiplexer.addListener(listener);
        Address joinedMember = new IpAddress(Network.LOCALHOST, 1338);
        View newView = new View(new ViewId(), new Address[]{joinedMember});

        viewMultiplexer.viewUpdate(newView);

        verify(listener).initialView(initialView.getMembers());
        verify(listener).memberJoined(joinedMember);
        verify(listener).memberLeft(initialAddress);
        verifyNoMoreInteractions(listener);
    }

    private JgroupsViewMultiplexer viewMultiplexer() {
        return new JgroupsViewMultiplexer(jChannel);
    }
}