package io.amaze.bench.shared.helper;

import com.google.common.base.Throwables;

import java.net.ServerSocket;

/**
 * Created on 3/19/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public final class NetworkHelper {

    public static final String LOCALHOST = "localhost";

    private NetworkHelper() {
        // Helper class
    }

    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
