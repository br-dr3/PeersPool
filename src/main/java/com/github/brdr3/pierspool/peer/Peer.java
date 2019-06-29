package com.github.brdr3.pierspool.peer;

import com.github.brdr3.pierspool.util.Message;
import com.github.brdr3.pierspool.util.Message.MessageBuilder;
import com.github.brdr3.pierspool.util.Utils;
import com.github.brdr3.pierspool.util.constants.Constants;
import com.google.gson.Gson;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peer {

    private final Gson gson = new Gson();
    private final Thread receiver;
    private final Thread sender;
    private final Thread dataGetter;
    private final Thread gossipTime;
    private final File folder;
    private HashMap<String, File> fileStatus;
    private final int id;
    private final int port;
    private final InetAddress address;
    private final ConcurrentLinkedQueue<Message> processQueue;
    private final ConcurrentLinkedQueue<Message> sendQueue;
    private Long version = (long) 0;

    public Peer(int id, String address, int port, String path) throws Exception {
        
        this.id = id;
        
        gossipTime = new Thread() {
            @Override
            public void run() {
                gossip();
            }
        };

        receiver = new Thread() {
            @Override
            public void run() {
                receive();
            }
        };

        sender = new Thread() {
            @Override
            public void run() {
                send();
            }
        };

        dataGetter = new Thread() {
            @Override
            public void run() {
                getDataStatus();
            }
        };

        this.address = InetAddress.getLocalHost();
        this.port = port;

        this.processQueue = new ConcurrentLinkedQueue<>();
        this.sendQueue = new ConcurrentLinkedQueue<>();
        this.folder = new File(path);

        this.fileStatus = new HashMap<>();
    }

    public void start() {
        receiver.start();
        sender.start();
        gossipTime.start();
        dataGetter.start();
    }

    public void receive() {
        DatagramSocket socket;
        DatagramPacket packet;
        String jsonMessage;
        Message message;
        byte buffer[] = new byte[10000];

        try {
            socket = new DatagramSocket(this.port);
            while (true) {
                packet = new DatagramPacket(buffer, buffer.length, this.address, this.port);

                socket.receive(packet);

                jsonMessage = new String(packet.getData()).trim();
                message = gson.fromJson(jsonMessage, Message.class);
                processQueue.add(message);

                Utils.cleanBuffer(buffer);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void send() {
        while (true) {
            Message m = sendQueue.poll();
            try {
                sendMessage(m);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void getDataStatus() {
        while (true) {
            boolean newVersion = false;
            HashMap<String, File> auxiliarFileStatus = new HashMap<>();

            for (File f : this.folder.listFiles()) {
                newVersion = f.isFile() && !fileStatus.containsValue(f);
                auxiliarFileStatus.put(f.getName(), f);
            }

            synchronized (fileStatus) {
                fileStatus = auxiliarFileStatus;
            }

            if (newVersion) {
                version++;
            }

            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendMessage(Message m) throws Exception {
        String jsonMessage = gson.toJson(m);
        byte buffer[] = new byte[10000];
        DatagramSocket socket;
        DatagramPacket packet;

        buffer = jsonMessage.getBytes();
        packet = new DatagramPacket(buffer, buffer.length, m.getTo().getAddress(),
                m.getTo().getPort());

        socket = new DatagramSocket();
        socket.send(packet);
        socket.close();
    }

    public void gossip() {
        while (true) {
            MessageBuilder messageBuilder = new MessageBuilder();
            
            Random r = new Random();
            int pair;
            
            do {
                pair = r.nextInt(Constants.users.length);
            } while(pair == this.id); 
            
            Message m;
            synchronized (fileStatus) {
                m = messageBuilder.content(fileStatus)
                                  .id(version)
                                  .from(Constants.users[id])
                                  .to(Constants.users[pair])
                                  .build();
                                
            }
            
            sendQueue.add(m);
            
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
