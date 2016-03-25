package io.amaze.bench.shared.helper;

import org.junit.Test;

import javax.validation.constraints.Null;
import java.lang.reflect.Method;

import static io.amaze.bench.shared.helper.ReflectionHelper.findAtMostOneAnnotatedMethod;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created on 3/1/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class ReflectionHelperTest {

    @Test
    public void find_no_annotated_method_returns_null() {
        Method method = findAtMostOneAnnotatedMethod(NoAnnotatedMethod.class, Null.class);
        assertNull(method);
    }

    @Test
    public void find_single_annotated_method_works() {
        Method method = findAtMostOneAnnotatedMethod(SingleAnnotatedMethod.class, Null.class);
        assertNotNull(method);
        assertThat(method.getName(), is("annotated"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void find_two_annotated_methods_throws() {
        findAtMostOneAnnotatedMethod(TwoAnnotatedMethods.class, Null.class);
    }

    private interface NoAnnotatedMethod {
        void test();
    }

    private interface SingleAnnotatedMethod {
        @Null
        void annotated();

        void test();
    }

    private interface TwoAnnotatedMethods {
        @Null
        void annotated1();

        @Null
        void annotated2();
    }

}