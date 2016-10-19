package com.networking.UF;

import com.networking.UF.client.Client;
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
    private Client client;
    private Server server;
    private Protocol protocol;
    private static Logger logger = Logger.getInstance();
    private static FileManager fileManager = FileManager.getInstance();

    public Peer(int peerId) {
        this.peerId = peerId;
        this.logger = logger.getInstance();
    }

    /** Accessor methods */
    public int getPeerId() {
        return peerId;
    }

}