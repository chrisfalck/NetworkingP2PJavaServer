package com.networking.UF;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.networking.UF.client.Client;
import com.networking.UF.server.Server;

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

    /** Populated by the ConfigParser static class */
    CommonConfig commonConfig = null;
    PeerInfoConfig peerInfoConfig = null;
    

    public Peer(int peerId) {
    	this.peerId = peerId;
    }
    
    public void start() {
    	try {
    		// Parse config files and store their info in two config objects.
    		this.commonConfig = ConfigParser.parseCommonFile();
    		this.peerInfoConfig = ConfigParser.parsePeerInfoFile();
    		Server myPeerServer = new Server();
    		
    		// The server is intended to serve information from this Peer.
    		Thread serverThread = new Thread(myPeerServer, "serverThread");
    		serverThread.start();
    		
    		// Create a client connection to each server in the peer info config file.
    		for (int i = 0; i < peerInfoConfig.getConfigLength(); ++i) {
    			
    			// Skip the line in the peer info config that corresponds to this Peer itself.
    			if (peerInfoConfig.getPeerId(i) == this.peerId) {
    				System.out.println("Skipping self from peerInfo config.");
    				continue;
    			}

    			// Clients are intended to retrieve information for this Peer.
    			Thread clientThread = new Thread(new Client(peerInfoConfig.getHostName(i), peerInfoConfig.getListeningPort(i), peerInfoConfig.getPeerId(i)), "clientThread" + i);
    			clientThread.start();
    			
    		}
    		
    		int timeBetweenUnchoked = ConfigParser.parseCommonFile().getUnchokingInterval();
    		int timeBetweenOptimisticallyUnchoked = ConfigParser.parseCommonFile().getOptimisticUnchokingInterval();
    		
    		Timer chokingTimer = new Timer();
    		Timer optimisticChokingTimer = new Timer();
    		
    		chokingTimer.schedule(new TimerTask() {
    			public void run() {
    				myPeerServer.updatePreferredNeighbors();
    			}
    		}, 0, timeBetweenUnchoked);
    		
    		optimisticChokingTimer.schedule(new TimerTask() {
				public void run() {
					myPeerServer.updateOptimisticallyUnchokedNeighbor();
				}
			}, 0, timeBetweenOptimisticallyUnchoked);
    		
    		while (true) {}

    	} catch (Exception exception) {
    		System.err.println("Exception:\n" + exception.toString());
    	}

    	System.exit(0);
    }
    
        /** Accessor methods */
    public int getPeerId() {
        return peerId;
    }

}







