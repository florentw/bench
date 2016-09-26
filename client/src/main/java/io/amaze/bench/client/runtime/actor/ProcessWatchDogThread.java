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
package io.amaze.bench.client.runtime.actor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.io.Closeable;
import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;

/**
 * Perform a waitFor on the given process to know when the process has terminated and avoid zombies.
 */
final class ProcessWatchDogThread extends Thread implements Closeable {

    private static final Logger LOG = LogManager.getLogger(ProcessWatchDogThread.class);

    private final String name;
    private final Process process;
    private final ProcessTerminationListener terminationListener;
    private final CountDownLatch watchdogStartedLatch;

    private volatile boolean doWork = true;
    private volatile boolean exited = false;

    ProcessWatchDogThread(@NotNull final String name,
                          @NotNull final Process process,
                          @NotNull final ProcessTerminationListener terminationListener) {
        this.name = checkNotNull(name);
        this.process = checkNotNull(process);
        this.terminationListener = checkNotNull(terminationListener);

        watchdogStartedLatch = new CountDownLatch(1);

        setName("WatchDog-" + name);
        setDaemon(true);
    }

    @Override
    public void close() {
        doWork = false;
    }

    @Override
    public void run() {
        LOG.info("{} Watching process {}...", this, process);

        while (doWork && !exited) {
            watchdogStartedLatch.countDown();

            try {
                int exitCode = process.waitFor();
                exited = true;
                terminationListener.onProcessExited(name, exitCode);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public String toString() {
        return "{\"ProcessWatchDogThread\":{" + //
                "\"name\":\"" + name + "\"" + "}}";
    }

    Process getProcess() {
        return process;
    }

    /**
     * Allows to await that the watchdog thread is actually started
     */
    void awaitUntilStarted() {
        awaitUninterruptibly(watchdogStartedLatch);
    }

    boolean hasProcessExited() {
        return exited;
    }

}
