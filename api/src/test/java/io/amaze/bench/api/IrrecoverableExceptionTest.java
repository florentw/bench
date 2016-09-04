package io.amaze.bench.api;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 8/15/16.
 */
@RunWith(MockitoJUnitRunner.class)
public final class IrrecoverableExceptionTest {

    private IrrecoverableException exception;

    @org.junit.Before
    public void before() {
        exception = new IrrecoverableException("", new RuntimeException());
    }

    @Test
    public void null_parameters_are_invalid() {
        NullPointerTester tester = new NullPointerTester();

        tester.testAllPublicConstructors(IrrecoverableException.class);
        tester.testAllPublicInstanceMethods(exception);
    }

    @Test
    public void serialize_deserialize() {
        IrrecoverableException received = SerializableTester.reserialize(exception);

        assertThat(received.getMessage(), is(exception.getMessage()));
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

}