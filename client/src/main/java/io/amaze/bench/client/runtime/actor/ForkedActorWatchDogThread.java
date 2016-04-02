package io.amaze.bench.client.runtime.actor;

import com.google.common.util.concurrent.Uninterruptibles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Perform a waitFor on the given process to know when the process has terminated and avoid zombies.
 * <p>
 * Created on 3/27/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
final class ForkedActorWatchDogThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(ForkedActorWatchDogThread.class);

    private final String name;
    private final Process process;
    private final CountDownLatch watchdogStartedLatch;

    private volatile boolean doWork = true;
    private volatile boolean exited = false;

    ForkedActorWatchDogThread(final String name, final Process process) {
        watchdogStartedLatch = new CountDownLatch(1);
        this.name = name;
        this.process = process;
        setName("WatchDog-" + name);
    }

    Process getProcess() {
        return process;
    }

    public void close() {
        doWork = false;
    }

    @Override
    public void run() {
        LOG.info(this + " Watching process " + process + "...");

        while (doWork && !exited) {
            if (watchdogStartedLatch.getCount() > 0) {
                watchdogStartedLatch.countDown();
            }

            try {
                int exitCode = process.waitFor();
                exited = true;
                LOG.info(this + " Exited with code " + exitCode + ".");
            } catch (InterruptedException e) {
                LOG.warn("Interruption received!", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Allows to await that the watchdog thread is actually started
     */
    void awaitUntilStarted() {
        Uninterruptibles.awaitUninterruptibly(watchdogStartedLatch);
    }

    boolean hasProcessExited() {
        return exited;
    }

    @Override
    public String toString() {
        return "WatchDogThread{" +
                '\'' + name + '\'' +
                '}';
    }

}
