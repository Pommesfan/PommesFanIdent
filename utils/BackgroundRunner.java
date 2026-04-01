package utils;

import controller.Controller;
import jdk.jshell.execution.Util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;

public abstract class BackgroundRunner {
    final private Thread t;
    final protected ServerSocket serverSocket;
    final protected byte[] crypto_hash;

    public BackgroundRunner(Controller c) throws NoSuchAlgorithmException, IOException {
        serverSocket = new ServerSocket(0);
        String crypto = Utils.getAlphanumeric(16);
        crypto_hash = Utils.passwordHash(crypto);
        String ip = InetAddress.getLocalHost().getHostAddress();
        c.notifyObservers(new OutputEvent.ServerStartedEvent(ip, serverSocket.getLocalPort(), crypto));
        t = new Thread(() -> {
            try {
                routine();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected abstract void routine() throws Exception;

    public void start() {
        t.start();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }
}