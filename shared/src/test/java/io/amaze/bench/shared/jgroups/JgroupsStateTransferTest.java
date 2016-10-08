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

    private static final int TIMEOUT_MS = 1000;

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
        JChannel jChannel = new JChannel("fast.xml");
        JgroupsClusterMember member = new JgroupsClusterMember(jChannel);
        member.stateMultiplexer().addStateHolder(holder);
        member.join();
        return jChannel;
    }

    private class MemberStateHolder implements JgroupsStateHolder<String> {
        private final JgroupsStateKey key;
        private String state;

        public MemberStateHolder(final JgroupsStateKey key, final String state) {
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
