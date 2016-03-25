package io.amaze.bench.shared.jms;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;
import net.timewalker.ffmq3.FFMQCoreSettings;
import net.timewalker.ffmq3.listeners.ClientListener;
import net.timewalker.ffmq3.listeners.tcp.io.TcpListener;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.management.destination.definition.TopicDefinition;
import net.timewalker.ffmq3.utils.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.naming.NameAlreadyBoundException;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.TimeUnit;

/**
 * Created on 3/2/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class FFMQServer implements JMSServer {

    private static final Logger LOG = LoggerFactory.getLogger(FFMQServer.class);

    private static final String ENGINE_NAME = "FFMQ";
    private static final int MAX_NON_PERSISTENT_MESSAGES = 1000;
    private static final String DATA_DIR_PATH = "tmp/";

    private final FFMQEngine engine;
    private final ClientListener tcpListener;
    private final Object queuesLock = new Object();

    public FFMQServer(@NotNull final String host, @NotNull final int port) throws JMSException {
        purgeQueuesAnTopicsProperties();

        Settings settings = createSettings(DATA_DIR_PATH);
        engine = initEngine(settings);
        tcpListener = startListenerSynchronously(host, port, settings);
    }

    private static void purgeQueuesAnTopicsProperties() throws JMSException {
        File tmpDir = new File(DATA_DIR_PATH);
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                throw new JMSException("Could not createForAgent DATA_DIR for FFMQServer " + tmpDir.getAbsolutePath());
            }
        }

        String[] tmpProperties = tmpDir.list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".properties") && (name.startsWith("queue-") || name.startsWith("topic-"));
            }
        });

        for (String fileName : tmpProperties) {
            File tmpFileToDelete = new File(tmpDir.getAbsolutePath() + File.separator + fileName);
            if (!tmpFileToDelete.delete()) {
                throw new JMSException("Could not delete tmp file of FFMQServer " + tmpFileToDelete.getAbsolutePath());
            }
        }
    }

    @Override
    public void createQueue(@NotNull final String name) throws JMSException, NameAlreadyBoundException {
        synchronized (queuesLock) {
            if (engine.getDestinationDefinitionProvider().hasQueueDefinition(name)) {
                throw new NameAlreadyBoundException(String.format("The queue name '%s' is already in use.", name));
            }

            QueueDefinition queueDef = new QueueDefinition();
            queueDef.setName(name);
            queueDef.setUseJournal(false);
            queueDef.setOverflowToPersistent(false);
            queueDef.setMaxNonPersistentMessages(MAX_NON_PERSISTENT_MESSAGES);
            queueDef.check();
            engine.createQueue(queueDef);
        }
    }

    @Override
    public void createTopic(@NotNull final String name) throws JMSException, NameAlreadyBoundException {
        synchronized (queuesLock) {
            if (engine.getDestinationDefinitionProvider().hasTopicDefinition(name)) {
                throw new NameAlreadyBoundException(String.format("The topic name '%s' is already in use.", name));
            }

            TopicDefinition topicDef = new TopicDefinition();
            topicDef.setName(name);
            topicDef.setUseJournal(false);
            topicDef.setOverflowToPersistent(false);
            topicDef.setMaxNonPersistentMessages(MAX_NON_PERSISTENT_MESSAGES);
            topicDef.check();
            engine.createTopic(topicDef);
        }
    }

    @Override
    public boolean deleteQueue(@NotNull final String queue) {
        try {
            engine.deleteQueue(queue);
            return true;
        } catch (JMSException e) {
            LOG.debug("Error while deleting queue " + queue, e);
            return false;
        }
    }

    @Override
    public boolean deleteTopic(@NotNull final String topic) {
        try {
            engine.deleteTopic(topic);
            return true;
        } catch (JMSException e) {
            LOG.warn("Error while deleting topic " + topic, e);
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        synchronized (queuesLock) {
            for (String queue : engine.getDestinationDefinitionProvider().getAllQueueNames()) {
                deleteQueue(queue);
            }

            for (String topic : engine.getDestinationDefinitionProvider().getAllTopicNames()) {
                deleteTopic(topic);
            }

            tcpListener.stop();
            engine.undeploy();
        }
    }

    private ClientListener startListenerSynchronously(@NotNull final String host,
                                                      @NotNull final int port,
                                                      @NotNull final Settings settings) {

        ClientListener tcpListener = new TcpListener(engine, host, port, settings, null);
        ListenerThread listenerThread = new ListenerThread(host, port, tcpListener);
        listenerThread.start();

        int count = 200;
        while (!listenerThread.hasFailed() && !tcpListener.isStarted() && count > 0) {
            Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
            count--;
        }

        if (listenerThread.hasFailed() || !tcpListener.isStarted() || count == 0) {
            throw Throwables.propagate(listenerThread.getException());
        }

        return tcpListener;
    }

    private FFMQEngine initEngine(@NotNull final Settings settings) throws JMSException {
        FFMQEngine engine = new FFMQEngine(ENGINE_NAME, settings);
        engine.deploy();
        return engine;
    }

    private Settings createSettings(@NotNull final String dataDirPath) {
        Settings settings = new Settings();
        settings.setStringProperty(FFMQCoreSettings.DESTINATION_DEFINITIONS_DIR, dataDirPath);
        settings.setStringProperty(FFMQCoreSettings.BRIDGE_DEFINITIONS_DIR, dataDirPath);
        settings.setStringProperty(FFMQCoreSettings.TEMPLATES_DIR, dataDirPath);
        settings.setStringProperty(FFMQCoreSettings.DEFAULT_DATA_DIR, dataDirPath);
        return settings;
    }

    private static class ListenerThread extends Thread {

        private static final String LISTENER_THREAD_NAME = "jms-listener";
        private final ClientListener listener;
        private final String host;
        private final int port;

        private volatile JMSException exception;

        ListenerThread(@NotNull final String host, @NotNull final int port, @NotNull final ClientListener listener) {
            this.host = host;
            this.port = port;
            this.listener = listener;
            setName(LISTENER_THREAD_NAME);
        }

        @Override
        public void run() {
            try {
                listener.start();
            } catch (JMSException e) {
                LOG.error("Error while starting TCP listener on " + host + ":" + port, e);
                exception = e;
            }
        }

        boolean hasFailed() {
            return exception != null;
        }

        public JMSException getException() {
            return exception;
        }
    }

}
