package io.amaze.bench.shared.jgroups;

import io.amaze.bench.shared.test.IntegrationTest;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.verification.VerificationWithTimeout;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * In some cases the cluster does not form, check the firewall or for networking issues in that case.
 */
@Category(IntegrationTest.class)
public final class JgroupsMessagingTest {

    private static final VerificationWithTimeout TIMEOUT = timeout(1000);

    private static final String BROADCAST = "broadcast";
    private static final String MSG_FROM_1_TO_2 = "msg from 1 to 2";
    private static final String MSG_FROM_2_TO_1 = "msg from 2 to 1";
    private Member member1;
    private Member member2;

    @Before
    public void init() throws Exception {
        member1 = createMember();
        member2 = createMember();
    }

    @After
    public void close() {
        member1.jChannel.close();
        member2.jChannel.close();
    }

    @Test
    public void init_cluster() throws Exception {
        assertThat(member1.jChannel.view().getMembers().size(), is(2));
        assertThat(member2.jChannel.view().getMembers().size(), is(2));
    }

    @Test
    public void members_send_messages_to_each_other_and_receive() throws Exception {
        JgroupsListener<String> jgroupsListener1 = addListener(member1);
        JgroupsListener<String> jgroupsListener2 = addListener(member2);

        member1.jChannel.send(new Message(member2.jChannel.address(), MSG_FROM_1_TO_2));
        member2.jChannel.send(new Message(member1.jChannel.address(), MSG_FROM_2_TO_1));

        verify(jgroupsListener1, TIMEOUT).onMessage(any(Message.class), eq(MSG_FROM_2_TO_1));
        verify(jgroupsListener2, TIMEOUT).onMessage(any(Message.class), eq(MSG_FROM_1_TO_2));
        verifyNoMoreInteractions(jgroupsListener1);
        verifyNoMoreInteractions(jgroupsListener2);
    }

    @Test
    public void member_broadcasts_a_message_and_everyone_receives() throws Exception {
        JgroupsListener<String> jgroupsListener1 = addListener(member1);
        JgroupsListener<String> jgroupsListener2 = addListener(member2);

        member1.jChannel.send(new Message(null, BROADCAST));

        verify(jgroupsListener1, TIMEOUT).onMessage(any(Message.class), eq(BROADCAST));
        verify(jgroupsListener2, TIMEOUT).onMessage(any(Message.class), eq(BROADCAST));
        verifyNoMoreInteractions(jgroupsListener1);
        verifyNoMoreInteractions(jgroupsListener2);
    }

    private JgroupsListener<String> addListener(final Member member1) {
        JgroupsListener<String> jgroupsListener = mock(JgroupsListener.class);
        member1.clusterClient.listenerMultiplexer().addListener(String.class, jgroupsListener);
        return jgroupsListener;
    }

    private Member createMember() throws Exception {
        JChannel jChannel = new JChannel("fast.xml");
        JgroupsClusterMember clusterClient = createMember(jChannel);
        return new Member(jChannel, clusterClient);
    }

    private JgroupsClusterMember createMember(JChannel jChannel) throws Exception {
        JgroupsClusterMember jgroupsClusterMember = new JgroupsClusterMember(jChannel);
        jgroupsClusterMember.join();
        return jgroupsClusterMember;
    }

    private static class Member {
        private final JChannel jChannel;
        private final JgroupsClusterMember clusterClient;

        private Member(final JChannel jChannel, final JgroupsClusterMember clusterClient) {
            this.jChannel = jChannel;
            this.clusterClient = clusterClient;
        }
    }

}
