package io.amaze.bench.client.runtime.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Perform a waitFor on the given process to know when the process has terminated and avoid zombies.
 * <p>
 * Created on 3/27/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
class ForkedActorWatchDogThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(ForkedActorWatchDogThread.class);

    private final String name;
    private final Process process;

    private volatile boolean doWork = true;
    private volatile boolean exited = false;

    ForkedActorWatchDogThread(final String name, final Process process) {
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
            try {
                int exitCode = process.waitFor();
                exited = true;
                LOG.info(this + " Exited with code " + exitCode + ".");
            } catch (InterruptedException ignored) { // NOSONAR
            }
        }
    }

    @Override
    public String toString() {
        return "WatchDogThread{" +
                '\'' + name + '\'' +
                '}';
    }

    boolean hasExited() {
        return exited;
    }
}
