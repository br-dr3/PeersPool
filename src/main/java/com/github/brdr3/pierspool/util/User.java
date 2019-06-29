package com.github.brdr3.pierspool.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bluescreen
 */
public class User {
    private InetAddress address;
    private int port;

    public User(String address, int port) {
        try {
            this.address = InetAddress.getByName(address);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.port = port;
    }
    
    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
