package com.github.brdr3.pierspool.peer;

import com.github.brdr3.pierspool.util.Message;
import com.github.brdr3.pierspool.util.Message.MessageBuilder;
import com.github.brdr3.pierspool.util.Tuple;
import com.github.brdr3.pierspool.util.User;
import com.github.brdr3.pierspool.util.Utils;
import com.github.brdr3.pierspool.util.constants.Constants;
import com.google.gson.Gson;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Peer {

    private final Gson gson = new Gson();

    private final Thread receiver;
    private final Thread sender;
    private final Thread dataGetter;
    private final Thread gossipTime;
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

    public Peer(int id, String address, int port, String path) throws Exception {

        this.id = id;

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

        gossipTime = new Thread() {
            @Override
            public void run() {
                gossip();
            }
        };

        processor = new Thread() {
            @Override
            public void run() {
                process();
            }
        };

        confessor = new Thread() {
            @Override
            public void run() {
                confess();
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
        processor.start();
        confessor.start();
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
            User gossiped;
            Entry<User, Tuple<HashMap<String, File>, Long>> gossip;

            do {
                pair = r.nextInt(Constants.users.length);
            } while (pair == this.id);

            if (peersStatus.isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                continue;
            } else {
                Object users[] = peersStatus.values().toArray();
                gossiped = (User) users[r.nextInt(users.length)];

                // Functional Programming
                // Pick the map entry that has the user that I want to gossip.
                // In another words, pick what I want to gossip.
                gossip = peersStatus.entrySet()
                        .stream()
                        .filter(e -> e.getKey().equals(gossiped))
                        .findFirst().get();
            }

            Message m;
            synchronized (peersStatus) {
                m = messageBuilder
                        .content(gossip)
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

    public void process() {
        while (true) {
            Message m = processQueue.poll();
            try {
                processMessage(m);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void processMessage(Message m) {
        Entry<User, Tuple<HashMap<String, File>, Long>> peerStatus
                = (Entry<User, Tuple<HashMap<String, File>, Long>>) m.getContent();

        Long peerStatusVersion = peerStatus.getValue().getY();

        if (peersStatus.containsKey(peerStatus.getKey())) {
            Long maxPeerStatusVersion = peersStatus.get(m.getFrom()).getY();
            if (maxPeerStatusVersion < peerStatusVersion) {
                System.out.println("New entry to peer " + m.getFrom() + "!");
                peersStatus.put(peerStatus.getKey(), peerStatus.getValue());
            } else if (maxPeerStatusVersion.equals(peerStatusVersion)) {
                System.out.println("This entry is duplicated to peer " + m.getFrom() + "!");
            } else {
                System.out.println("This entry is old to peer " + m.getFrom() + "!");
            }
        } else {
            System.out.println("First peer entry. Peer: " + m.getFrom() + ".");
            peersStatus.put(peerStatus.getKey(), peerStatus.getValue());
        }
    }

    public void confess() {
        while (true) {
            MessageBuilder messageBuilder = new MessageBuilder();
            int pair;
            Entry<User, Tuple<HashMap<String, File>, Long>> gossip;
            Message m;

            do {
                pair = new Random().nextInt(Constants.users.length);
            } while (pair == this.id);

            synchronized (fileStatus) {
                gossip = new FileStatusEntry<>(Constants.users[this.id],
                        new Tuple<>(fileStatus, version));

                m = messageBuilder
                        .content(gossip)
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

    private final class FileStatusEntry<K, V> implements Entry<K, V> {

        private final K key;
        private V value;

        private FileStatusEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }

    }
}
