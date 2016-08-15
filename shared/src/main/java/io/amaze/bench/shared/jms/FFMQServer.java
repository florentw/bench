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
package io.amaze.bench.shared.jms;

import com.google.common.annotations.VisibleForTesting;
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
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 3/2/16.
 */
public final class FFMQServer implements JMSServer {

    @VisibleForTesting
    static final String DATA_DIR_PATH = "tmp";
    @VisibleForTesting
    static final String PROP_SUFFIX = ".properties";
    @VisibleForTesting
    static final String QUEUE_PREFIX = "queue-";
    @VisibleForTesting
    static final String TOPIC_PREFIX = "topic-";

    private static final Logger LOG = LoggerFactory.getLogger(FFMQServer.class);
    private static final String ENGINE_NAME = "FFMQ";
    private static final int MAX_NON_PERSISTENT_MESSAGES = 1000;

    private final FFMQEngine engine;
    private final ClientListener tcpListener;
    private final Object queuesLock = new Object();

    public FFMQServer(@NotNull final String host, final int port) throws io.amaze.bench.shared.jms.JMSException {
        checkNotNull(host);

        try {
            purgeQueuesAnTopicsProperties();

            Settings settings = createSettings(DATA_DIR_PATH);
            engine = initEngine(settings);
            tcpListener = startListenerSynchronously(host, port, settings);
        } catch (Exception e) {
            throw new io.amaze.bench.shared.jms.JMSException(e);
        }
    }

    @Override
    public void createQueue(@NotNull final String name) throws io.amaze.bench.shared.jms.JMSException {
        checkNotNull(name);

        try {
            internalCreateQueue(name);
        } catch (Exception e) {
            throw new io.amaze.bench.shared.jms.JMSException(e);
        }
    }

    @Override
    public void createTopic(@NotNull final String name) throws io.amaze.bench.shared.jms.JMSException {
        checkNotNull(name);

        try {
            internalCreateTopic(name);
        } catch (Exception e) {
            throw new io.amaze.bench.shared.jms.JMSException(e);
        }
    }

    @Override
    public boolean deleteQueue(@NotNull final String queue) {
        checkNotNull(queue);

        try {
            engine.deleteQueue(queue);
            return true;
        } catch (Exception e) {
            LOG.debug("Error while deleting queue " + queue, e);
            return false;
        }
    }

    @Override
    public boolean deleteTopic(@NotNull final String topic) {
        checkNotNull(topic);

        try {
            engine.deleteTopic(topic);
            return true;
        } catch (Exception e) {
            LOG.warn("Error while deleting topic " + topic, e);
            return false;
        }
    }

    @Override
    public void close() {
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

    private void purgeQueuesAnTopicsProperties() throws IOException {
        File tmpDir = new File(DATA_DIR_PATH);
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IOException("Could not create DATA_DIR for FFMQServer " + tmpDir.getAbsolutePath());
        }

        String[] tmpProperties = tmpDir.list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return (name.startsWith(QUEUE_PREFIX) || name.startsWith(TOPIC_PREFIX)) && //
                        name.endsWith(PROP_SUFFIX);
            }
        });

        for (String fileName : tmpProperties) {
            File tmpFileToDelete = new File(tmpDir.getAbsolutePath() + File.separator + fileName);
            if (!tmpFileToDelete.delete()) {
                throw new IOException("Could not delete tmp file of FFMQServer " + tmpFileToDelete.getAbsolutePath());
            }
        }
    }

    private void internalCreateQueue(@NotNull final String name) throws JMSException, NameAlreadyBoundException {
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

    private void internalCreateTopic(@NotNull final String name) throws JMSException, NameAlreadyBoundException {
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

    private ClientListener startListenerSynchronously(@NotNull final String host,
                                                      @NotNull final int port,
                                                      @NotNull final Settings settings) {

        ClientListener localListener = new TcpListener(engine, host, port, settings, null);
        ListenerThread listenerThread = new ListenerThread(host, port, localListener);
        listenerThread.start();

        int count = 200;
        while (!listenerThread.hasFailed() && !localListener.isStarted() && count > 0) {
            Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
            count--;
        }

        if (listenerThread.hasFailed() || !localListener.isStarted() || count == 0) {
            throw Throwables.propagate(listenerThread.getException());
        }

        return localListener;
    }

    private FFMQEngine initEngine(@NotNull final Settings settings) throws JMSException {
        FFMQEngine localEngine = new FFMQEngine(ENGINE_NAME, settings);
        localEngine.deploy();
        return localEngine;
    }

    private Settings createSettings(@NotNull final String dataDirPath) {
        Settings settings = new Settings();
        settings.setStringProperty(FFMQCoreSettings.DESTINATION_DEFINITIONS_DIR, dataDirPath);
        settings.setStringProperty(FFMQCoreSettings.BRIDGE_DEFINITIONS_DIR, dataDirPath);
        settings.setStringProperty(FFMQCoreSettings.TEMPLATES_DIR, dataDirPath);
        settings.setStringProperty(FFMQCoreSettings.DEFAULT_DATA_DIR, dataDirPath);
        return settings;
    }

    private static final class ListenerThread extends Thread {

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

        public JMSException getException() {
            return exception;
        }

        boolean hasFailed() {
            return exception != null;
        }
    }

}
