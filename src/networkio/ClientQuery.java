/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkio;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author nickz
 */
public class ClientQuery implements Serializable {
    private UUID uuid;
    
    public ClientQuery(UUID uuid) {
        this.uuid = uuid;
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
    
    @Override
    public String toString() {
        return "Client Query " + this.uuid;
    }
}
