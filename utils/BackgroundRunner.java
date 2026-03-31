package utils;

import java.net.ServerSocket;

public abstract class BackgroundRunner {
    final private Thread t;
    final protected ServerSocket serverSocket;
    final protected byte[]password_hash;

    public BackgroundRunner(ServerSocket serverSocket, byte[]password_hash) {
        t = new Thread(() -> {
            try {
                routine();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.serverSocket = serverSocket;
        this.password_hash = password_hash;
    }

    protected abstract void routine() throws Exception;

    public void start() {
        t.start();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }
}