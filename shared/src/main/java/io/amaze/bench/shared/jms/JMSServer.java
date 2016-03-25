package io.amaze.bench.shared.jms;

import javax.jms.JMSException;
import javax.naming.NameAlreadyBoundException;
import javax.validation.constraints.NotNull;

/**
 * Created on 3/4/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface JMSServer extends AutoCloseable {
    void createQueue(@NotNull String name) throws JMSException, NameAlreadyBoundException;

    void createTopic(@NotNull String name) throws JMSException, NameAlreadyBoundException;

    boolean deleteQueue(@NotNull String queue);

    boolean deleteTopic(@NotNull String topic);
}
