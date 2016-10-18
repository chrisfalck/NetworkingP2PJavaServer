package com.networking.UF;

import com.networking.UF.client.Client;
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
    private Client client;
    private Server server;
    private Protocol protocol;
    private Logger logger;



    public Peer(int peerId) {
        this.peerId = peerId;
        this.logger = logger.getInstance();
    }

    /**
     * Uses the logger to initialize files and directories used throughout the life of the program
     * @returns boolean True if initialization successful, false otherwise
     * @throws IOException
     */
    public boolean initialize() throws IOException {

        logger.initializeDirectoriesAndFiles(peerId);

        if ( logger.confirmConfigFilesExist() ) {
            return true;
        } else {
            return false;
        }
    }

    /** Accessor methods */
    public int getPeerId() {
        return peerId;
    }

}