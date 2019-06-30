package com.github.brdr3.pierspool.main;

import com.github.brdr3.pierspool.peer.Peer;
import com.github.brdr3.pierspool.util.constants.Constants;

public class Main {
    public static void main(String[] args) {
        int id = Integer.parseInt(System.getProperty("id"));
        String path = System.getProperty("path");
        Peer peer = new Peer(id, Constants.address[id], Constants.port[id], path);
        peer.start();
    }
}
