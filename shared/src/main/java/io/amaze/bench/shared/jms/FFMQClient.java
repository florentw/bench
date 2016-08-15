package io.amaze.bench.shared.jms;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.timewalker.ffmq3.FFMQConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.Hashtable;

/**
 * Created on 3/2/16.
 */
public final class FFMQClient implements JMSClient {

    private static final Logger LOG = LoggerFactory.getLogger(FFMQClient.class);

    private static final int MAX_CACHE_SIZE = 100;

    private static final String JNDI_QUEUE_PREFIX = "queue/";
    private static final String JNDI_TOPIC_PREFIX = "topic/";

    private final Cache<String, MessageProducer> queueProducers = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE).build();
    private final Cache<String, MessageProducer> topicProducers = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE).build();

    private final InitialContext context;
    private final Session session;
    private final Connection conn;

    public FFMQClient(@NotNull final String host, @NotNull final int port) throws JMSException {
        try {
            context = initContext(host, port);
            conn = initConnection();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (Exception e) {
            throw new JMSException(e);
        }
    }

    @Override
    public void startListening() throws JMSException {
        try {
            conn.start();
        } catch (javax.jms.JMSException e) {
            throw new JMSException(e);
        }
    }

    @Override
    public void addQueueListener(@NotNull final String listenerQueueName, @NotNull final MessageListener listener)
            throws JMSException {
        try {
            Queue listenerQueue = lookupQueue(listenerQueueName);
            MessageConsumer messageConsumer = session.createConsumer(listenerQueue);
            messageConsumer.setMessageListener(listener);
        } catch (NamingException | javax.jms.JMSException e) {
            throw new JMSException(e);
        }
    }

    @Override
    public void addTopicListener(@NotNull final String listenerTopicName, @NotNull final MessageListener listener)
            throws JMSException {
        try {
            Topic listenerTopic = lookupTopic(listenerTopicName);
            MessageConsumer messageConsumer = session.createConsumer(listenerTopic);
            messageConsumer.setMessageListener(listener);
        } catch (NamingException | javax.jms.JMSException e) {
            throw new JMSException(e);
        }
    }

    @Override
    public void sendToQueue(@NotNull final String queueName, @NotNull final Serializable msg) throws JMSException {
        try {
            internalSendToQueue(queueName, msg);
        } catch (Exception e) {
            throw new JMSException(e);
        }
    }

    @Override
    public void sendToTopic(@NotNull final String topicName, @NotNull final Serializable msg) throws JMSException {
        try {
            internalSendToTopic(topicName, msg);
        } catch (Exception e) {
            throw new JMSException(e);
        }
    }

    @Override
    public void close() {
        try {
            internalClose();
        } catch (javax.jms.JMSException e) {
            throw Throwables.propagate(e);
        }
    }

    private void internalClose() throws javax.jms.JMSException {
        synchronized (queueProducers) {
            queueProducers.cleanUp();
        }

        synchronized (topicProducers) {
            topicProducers.cleanUp();
        }

        try {
            session.close();
        } finally {
            try {
                conn.close();
            } catch (javax.jms.JMSException e) {
                LOG.debug("Error while closing JMS connection.", e);
            }
        }
    }

    private void internalSendToTopic(@NotNull final String topicName, @NotNull final Serializable msg)
            throws NamingException, javax.jms.JMSException, IOException, ClassNotFoundException {
        MessageProducer producer;
        synchronized (topicProducers) {
            producer = topicProducers.getIfPresent(topicName);
            if (producer == null) {
                Topic topic = lookupTopic(topicName);
                producer = session.createProducer(topic);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                topicProducers.put(topicName, producer);
            }
        }

        producer.send(objectToMessage(msg));
    }

    private void internalSendToQueue(@NotNull final String queueName, @NotNull final Serializable msg)
            throws NamingException, javax.jms.JMSException, IOException, ClassNotFoundException {
        MessageProducer producer;
        synchronized (queueProducers) {
            producer = queueProducers.getIfPresent(queueName);
            if (producer == null) {
                Queue queue = lookupQueue(queueName);
                producer = session.createProducer(queue);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                queueProducers.put(queueName, producer);
            }
        }

        producer.send(objectToMessage(msg));
    }

    private BytesMessage objectToMessage(final Serializable payload)
            throws IOException, ClassNotFoundException, javax.jms.JMSException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {

            out.writeObject(payload);
            byte[] yourBytes = bos.toByteArray();
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(yourBytes);
            return bytesMessage;
        }
    }

    private Queue lookupQueue(final String queue) throws NamingException {
        return (Queue) context.lookup(JNDI_QUEUE_PREFIX + queue);
    }

    private Topic lookupTopic(final String topic) throws NamingException {
        return (Topic) context.lookup(JNDI_TOPIC_PREFIX + topic);
    }

    private Connection initConnection() throws NamingException, javax.jms.JMSException {
        // Lookup a connection factory in the context
        ConnectionFactory connFactory = (ConnectionFactory) context.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);

        // Obtain a JMS connection from the factory
        return connFactory.createConnection();
    }

    private InitialContext initContext(@NotNull final String host, @NotNull final int port) throws NamingException {
        InitialContext localContext;
        Hashtable<String, Object> env = new Hashtable<>(); // NOSONAR - No choice here...
        env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, String.format("tcp://%s:%d", host, port));
        localContext = new InitialContext(env);
        return localContext;
    }
}
