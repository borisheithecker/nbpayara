package org.nbpayara.core.gfconfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Random;
import javax.net.ServerSocketFactory;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
class Util {

    public static final int PORT_MAX = 65535;
    private static final Random GENERATOR = new Random(System.currentTimeMillis());

    @Messages({"Util.findAvailablePort.noPort=Could not allocate any available port number."})
    static String findAvailablePort(int portMin, List<Integer> alreadAllocated) {
        if (portMin < 0 || portMin > PORT_MAX) {
            throw new IllegalArgumentException();
        }
        int c = 0;
        while (c < PORT_MAX - portMin) {
            final Integer port = portMin + GENERATOR.nextInt(PORT_MAX - portMin);
            if (!alreadAllocated.contains(port) && isPortAvailable(port)) {
                alreadAllocated.add(port);
                return port.toString();
            }
        }
        throw new IllegalStateException();
    }

    private static boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);
            serverSocket.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
