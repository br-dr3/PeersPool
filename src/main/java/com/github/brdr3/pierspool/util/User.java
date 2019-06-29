package com.github.brdr3.pierspool.util;

import java.net.InetAddress;
import java.util.Objects;

public class User {
    int id;
    private InetAddress address;
    private int port;

    public User(int id, String address, int port) {
        this.id = id;
        
        try {
            this.address = InetAddress.getByName(address);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.id);
        hash = 71 * hash + Objects.hashCode(this.address);
        hash = 71 * hash + Objects.hashCode(this.port);
        return hash;
    }
}