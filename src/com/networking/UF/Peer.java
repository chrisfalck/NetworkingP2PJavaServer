package com.networking.UF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.networking.UF.client.Client;
import com.networking.UF.server.ConnectionState;
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
	private ArrayList<Client> myClients;

    /** Populated by the ConfigParser static class */
    CommonConfig commonConfig = null;
    PeerInfoConfig peerInfoConfig = null;

    public Peer(int peerId) {
    	this.peerId = peerId;
		myClients = new ArrayList<Client>();
    }

	/**
	 * WARNING: The values of the hashmap this method returns are ConnectionStates for the clients this Peer has spun threads for
	 *          The keys are the peerIDs of the servers those clients are connected to
	 */
//	public ConcurrentHashMap<Integer,ConnectionState> getThisPeersClientConnectionStates() {
//		ConcurrentHashMap<Integer,ConnectionState> thisPeersClientConnectionStates = new ConcurrentHashMap<Integer,ConnectionState>();
//		for (Client client: myClients) {
//			thisPeersClientConnectionStates.put(client.getServerPeerId(), client.getConnectionState());
//		}
//		return thisPeersClientConnectionStates;
//	}

	/**
	 * Currently only used for testing
	 * @param client
	 */
	public void addClient(Client client) {
		this.myClients.add(client);
	}
    
    public void start() {
    	try {
    		// Parse config files and store their info in two config objects.
    		this.commonConfig = ConfigParser.parseCommonFile();
    		this.peerInfoConfig = ConfigParser.parsePeerInfoFile();
    		Server myPeerServer = new Server(this);
    		this.myClients = new ArrayList<Client>();
    		
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
    			Client tempClient = new Client(peerInfoConfig.getHostName(i), peerInfoConfig.getListeningPort(i), peerInfoConfig.getPeerId(i), this);
    			myClients.add(tempClient);
    			Thread clientThread = new Thread(tempClient ,"clientThread" + i);
    			clientThread.start();
    			
    		}
    		
//    		int timeBetweenUnchoked = ConfigParser.parseCommonFile().getUnchokingInterval();
//    		int timeBetweenOptimisticallyUnchoked = ConfigParser.parseCommonFile().getOptimisticUnchokingInterval();
//    		
//    		System.out.println(timeBetweenOptimisticallyUnchoked + " " + timeBetweenUnchoked);
//    		
//    		Timer chokingTimer = new Timer();
//    		Timer optimisticChokingTimer = new Timer();
//    		
//    		chokingTimer.schedule(new TimerTask() {
//    			public void run() {
//    				System.out.println("Updating preferred neighbors.");
//    				myPeerServer.updatePreferredNeighbors();
//    			}
//    		}, 20 * 1000, timeBetweenUnchoked * 1000);
//    		
//    		optimisticChokingTimer.schedule(new TimerTask() {
//				public void run() {
//    				System.out.println("Updating optimistically unchoked neighbor.");
//					myPeerServer.updateOptimisticallyUnchokedNeighbor();
//				}
//			}, 23 * 1000, timeBetweenOptimisticallyUnchoked * 1000);
    		
    		while (true) {}

    	} catch (Exception exception) {
    		System.err.println("Exception:\n" + exception.toString());
    	}
    }
    
    public void broadcastShouldSendHaveMessages(byte[] currentHaveMessageIndexToSend) {
    	for (Client currClient : myClients) {
    		currClient.setShouldSendHaveMessage(true);
    		currClient.setCurrentHaveMessageIndexToSend(currentHaveMessageIndexToSend);
    	}
    }
    
    /** Accessor methods */
    public int getPeerId() {
        return peerId;
    }

}







