package io.amaze.bench.shared.jms;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.timewalker.ffmq3.FFMQConstants;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.Hashtable;

/**
 * Created on 3/2/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class FFMQClient implements JMSClient {

    private static final int MAX_CACHE_SIZE = 100;

    private static final String JNDI_QUEUE_PREFIX = "queue/";
    private static final String JNDI_TOPIC_PREFIX = "topic/";

    private final Cache<String, MessageProducer> queueProducers = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE).build();
    private final Cache<String, MessageProducer> topicProducers = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE).build();

    private final InitialContext context;
    private final Session session;
    private final Connection conn;

    public FFMQClient(@NotNull final String host, @NotNull final int port) throws NamingException, JMSException {

        context = initContext(host, port);
        conn = initConnection();
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @Override
    public void startListening() throws JMSException {
        conn.start();
    }

    @Override
    public void addQueueListener(@NotNull final String listenerQueueName,
                                 @NotNull final MessageListener listener) throws NamingException, JMSException {
        Queue listenerQueue = lookupQueue(listenerQueueName);
        MessageConsumer messageConsumer = session.createConsumer(listenerQueue);
        messageConsumer.setMessageListener(listener);
    }

    @Override
    public void addTopicListener(@NotNull final String listenerTopicName,
                                 @NotNull final MessageListener listener) throws NamingException, JMSException {
        Topic listenerTopic = lookupTopic(listenerTopicName);
        MessageConsumer messageConsumer = session.createConsumer(listenerTopic);
        messageConsumer.setMessageListener(listener);
    }

    @Override
    public void sendToQueue(@NotNull final String queueName,
                            @NotNull final Serializable msg) throws NamingException, JMSException, InterruptedException, IOException, ClassNotFoundException {
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

    @Override
    public void sendToTopic(@NotNull final String topicName,
                            @NotNull final Serializable msg) throws NamingException, JMSException, InterruptedException, IOException, ClassNotFoundException {
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

    @Override
    public void close() throws Exception {
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
            } catch (JMSException ignore) {
            }
        }
    }

    private BytesMessage objectToMessage(final Serializable payload) throws JMSException, IOException, ClassNotFoundException {
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

    private Connection initConnection() throws NamingException, JMSException {
        // Lookup a connection factory in the context
        ConnectionFactory connFactory = (ConnectionFactory) context.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);

        // Obtain a JMS connection from the factory
        return connFactory.createConnection();
    }

    private InitialContext initContext(@NotNull final String host, @NotNull final int port) throws NamingException {
        InitialContext context;
        Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, String.format("tcp://%s:%d", host, port));
        context = new InitialContext(env);
        return context;
    }
}
