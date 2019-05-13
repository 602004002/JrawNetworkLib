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

    private IThread iThread;
    private OThread oThread;

    private final Socket socket;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;

    private final ArrayList<ObjectHandler> objectHandlers;
    private final ArrayList<DisconnectHandler> disconnectHandlers;

    private boolean disconnected;

    public NetworkSocketWrapper(Socket socket) throws IOException {
        this.socket = socket;
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.objectHandlers = new ArrayList<>();
        this.disconnectHandlers = new ArrayList<>();
    }

    public void startIOThreads() {
        this.iThread = new IThread();
        this.oThread = new OThread();
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

    public void addHandler(ObjectHandler oh) {
        this.objectHandlers.add(oh);
    }

    public void removeHandler(ObjectHandler oh) {
        this.objectHandlers.remove(oh);
    }
    
    public void addHandler(DisconnectHandler dh) {
        this.disconnectHandlers.add(dh);
    }
    
    public void removeHandler(DisconnectHandler dh) {
        this.disconnectHandlers.remove(dh);
    }

    public void queueSend(Collection c) {
        this.oThread.queue.addAll(c);
    }

    public void queueSend(Object o) {
        this.oThread.queue.add(o);
    }

    public void disconnect() throws IOException {
        handleDisconnect();
        this.oos.close();
        this.ois.close();
        this.socket.close();
        this.disconnected = true;
    }

    public boolean isDisconnected() {
        return this.disconnected;
    }

    /**
     * This method is automatically called when this socket receives an object.
     * It must be implemented to handle objects that arrive.
     *
     * @param obj The incoming object.
     */
    private void handleIncomingObject(Object obj) {
        this.objectHandlers.forEach(oh -> oh.handleObject(obj));
    }

    private void handleDisconnect() {
        for (int i = this.disconnectHandlers.size() - 1; i >= 0; i--) {
            this.disconnectHandlers.remove(i).method();
        }
    }

    private class IThread extends Thread {

        @Override
        public synchronized void run() {
            while (true) {
                try {
                    Object o = ois.readObject();
                    handleIncomingObject(o);
                } catch (IOException | ClassNotFoundException ex) {
                    try {
                        disconnect();
                    } catch (IOException ex1) {
                    }
                    return;
                }
            }
        }
    }

    private class OThread extends Thread {

        private LinkedBlockingQueue queue;

        private OThread() {
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
                    try {
                        disconnect();
                    } catch (IOException ex1) {
                    }
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
