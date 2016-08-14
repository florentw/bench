package io.amaze.bench.client.runtime.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Closeable;
import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;

/**
 * Perform a waitFor on the given process to know when the process has terminated and avoid zombies.
 * <p>
 * Created on 3/27/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class ProcessWatchDogThread extends Thread implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessWatchDogThread.class);

    private final String name;
    private final Process process;
    private final CountDownLatch watchdogStartedLatch;

    private volatile boolean doWork = true;
    private volatile boolean exited = false;

    ProcessWatchDogThread(@NotNull final String name, @NotNull final Process process) {
        this.name = checkNotNull(name);
        this.process = checkNotNull(process);

        watchdogStartedLatch = new CountDownLatch(1);

        setName("WatchDog-" + name);
    }

    @Override
    public void close() {
        doWork = false;
    }

    @Override
    public void run() {
        LOG.info(this + " Watching process " + process + "...");

        while (doWork && !exited) {
            watchdogStartedLatch.countDown();

            try {
                int exitCode = process.waitFor();
                exited = true;
                LOG.info(this + " Exited with code " + exitCode + ".");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public String toString() {
        return "WatchDogThread{" + '\'' + name + '\'' + '}';
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
