package com.networking.UF;

import com.networking.UF.client.Client;
import com.networking.UF.client.MyClient;
import com.networking.UF.server.MyServer;
import com.networking.UF.server.Server;
import com.networking.UF.Logger;

import java.io.IOException;

/**
 * The Peer creates a Client and Server parameterized according to two config files:
 *  1) Common.cfg
 *  2) PeerList.cfg
 *
 * The Peer will then provide it's Client and Server with data needed by both:
 * - the bitfield
 */
public class Peer {

    /** Member data */
    private int peerId;

    /** Associations */
    private MyClient client;
    private MyServer server;
    private Protocol protocol = new P2PProtocol();
    private static Logger logger = Logger.getInstance();
    private static FileManager fileManager = FileManager.getInstance();

    public Peer(int peerId) {
        this.peerId = peerId;
    }

    public void initialize() throws IOException {
        server = new MyServer(peerId);
        server.start();
        System.out.println("Peer: Server started.");

        client = new MyClient(peerId);
        client.start();
        System.out.println("Peer: Client started.");
    }

    /** Accessor methods */
    public int getPeerId() {
        return peerId;
    }

}