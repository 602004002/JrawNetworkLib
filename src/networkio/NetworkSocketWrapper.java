/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author nickz
 */
public class NetworkSocketWrapper {

    private InThread iThread;
    private OutThread oThread;

    private final Socket socket;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;

    private final ArrayList<ObjectHandler> objectHandlers;
    private final ArrayList<DisconnectHandler> disconnectHandlers;

    public NetworkSocketWrapper(Socket socket) throws IOException {
        this.socket = socket;
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.objectHandlers = new ArrayList<>();
        this.disconnectHandlers = new ArrayList<>();
    }

    public synchronized void startIOThreads() {
        this.iThread = new InThread();
        this.oThread = new OutThread();
        this.iThread.start();
        this.oThread.start();
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public boolean inputThreadActive() {
        if (this.iThread == null) {
            return false;
        }
        return this.iThread.isAlive();
    }

    public boolean outputThreadActive() {
        if (this.oThread == null) {
            return false;
        }
        return this.oThread.isAlive();
    }

    public synchronized void addHandler(ObjectHandler oh) {
        this.objectHandlers.add(oh);
    }

    public synchronized void removeHandler(ObjectHandler oh) {
        this.objectHandlers.remove(oh);
    }

    public synchronized void addDisconnectHandler(DisconnectHandler dh) {
        this.disconnectHandlers.add(dh);
    }

    public synchronized void removeHandler(DisconnectHandler dh) {
        this.disconnectHandlers.remove(dh);
    }

    public synchronized void queueSend(Collection c) {
        this.oThread.queue.addAll(c);
    }

    public synchronized void queueSend(Object o) {
        this.oThread.queue.add(o);
    }

    public void disconnect() {
        handleDisconnect();
        try {
            this.oos.close();
            this.ois.close();
            this.socket.close();
        } catch (IOException ex) {
        }
    }

    /**
     * This method is automatically called when this socket receives an object.
     * It must be implemented to handle objects that arrive.
     *
     * @param obj The incoming object.
     */
    private synchronized void handleIncomingObject(Object obj) {
        for (int i = this.objectHandlers.size() - 1; i >= 0; i--) {
            this.objectHandlers.get(i).handleObject(obj);
        }
    }

    private synchronized void handleDisconnect() {
        for (int i = this.disconnectHandlers.size() - 1; i >= 0; i--) {
            this.disconnectHandlers.remove(i).method();
        }
    }

    private class InThread extends Thread {

        InThread() {
            super("InThead @ " + NetworkSocketWrapper.this.toString());
        }

        @Override
        public synchronized void run() {
            while (true) {
                try {
                    Object o = ois.readObject();
                    handleIncomingObject(o);
                } catch (IOException | ClassNotFoundException ex) {
                    disconnect();
                    return;
                }
            }
        }
    }

    private class OutThread extends Thread {

        private LinkedBlockingQueue queue;

        private OutThread() {
            super("OutThead @ " + NetworkSocketWrapper.this.toString());
            this.queue = new LinkedBlockingQueue();
        }

        @Override
        public synchronized void run() {
            while (true) {
                try {
                    oos.writeObject(queue.take());
                    oos.flush();
                    oos.reset();
                    while (!queue.isEmpty()) {
                        oos.writeObject(queue.take());
                        oos.flush();
                        oos.reset();
                    }
                } catch (IOException | InterruptedException ex) {
                    disconnect();
                    return;
                }
            }
        }
    }

    @Override
    public String toString() {
        return this.socket.toString();
    }

}
