package com.networking.UF;

import com.networking.UF.Peer;

/**
 * Driver of the program. Creates a Peer with a peer ID provided as the first command line argument.
 * Exits the program if either the peer ID was not provided, or the initial configuration files do not exist.
 */
public class peerProcess {
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("A peer ID must be provided as an argument. Exiting...");
            System.exit(0);
        }

        //TODO
        // Check that peerId matches one from PeerList.cfg

        int peerId = Integer.parseInt(args[0]);

        Peer peer = new Peer(peerId);

        boolean success = peer.initialize();

        if (!success) {
            System.out.println("Both Common.cfg and PeerInfo.cfg must exist in the current working directory. Exiting...");
            System.exit(0);
        };

    }
}