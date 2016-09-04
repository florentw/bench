package io.amaze.bench.api;

import com.google.common.testing.SerializableTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class TerminationExceptionTest {

    private TerminationException exception;

    @org.junit.Before
    public void before() {
        exception = new TerminationException();
    }

    @Test
    public void serialize_deserialize() {
        TerminationException received = SerializableTester.reserialize(exception);

        assertThat(received.getMessage(), is(exception.getMessage()));
    }

}