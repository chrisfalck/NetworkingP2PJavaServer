package com.networking.UF.server;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.networking.UF.ConfigParser;
import com.networking.UF.FileManager;
import com.networking.UF.Logger;
import com.networking.UF.MessageType;
import com.networking.UF.P2PProtocol;
import com.networking.UF.Peer;
import com.networking.UF.messages.HandshakeMessage;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;

public class Server implements Runnable {

	private static final int sPort = 6009;   //The server will be listening on this port number
	static FileManager fileManager = FileManager.getInstance();
	static Logger logger = Logger.getInstance();
	private int numPreferredNeighbors = 0;
	Peer myPeer;

	ConcurrentHashMap<Integer, ConnectionState> connectionStates = new ConcurrentHashMap<Integer, ConnectionState>();
	
	public Server(Peer myPeer) {
		this.myPeer = myPeer;
	}
	
	public ConnectionState getConnectionState(Integer peerId) {
		return connectionStates.get(peerId);
	}
	
	private class PeerAndSpeed {
		public int peerAndSpeedId;
		public long peerAndSpeedSpeed;
		public PeerAndSpeed(int peerId, long peerSpeed) {
			peerAndSpeedId = peerId;
			peerAndSpeedSpeed = peerSpeed;
		}
	}

	public void updateOptimisticallyUnchokedNeighbor() {
		Random generator = new Random(); 
		try {
			ArrayList<Integer> peerIds = new ArrayList<Integer>();

			Enumeration<Integer> connectionStateKeys = connectionStates.keys();
			while(connectionStateKeys.hasMoreElements()) {
				peerIds.add(connectionStates.get(connectionStateKeys.nextElement()).getPeerId());
			}

			int randomPeerId = peerIds.get(generator.nextInt(peerIds.size()));
			while (true) {
				boolean isNotPreferredNeighbor = (connectionStates.get(randomPeerId).isChoked() == true);
				boolean isNotOwnPeer = (connectionStates.get(randomPeerId).getPeerId() != fileManager.getThisPeerIdentifier());
				if (isNotOwnPeer && isNotPreferredNeighbor) break;
				else {
					randomPeerId = generator.nextInt(ConfigParser.parsePeerInfoFile().getConfigLength());
				}
			}

			connectionStates.get(randomPeerId).setOptimisticallyUnchoked(true);
			connectionStates.get(randomPeerId).setNeedToUpdateOptimisticNeighbor(true);
			logger.logChangeOfOptimisticallyUnchokedNeighbor(connectionStates.get(randomPeerId).getPeerId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update the list of preferred neighbors for this peer. Preferred neighbors are unchoked so that they can
	 * request pieces from this peer. Preferred neighbors are determined by the downloading rate of the clients created
	 * by this peer (the downloading rate of another peer depends on this peer's client's download rate from that peer)
	 */
	public void updatePreferredNeighbors() {
		ConcurrentHashMap<Integer,ConnectionState> thisPeersClientConnectionStates = myPeer.getThisPeersClientConnectionStates();
		Enumeration<Integer> peerIds = thisPeersClientConnectionStates.keys();
		ArrayList<PeerAndSpeed> unsortedPeers = new ArrayList<PeerAndSpeed>();
		ArrayList<PeerAndSpeed> sortedPeers = new ArrayList<PeerAndSpeed>();
		while(peerIds.hasMoreElements()) {
			int currentPeerId = peerIds.nextElement();
			ConnectionState currentConnectionState = thisPeersClientConnectionStates.get(currentPeerId);
			if(currentConnectionState.isInterested() == true){
				unsortedPeers.add(new PeerAndSpeed(currentPeerId, currentConnectionState.getConnectionSpeed()));
			}
		}


		// At the end of this loop, sortedPeers will contain fastest to slowest peers from lowest to highest index.
		Random generator = new Random();
		while (unsortedPeers.size() > 0) {
			long connectionSpeed = 0;
			int fastestPeerIdIndex = 0;
			for (int i = 0; i < unsortedPeers.size(); ++i) {
				if (unsortedPeers.get(i).peerAndSpeedSpeed > connectionSpeed) {
					connectionSpeed = unsortedPeers.get(i).peerAndSpeedSpeed;
					fastestPeerIdIndex = i;
				} else if (unsortedPeers.get(i).peerAndSpeedSpeed == connectionSpeed) {
					int tieBreaker  = generator.nextInt(2);
					if (tieBreaker == 0) {
						connectionSpeed = unsortedPeers.get(i).peerAndSpeedSpeed;
					}
				}
			}
			sortedPeers.add(unsortedPeers.get(fastestPeerIdIndex));
			unsortedPeers.remove(fastestPeerIdIndex);
		}

		try {
			if (numPreferredNeighbors == 0) {
				numPreferredNeighbors = ConfigParser.parseCommonFile().getNumberOfPreferredNeighbors();
			}
			List<Integer> preferredNeighbors = new ArrayList<Integer>();
			for (int i = 0; i < thisPeersClientConnectionStates.size(); ++i) {
				if (i < numPreferredNeighbors) {
					connectionStates.get(sortedPeers.get(i).peerAndSpeedId).setChoked(false);
					preferredNeighbors.add(sortedPeers.get(i).peerAndSpeedId);
				} else {
					connectionStates.get(sortedPeers.get(i).peerAndSpeedId).setChoked(true);
				}
				connectionStates.get(sortedPeers.get(i).peerAndSpeedId).setNeedToUpdatePreferredNeighbors(true);
			}
			logger.logChangeOfPreferredNeighbors(preferredNeighbors);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void setConnectionState(Integer peerId, ConnectionState connectionState) {
		connectionStates.put(peerId, connectionState);
		System.out.println("\nServer for peer " + fileManager.getThisPeerIdentifier() + " updated connection state for peer " + peerId + ".\n" + connectionState.toString() + "\n");
	}

	public void setNumPreferredNeighbors(int numPreferredNeighbors) {
		this.numPreferredNeighbors = numPreferredNeighbors;
	}

	public void run() {
		try {
			System.out.println("The server is running."); 
			ServerSocket listener = new ServerSocket(sPort);
			int clientNum = 1;
			try {
				while(true) {
					new Handler(listener.accept(), clientNum, this).start();
					clientNum++;
				}
			} finally {
				listener.close();
			} 

		} catch (Exception exception) {
			System.out.println(exception.getStackTrace());
		}

	}

	/**
	 * A handler thread class.  Handlers are spawned from the listening
	 * loop and are responsible for dealing with a single client's requests.
	 */
	public static class Handler extends Thread {
		// The Socket representing our TCP connectin. 
		private Socket connection;
		
		// The instream from our TCP connection. 
		private ObjectInputStream in;	
		
		// The outstream for our TCP connection. 
		private ObjectOutputStream out;    

		// The Server that started this Handler thread. 
		private Server myServer;
		
		// The P2PProtocol created for use by this Handler thread. 
		private P2PProtocol p2pProtocol;

		// Determines whether the Handler should send messages first or receive them
		private boolean waiting = true;


		public Handler(Socket connection, int no, Server server) {
			this.connection = connection;
			this.myServer = server;
			this.p2pProtocol = new P2PProtocol(fileManager.getThisPeerIdentifier(), "server", myServer);

		}
		
		/**
		 * Determines the next message the client should send by analysing the client's state variables. 
		 * Returns an appropriate built message to be sent. 
		 * @return
		 * @throws InterruptedException
		 */
		private Message getNextMessageToSend() throws InterruptedException {
			ConnectionState connectionState = myServer.getConnectionState(p2pProtocol.getConnectedPeerId());
			if (connectionState.haveReceivedHandshake() && !connectionState.haveReceivedBitfield()) {
				// Send Handshake
				System.out.println("Building handshake message to send to client.");

				return new HandshakeMessage(fileManager.getThisPeerIdentifier());
			}
			
			else if (connectionState.haveReceivedHandshake() && connectionState.haveReceivedBitfield()) {
				// Send Bitfield
				System.out.println("Building bitfield message to send to client.");
				BitSet bitfield = fileManager.getBitfield();
				int messageLength = 1 + bitfield.toByteArray().length;
				RegularMessage bitfieldMessage = new RegularMessage(messageLength, MessageType.bitfield, bitfield.toByteArray());

				return bitfieldMessage;
			}

			else if (connectionState.isNeedToUpdatePreferredNeighbors()) {
				// Send Choke / Unchoke
				connectionState.setNeedToUpdatePreferredNeighbors(false);
				boolean choked = connectionState.isChoked();
				if (choked) {
					System.out.println("Building choke message to send to client.");
					RegularMessage chokeMessage = new RegularMessage(1, 0, null);
					connectionState.setFileIndexToSend(-1);
					// Now wait for a request
					waiting = true;

					return chokeMessage;
				} else {
					System.out.println("Building unchoke message to send to client.");
					RegularMessage unchokeMessage = new RegularMessage(1, 1, null);

					return unchokeMessage;
				}
			}

			else if (connectionState.isNeedToUpdateOptimisticNeighbor()) {
				// Send Choke / Unchoke
				System.out.println("Building unchoke message to send to client.");
				connectionState.setNeedToUpdateOptimisticNeighbor(false);
				RegularMessage unchokeMessage = new RegularMessage(1, 1, null);
				// Now wait for a request
				waiting = true;

				return unchokeMessage;
			} 
			
			else if (connectionState.getFileIndexToSend() != -1){
				waiting = false;
				// Send Piece
				if (connectionState.isChoked()) {
					System.out.println("Building choke message to send to client.");
					RegularMessage chokeMessage = new RegularMessage(1, 0, null);

					return chokeMessage;
				} else {
					connectionState.setFileIndexToSend(-1);
					int messageLengthFTS = 1 + (fileManager.getFilePieceAtIndex(connectionState.getFileIndexToSend())).length;
					System.out.println("Building a piece message to send to client");
					return new RegularMessage(messageLengthFTS, MessageType.piece, fileManager.getFilePieceAtIndex(connectionState.getFileIndexToSend()));
				}

			} 

			else{
				System.out.println("Waiting for further implementation.");
				while(true){}
			}
		}

		public void run() {
			try{
				//initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				in = new ObjectInputStream(connection.getInputStream());
				
				try{
					
					// Most of the business logic of the server will happen in this while loop.
					while(true)
					{
						p2pProtocol.reset();

						if (waiting) {
							// Wait for the client messages to arrive.
							p2pProtocol.receiveMessage(in);

							Message messageToSend = getNextMessageToSend();

							System.out.println("Sending message to client: " + p2pProtocol.getConnectedPeerId());

							p2pProtocol.sendMessage(out, messageToSend);
						} else {
							Message messageToSend = getNextMessageToSend();

							p2pProtocol.sendMessage(out, messageToSend);


							p2pProtocol.receiveMessage(in);
						}
						System.out.println("End-Server------------------------------------------------------------------------\n\n\n");
					}
				}
				catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				} 
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + fileManager.getThisPeerIdentifier());
			}
			finally{
				//Close connections
				try{
					in.close();
					out.close();
					connection.close();
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Client " + fileManager.getThisPeerIdentifier());
				}
			}
		}
	}

}
