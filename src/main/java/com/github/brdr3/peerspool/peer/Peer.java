package com.github.brdr3.peerspool.peer;

import com.github.brdr3.peerspool.util.Message;
import com.github.brdr3.peerspool.util.FileStatusEntry;
import com.github.brdr3.peerspool.util.Message.MessageBuilder;
import com.github.brdr3.peerspool.util.Tuple;
import com.github.brdr3.peerspool.util.User;
import com.github.brdr3.peerspool.util.Utils;
import com.github.brdr3.peerspool.util.constants.Constants;
import com.google.gson.Gson;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Peer {

    private final Gson gson = new Gson();

    private final Thread receiver;
    private final Thread sender;
    private final Thread dataGetter;
    private final Thread gossiper;
    private final Thread processor;
    private final Thread confessor;

    private final File folder;
    private final int id;
    private final int port;
    private final InetAddress address;
    private final ConcurrentLinkedQueue<Message> processQueue;
    private final ConcurrentLinkedQueue<Message> sendQueue;
    private Long version = (long) 0;

    private volatile HashMap<String, File> fileStatus;
    private volatile HashMap<User, Tuple<HashMap<String, File>, Long>> peersStatus;

    public Peer(int id, String address, int port, String path) {
        receiver = new Thread() {
            @Override
            public void run() {
                System.out.println("receiver > Hi!");
                receive();
            }
        };

        sender = new Thread() {
            @Override
            public void run() {
                System.out.println("sender > Hi!");
                send();
            }
        };

        dataGetter = new Thread() {
            @Override
            public void run() {
                System.out.println("dataGetter > Hi!");
                getDataStatus();
            }
        };

        gossiper = new Thread() {
            @Override
            public void run() {
                System.out.println("gossiper > Hi!");
                gossip();
            }
        };

        processor = new Thread() {
            @Override
            public void run() {
                System.out.println("processor > Hi!");
                process();
            }
        };

        confessor = new Thread() {
            @Override
            public void run() {
                System.out.println("confessor > Hi!");
                confess();
            }
        };

        this.id = id;

        try {
            this.address = InetAddress.getLocalHost();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Could not create Peer. Address error.");
        }

        this.port = port;
        this.processQueue = new ConcurrentLinkedQueue<>();
        this.sendQueue = new ConcurrentLinkedQueue<>();
        this.folder = new File(path);
        this.fileStatus = new HashMap<>();
        this.peersStatus = new HashMap<>();
    }

    public void start() {
        dataGetter.start();
        receiver.start();
        sender.start();
        gossiper.start();
        processor.start();
        confessor.start();
    }

    public void receive() {
        receive(false);
    }

    public void receive(boolean silent) {
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

                if (!silent) {
                    System.out.println("receiver > Message received from " + message.getFrom());
                }

                processQueue.add(message);
                Utils.cleanBuffer(buffer);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void send() {
        send(false);
    }

    public void send(boolean silent) {
        while (true) {
            Message m = sendQueue.poll();
            
            try {
                if (m != null) {
                    sendMessage(m);
                    
                    if (!silent) {
                        System.out.println("sender > Message sent to " + m.getTo());
                    }
                }
                
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void getDataStatus() {
        getDataStatus(false);
    }

    public void getDataStatus(boolean silent) {
        while (true) {
            boolean newVersion = false;
            HashMap<String, File> auxiliarFileStatus = new HashMap<>();

            if (!silent) {
                System.out.println("dataGetter > Retrieving data from folder " + folder.getAbsolutePath());
            }

            for (File f : this.folder.listFiles()) {
                newVersion |= f.isFile() && !fileStatus.containsValue(f);
                auxiliarFileStatus.put(f.getName(), f);
            }

            for (File f : this.fileStatus.values()) {
                newVersion |= !auxiliarFileStatus.containsValue(f);
            }

            if (!silent) {
                System.out.println("dataGetter > Data retrieved.");
            }

            synchronized (fileStatus) {
                fileStatus = auxiliarFileStatus;
                if (newVersion) {
                    if (!silent) {
                        System.out.println("dataGetter > New version of files.");
                        System.out.println("dataGetter > fileStatus = " + fileStatus);
                    }
                    
                    version++;
                }
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
        gossip(false);
    }

    public void gossip(boolean silent) {
        while (true) {
            MessageBuilder messageBuilder = new MessageBuilder();

            Random r = new Random();
            int pair;
            User gossiped;
            FileStatusEntry<User, Tuple<HashMap<String, File>, Long>> gossip;

            do {
                pair = r.nextInt(Constants.users.length);
            } while (pair == this.id);

            synchronized (peersStatus) {
                if (peersStatus.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    continue;
                } else {
                    Object users[] = peersStatus.keySet().toArray();
                    gossiped = (User) users[r.nextInt(users.length)];
                    gossip = new FileStatusEntry<>(gossiped, peersStatus.get(gossiped));
                }
            }

            Message m;
            m = messageBuilder
                    .content(gossip)
                    .id(version)
                    .from(Constants.users[id])
                    .to(Constants.users[pair])
                    .build();

            if (!silent) {
                System.out.println("gossiper > gossiping about " + gossiped + " to " + Constants.users[pair] + "!");
            }

            sendQueue.add(m);

            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void process() {
        process(false);
    }

    public void process(boolean silent) {
        while (true) {
            Message m = processQueue.poll();
            
            try {
                if (m != null) {
                    processMessage(m, silent);
                    
                    if (!silent) {
                        System.out.println("processor > Processing message " + m);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void processMessage(Message m, boolean silent) {
        FileStatusEntry<User, Tuple<HashMap<String, File>, Long>> peerStatus
                = (FileStatusEntry<User, Tuple<HashMap<String, File>, Long>>) m.getContent();

        User gossiped = peerStatus.getKey();
        Tuple<HashMap<String, File>, Long> gossip = peerStatus.getValue();
        Long peerStatusVersion = gossip.getY();

        synchronized (peersStatus) {
            if (peersStatus.containsKey(gossiped)) {
                Long maxPeerStatusVersion = peersStatus.get(gossiped).getY();
                if (maxPeerStatusVersion < peerStatusVersion) {
                    if (!silent) {
                        System.out.println("processor > New entry to peer " + gossiped + "!");
                    }
                    
                    peersStatus.put(gossiped, gossip);
                } else if (maxPeerStatusVersion.equals(peerStatusVersion)) {
                    if (!silent) {
                        System.out.println("processor > This entry is duplicated to peer " + m.getFrom() + "!");
                    }
                } else {
                    if (!silent) {
                        System.out.println("processor > This entry is old to peer " + m.getFrom() + "!");
                    }
                }
            } else {
                if (!silent) {
                    System.out.println("processor > First peer entry. Peer: " + m.getFrom() + ".");
                }
                peersStatus.put(gossiped, gossip);
            }
        }
    }

    public void confess() {
        confess(false);
    }

    public void confess(boolean silent) {
        while (true) {
            MessageBuilder messageBuilder = new MessageBuilder();
            int pair;
            FileStatusEntry<User, Tuple<HashMap<String, File>, Long>> confess;
            Message m;

            do {
                pair = new Random().nextInt(Constants.users.length);
            } while (pair == this.id);

            synchronized (fileStatus) {
                confess = new FileStatusEntry<>(Constants.users[id],
                        new Tuple<>(fileStatus, version));
            }

            m = messageBuilder
                    .content(confess)
                    .id(version)
                    .from(Constants.users[id])
                    .to(Constants.users[pair])
                    .build();

            if (!silent) {
                System.out.println("confessor > confessing to " + Constants.users[pair] + "!");
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
