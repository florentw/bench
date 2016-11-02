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

import org.jgroups.JChannel;
import org.junit.Test;

import javax.validation.constraints.NotNull;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

/**
 * Created on 10/5/16.
 */
public final class JgroupsStateTransferTest {

    private static final int TIMEOUT_MS = 5000;

    @Test
    public void joining_member_reads_state_from_existing_one() throws Exception {
        JgroupsStateKey key = new JgroupsStateKey("shared-state-key");
        String initialState = "initial-state-of-1";
        MemberStateHolder holder1 = spy(new MemberStateHolder(key, initialState));
        MemberStateHolder holder2 = spy(new MemberStateHolder(key, null));
        JChannel member1 = createMember(holder1);

        JChannel member2 = createMember(holder2);

        verify(holder1, timeout(TIMEOUT_MS)).getState();
        verify(holder2, timeout(TIMEOUT_MS)).setState(initialState);
        assertThat(holder1.getState(), is(initialState));
        assertThat(holder2.getState(), is(initialState));

        member1.close();
        member2.close();
    }

    private JChannel createMember(final MemberStateHolder holder) throws Exception {
        JChannel jChannel = new JChannel(JgroupsClusterConfigs.JGROUPS_XML_PROTOCOLS);
        JgroupsClusterMember member = new JgroupsClusterMember(jChannel);
        member.stateMultiplexer().addStateHolder(holder);
        member.join();
        return jChannel;
    }

    private class MemberStateHolder implements JgroupsStateHolder<String> {
        private final JgroupsStateKey key;
        private String state;

        MemberStateHolder(final JgroupsStateKey key, final String state) {
            this.key = key;
            this.state = state;
        }

        @Override
        public JgroupsStateKey getKey() {
            return key;
        }

        @Override
        public String getState() {
            return state;
        }

        @Override
        public void setState(@NotNull final String newState) {
            state = newState;
        }
    }
}
