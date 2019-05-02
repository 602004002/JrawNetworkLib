/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author nickz
 */
public class P2PUtilities {

    static {
        try {
            DEFAULT_DOMAIN = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException ex) {
            System.err.println(ex);
        }
    }
    public static final int WAIT_LEN = 50;

    public static final int BUFFER_SIZE = 300;
    private static InetAddress DEFAULT_DOMAIN;

    //CLIENT PORT MUST ALWAYS BE SERVER PORT + 1
    public static final int CLIENT_LISTENER_PORT = 9007;
    public static final int SERVER_LISTENER_PORT = 9006;

    public static InetAddress getDefaultDomain() {
        return P2PUtilities.DEFAULT_DOMAIN;
    }

    public static DatagramPacket getBufferPacket() {
        byte[] buf = new byte[BUFFER_SIZE];
        return new DatagramPacket(buf, buf.length);
    }

    public static DatagramPacket convertToPacket(Object o, InetSocketAddress sa) throws IOException {
        byte[] data;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.flush();
            baos.flush();
            data = baos.toByteArray();
            oos.close();
            baos.close();
        }
        return new DatagramPacket(data, data.length, sa);
    }

    public static Object fromPacket(DatagramPacket packet) throws IOException {
        Object o = null;
        try {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais)) {
                o = ois.readObject();
                ois.close();
                bais.close();
            }
        } catch (ClassNotFoundException ex) {
            System.err.println(ex);
        }
        return o;
    }

    public static List<InetAddress> getAllBroadcastAddresses() throws SocketException {
        List<InetAddress> ret = new ArrayList<>();
        Enumeration<NetworkInterface> nwi = NetworkInterface.getNetworkInterfaces();
        while (nwi.hasMoreElements()) {
            NetworkInterface networkInterface = nwi.nextElement();
            if (networkInterface.isLoopback()
                    || !networkInterface.isUp()) {
                continue; // Don't want to broadcast to the loopback interface
            }
            networkInterface.getInterfaceAddresses()
                    .stream()
                    .map(InterfaceAddress::getBroadcast)
                    .filter((address) -> (address != null))
                    .forEach(ret::add);
        }
        return ret;
    }
}
