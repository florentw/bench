/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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
import net.timewalker.ffmq3.FFMQCoreSettings;
import net.timewalker.ffmq3.listeners.ClientListener;
import net.timewalker.ffmq3.listeners.tcp.io.TcpListener;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.management.TemplateMapping;
import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.management.destination.definition.TopicDefinition;
import net.timewalker.ffmq3.management.destination.template.QueueTemplate;
import net.timewalker.ffmq3.management.destination.template.TopicTemplate;
import net.timewalker.ffmq3.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.JMSException;
import javax.naming.NameAlreadyBoundException;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.lang.String.format;

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

    private static final Logger log = LogManager.getLogger();
    private static final String ENGINE_NAME = "FFMQ";
    private static final int MAX_NON_PERSISTENT_MESSAGES = 1000;
    private static final String QUEUE_TEMPLATE = "QueueTemplate";
    private static final String TOPIC_TEMPLATE = "TopicTemplate";

    private final FFMQEngine engine;
    private final ClientListener tcpListener;
    private final Object queuesLock = new Object();

    public FFMQServer(@NotNull JMSEndpoint endpoint) throws io.amaze.bench.shared.jms.JMSException {
        checkNotNull(endpoint);

        try {
            purgeQueuesAnTopicsProperties();

            Settings settings = createSettings(DATA_DIR_PATH);
            engine = initEngine(settings);

            createQueueTemplate();
            createTopicTemplate();

            tcpListener = startListenerSynchronously(endpoint, settings);
        } catch (JMSException | IOException e) {
            throw new io.amaze.bench.shared.jms.JMSException(e);
        }
    }

    @Override
    public void createQueue(@NotNull final String name) throws io.amaze.bench.shared.jms.JMSException {
        checkNotNull(name);

        try {
            internalCreateQueue(name);
        } catch (NameAlreadyBoundException | JMSException e) {
            throw new io.amaze.bench.shared.jms.JMSException(e);
        }
    }

    @Override
    public void createTopic(@NotNull final String name) throws io.amaze.bench.shared.jms.JMSException {
        checkNotNull(name);

        try {
            internalCreateTopic(name);
        } catch (NameAlreadyBoundException | JMSException e) {
            throw new io.amaze.bench.shared.jms.JMSException(e);
        }
    }

    @Override
    public boolean deleteQueue(@NotNull final String queue) {
        checkNotNull(queue);

        try {
            engine.deleteQueue(queue);
            return true;
        } catch (JMSException e) {
            log.debug("Error while deleting queue {}", queue, e);
            return false;
        }
    }

    @Override
    public boolean deleteTopic(@NotNull final String topic) {
        checkNotNull(topic);

        try {
            engine.deleteTopic(topic);
            return true;
        } catch (JMSException e) {
            log.warn("Error while deleting topic {}", topic, e);
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

    private void createQueueTemplate() throws JMSException {
        Settings settings = new Settings();
        settings.setStringProperty("name", QUEUE_TEMPLATE);
        settings.setBooleanProperty("persistentStore.useJournal", false);
        settings.setBooleanProperty("memoryStore.overflowToPersistent", false);
        settings.setIntProperty("memoryStore.maxMessages", MAX_NON_PERSISTENT_MESSAGES);
        QueueTemplate queueTemplate = new QueueTemplate(settings);
        engine.getDestinationTemplateProvider().addQueueTemplate(queueTemplate);
        engine.getTemplateMappingProvider().addQueueTemplateMapping(new TemplateMapping("*", QUEUE_TEMPLATE));
    }

    private void createTopicTemplate() throws JMSException {
        Settings settings = new Settings();
        settings.setStringProperty("name", TOPIC_TEMPLATE);
        settings.setBooleanProperty("persistentStore.useJournal", false);
        settings.setBooleanProperty("memoryStore.overflowToPersistent", false);
        settings.setIntProperty("memoryStore.maxMessages", MAX_NON_PERSISTENT_MESSAGES);
        TopicTemplate topicTemplate = new TopicTemplate(settings);
        engine.getDestinationTemplateProvider().addTopicTemplate(topicTemplate);
        engine.getTemplateMappingProvider().addTopicTemplateMapping(new TemplateMapping("*", TOPIC_TEMPLATE));
    }

    private void purgeQueuesAnTopicsProperties() throws IOException {
        File tmpDir = new File(DATA_DIR_PATH);
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IOException("Could not create DATA_DIR for FFMQServer " + tmpDir.getAbsolutePath());
        }

        String[] tmpProperties = tmpDir.list((dir, name) -> name.endsWith(PROP_SUFFIX) && //
                (name.startsWith(QUEUE_PREFIX) || name.startsWith(TOPIC_PREFIX)));

        for (String fileName : checkNotNull(tmpProperties)) {
            File tmpFileToDelete = new File(tmpDir.getAbsolutePath() + File.separator + fileName);
            if (!tmpFileToDelete.delete()) {
                throw new IOException("Could not delete tmp file of FFMQServer " + tmpFileToDelete.getAbsolutePath());
            }
        }
    }

    private void internalCreateQueue(@NotNull final String name) throws JMSException, NameAlreadyBoundException {
        synchronized (queuesLock) {
            if (engine.getDestinationDefinitionProvider().hasQueueDefinition(name)) {
                throw new NameAlreadyBoundException(format("The queue name '%s' is already in use.", name));
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
                throw new NameAlreadyBoundException(format("The topic name '%s' is already in use.", name));
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

    private ClientListener startListenerSynchronously(final JMSEndpoint endpoint, final Settings settings) {

        ClientListener localListener = new TcpListener(engine, endpoint.getHost(), endpoint.getPort(), settings, null);
        ListenerThread listenerThread = new ListenerThread(endpoint, localListener);
        listenerThread.start();

        int count = 200;
        while (!listenerThread.hasFailed() && !localListener.isStarted() && count > 0) {
            sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
            count--;
        }

        if (listenerThread.hasFailed() || !localListener.isStarted() || count == 0) {
            throw propagate(listenerThread.getException());
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
        settings.setBooleanProperty(FFMQCoreSettings.AUTO_CREATE_QUEUES, true);
        settings.setBooleanProperty(FFMQCoreSettings.AUTO_CREATE_TOPICS, true);
        settings.setBooleanProperty(FFMQCoreSettings.DELIVERY_LOG_LISTENERS_FAILURES, true);
        return settings;
    }

    /**
     * Starts the TCP server socket on a dedicated thread.
     */
    private static final class ListenerThread extends Thread {

        private static final String LISTENER_THREAD_NAME = "jms-listener-";
        private final ClientListener listener;
        private final JMSEndpoint endpoint;

        private volatile JMSException exception;

        ListenerThread(@NotNull final JMSEndpoint endpoint, @NotNull final ClientListener listener) {
            this.endpoint = checkNotNull(endpoint);
            this.listener = checkNotNull(listener);

            setName(LISTENER_THREAD_NAME + this.endpoint.getHost() + ":" + this.endpoint.getPort());
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                listener.start();
            } catch (JMSException e) {
                log.error("Error while starting TCP listener on {}", endpoint, e);
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
