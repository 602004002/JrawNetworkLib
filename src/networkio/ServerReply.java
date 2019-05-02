/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkio;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

/**
 *
 * @author nickz
 */
public class ServerReply implements Serializable {
    
    private static final long serialVersionUID = 10L;

    private UUID clientUUID, serverUUID;
    private int serverPort;
    private String serverName;
    private transient InetAddress from;

    public ServerReply(String serverName,
            UUID clientUUID, UUID serverUUID,
            int serverPort) {
        this.serverName = serverName;
        this.clientUUID = clientUUID;
        this.serverUUID = serverUUID;
        this.serverPort = serverPort;
    }

    public UUID getClientUUID() {
        return this.clientUUID;
    }

    public UUID getServerUUID() {
        return this.serverUUID;
    }

    public String getServerName() {
        return this.serverName;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public void setAddress(InetAddress from) {
        this.from = from;
    }

    public InetAddress getAddress() {
        return this.from;
    }

    @Override
    public String toString() {
        return "Server Reply " + this.serverUUID
                + " @ " + from + "\nFrom client " + this.clientUUID;
    }
}
