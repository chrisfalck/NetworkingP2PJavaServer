package com.networking.UF;

import com.networking.UF.client.Client;
import com.networking.UF.client.MyClient;
import com.networking.UF.server.MyServer;
import com.networking.UF.server.Server;
import com.networking.UF.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

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
    private ProcessBuilder clientPb = new ProcessBuilder("java", "-classpath", "/home/chausen/Dev/NetworkingP2PJavaServer/NetworkingP2PJavaServer/target/classes", "com.networking.UF.client.MyClient");
    private ProcessBuilder serverPb = new ProcessBuilder("java", "-classpath", "/home/chausen/Dev/NetworkingP2PJavaServer/NetworkingP2PJavaServer/target/classes", "com.networking.UF.server.MyServer");
    private MyClient client = new MyClient();
    private MyServer server = new MyServer();
    private Protocol protocol = new P2PProtocol();
    private static Logger logger = Logger.getInstance();
    private static FileManager fileManager = FileManager.getInstance();

    public Peer(int peerId) {
        this.peerId = peerId;
    }

    public void initialize() throws IOException {
        System.out.println(Paths.get(".").toAbsolutePath().normalize().toString());
        serverPb.start();
        clientPb.start();
    }

    /** Accessor methods */
    public int getPeerId() {
        return peerId;
    }

}