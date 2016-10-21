package com.networking.UF;

import com.networking.UF.client.Client;
import com.networking.UF.client.MyClient;
import com.networking.UF.server.MyServer;
import com.networking.UF.server.Server;
import com.networking.UF.Logger;

import java.io.File;
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
    private ProcessBuilder clientPb = new ProcessBuilder("java", "-classpath", "target/classes", "com.networking.UF.client.MyClient", Integer.toString(peerId));
    private ProcessBuilder serverPb = new ProcessBuilder("java", "-classpath", "target/classes", "com.networking.UF.server.MyServer", Integer.toString(peerId));
//    private MyClient client = new MyClient();
//    private MyServer server = new MyServer();
    private Protocol protocol = new P2PProtocol();
    private static Logger logger = Logger.getInstance();
    private static FileManager fileManager = FileManager.getInstance();

    public Peer(int peerId) {
        this.peerId = peerId;
    }

    public void initialize() throws IOException {
        File serverLog = new File("server.log");
        serverPb.redirectOutput(serverLog);
        serverPb.redirectError(serverLog);
        serverPb.start();
        System.out.println("Peer: Server started.");

        File clientLog = new File("client.log");
        clientPb.redirectOutput(clientLog);
        clientPb.redirectError(clientLog);
        clientPb.start();
        System.out.println("Peer: Client started.");
    }

    /** Accessor methods */
    public int getPeerId() {
        return peerId;
    }

}