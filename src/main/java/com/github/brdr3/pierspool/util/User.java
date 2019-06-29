package com.github.brdr3.pierspool.util;

import java.net.InetAddress;

/**
 *
 * @author bluescreen
 */
public class User {
    private InetAddress address;
    private int port;

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
