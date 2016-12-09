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

	ConcurrentHashMap<Integer, ConnectionState> clientConnectionStates = new ConcurrentHashMap<Integer, ConnectionState>();

	public ConnectionState getClientConnectionState(Integer peerId) {
		return clientConnectionStates.get(peerId);
	}
	public synchronized void setClientConnectionState(Integer peerId, ConnectionState connectionState) {
		clientConnectionStates.put(peerId, connectionState);
		System.out.println("\nServer for peer " + fileManager.getThisPeerIdentifier() + 
				" updated connection state for peer " + peerId + ".\n" + connectionState.toString() + "\n");
	}
	
	public void setNumPreferredNeighbors(int numPreferredNeighbors) {
		this.numPreferredNeighbors = numPreferredNeighbors;
	}

	
	public Server(Peer myPeer) {
		this.myPeer = myPeer;
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

			Enumeration<Integer> connectionStateKeys = clientConnectionStates.keys();
			while(connectionStateKeys.hasMoreElements()) {
				peerIds.add(clientConnectionStates.get(connectionStateKeys.nextElement()).getPeerIdOfConnectedClient());
			}

			int randomPeerId = peerIds.get(generator.nextInt(peerIds.size()));
			while (true) {
				boolean isNotPreferredNeighbor = (clientConnectionStates.get(randomPeerId).clientIsChoked() == true);
				boolean isNotOwnPeer = (clientConnectionStates.get(randomPeerId).getPeerIdOfConnectedClient() != fileManager.getThisPeerIdentifier());
				if (isNotOwnPeer && isNotPreferredNeighbor) break;
				else {
					randomPeerId = generator.nextInt(ConfigParser.parsePeerInfoFile().getConfigLength());
				}
			}

			clientConnectionStates.get(randomPeerId).setClientIsOptimisticallyUnchoked(true);
			clientConnectionStates.get(randomPeerId).setNeedToUpdateOptimisticNeighbor(true);
			logger.logChangeOfOptimisticallyUnchokedNeighbor(clientConnectionStates.get(randomPeerId).getPeerIdOfConnectedClient());
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
//		System.out.println("Entered updatePreferredNeighbors() block");
		ConcurrentHashMap<Integer,ConnectionState> myServerConnectionStates = clientConnectionStates;
		Enumeration<Integer> peerIds = myServerConnectionStates.keys();
		ArrayList<PeerAndSpeed> unsortedPeers = new ArrayList<PeerAndSpeed>();
		ArrayList<PeerAndSpeed> sortedPeers = new ArrayList<PeerAndSpeed>();
		while(peerIds.hasMoreElements()) {
			int currentPeerId = peerIds.nextElement();
			ConnectionState currentConnectionState = myServerConnectionStates.get(currentPeerId);
			if (currentConnectionState.clientIsInterested() == true){
				unsortedPeers.add(new PeerAndSpeed(currentPeerId, 5));
			} else {
//				System.out.println("Skipping: " + currentPeerId + " " + currentConnectionState.getConnectionSpeed());
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
		
//		for (PeerAndSpeed pAndS: sortedPeers) {
//			System.out.println(pAndS.peerAndSpeedId);
//			System.out.println(pAndS.peerAndSpeedSpeed);
//		}

		try {
			if (numPreferredNeighbors == 0) {
				numPreferredNeighbors = ConfigParser.parseCommonFile().getNumberOfPreferredNeighbors();
			}
			List<Integer> preferredNeighbors = new ArrayList<Integer>();
			for (int i = 0; i < myServerConnectionStates.size(); ++i) {
				if (i < sortedPeers.size()) {
					if (i < numPreferredNeighbors) {
						clientConnectionStates.get(sortedPeers.get(i).peerAndSpeedId).setClientIsChoked(false);
						preferredNeighbors.add(sortedPeers.get(i).peerAndSpeedId);
					} else {
						clientConnectionStates.get(sortedPeers.get(i).peerAndSpeedId).setClientIsChoked(true);
					}
					System.out.println("Server is setting need to update preferred neighbors on peer " + sortedPeers.get(i).peerAndSpeedId);
					clientConnectionStates.get(sortedPeers.get(i).peerAndSpeedId).setNeedToUpdatePreferredNeighbors(true);
				}
			}
			if (preferredNeighbors.size() > 0) {
				logger.logChangeOfPreferredNeighbors(preferredNeighbors);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		
		public boolean shouldWaitForMessagesFromClient = true;
		public boolean shouldWaitForMessagesFromClient() {
			return shouldWaitForMessagesFromClient;
		}
		public void setShouldWaitForMessagesFromClient(boolean shouldWaitForMessagesFromClient) {
			this.shouldWaitForMessagesFromClient = shouldWaitForMessagesFromClient;
		}

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
			ConnectionState connectionState = myServer.getClientConnectionState(p2pProtocol.getConnectedPeerId());
			
			if (!connectionState.haveSentHandshakeToClient()) {
				System.out.println("Building handshake message to send to client.");

				connectionState.setHaveSentHandshakeToClient(true);
				myServer.setClientConnectionState(p2pProtocol.getConnectedPeerId(), connectionState);

				return new HandshakeMessage(fileManager.getThisPeerIdentifier());
			}
			
			else if (connectionState.haveSentHandshakeToClient() && !connectionState.haveSentBitfieldToClient()) {
				System.out.println("Building bitfield message to send to client.");
				
				connectionState.setHaveSentBitfieldToClient(true);
				myServer.setClientConnectionState(p2pProtocol.getConnectedPeerId(), connectionState);
				
				BitSet bitfield = fileManager.getBitfield();
				return new RegularMessage(1 + bitfield.toByteArray().length, MessageType.bitfield, bitfield.toByteArray());
			} 
			
			else if (connectionState.needToRespondToClientInterestedStatus()) {
				connectionState.setNeedToRespondToClientInterestedStatus(false);
				myServer.setClientConnectionState(p2pProtocol.getConnectedPeerId(), connectionState);
				
				if (connectionState.clientIsChoked() || !connectionState.clientIsInterested()) {
					System.out.println("Building choke message to send to client.");
					return new RegularMessage(1, MessageType.choke, null);
				} else {
					System.out.println("Building unchoke message to send to client.");
					return new RegularMessage(1, MessageType.unchoke, null);
				}
			}
			
			else if (connectionState.needToRespondToClientRequestForPiece()) {
				System.out.println("Building a piece message to send to client");
				connectionState.setNeedToRespondToClientRequestForPiece(false);
				myServer.setClientConnectionState(p2pProtocol.getConnectedPeerId(), connectionState);

				int fileIndex = connectionState.getFileIndexToSendToClient();
				connectionState.setFileIndexToSendToClient(-1);

				int messageLengthFTS = 1 + (fileManager.getFilePieceAtIndex(fileIndex)).length;
				return new RegularMessage(messageLengthFTS, MessageType.piece, fileManager.getFilePieceAtIndex(fileIndex));			
			}
			
//			// If we have a file index we should send in response to a request message. 
//			else if (connectionState.getFileIndexToSendToClient() != -1) {
//				
//				// If the client asked us for a piece but was choked before we replied. 
//				if (connectionState.clientIsChoked()) {
//					System.out.println("Building choke message to send to client.");
//					return new RegularMessage(1, MessageType.choke, null);
//				} 
//				// We received a request message and the client is not choked. 
//				else {
//					int fileIndex = connectionState.getFileIndexToSendToClient();
//					connectionState.setFileIndexToSendToClient(-1);
//					int messageLengthFTS = 1 + (fileManager.getFilePieceAtIndex(fileIndex)).length;
//					System.out.println("Preparing a piece message to send to client");
//					return new RegularMessage(messageLengthFTS, MessageType.piece, fileManager.getFilePieceAtIndex(fileIndex));
//				}
//			}
//
//			// Determine whether we should send a choke or unchoke message based on who 
//			// the new preferred neighbors are. 
//			else if (connectionState.needToUpdatePreferredNeighbors()) {
//				connectionState.setNeedToUpdatePreferredNeighbors(false);
//
//				boolean choked = connectionState.isChoked();
//
//				if (choked) {
//					System.out.println("Building choke message to send to client with state:\n" + connectionState.toString());
//
//					RegularMessage chokeMessage = new RegularMessage(1, MessageType.choke, null);
//
//					connectionState.setFileIndexToSend(-1);
//
//					// The server should no longer be waiting for messsages from this client. 
//					connectionState.setWaiting(false);
//
//					return chokeMessage;
//				} else {
//					// The server should now wait for request messages from this client. 
//					connectionState.setWaiting(true);
//					System.out.println("Building unchoke message to send to client with state:\n" + connectionState.toString());
//					RegularMessage unchokeMessage = new RegularMessage(1, MessageType.unchoke, null);
//
//					return unchokeMessage;
//				}
//			}
//
//			else if (connectionState.needToUpdateOptimisticNeighbor()) {
//				// Send Choke / Unchoke
//				System.out.println("Building unchoke message to send to client with state:\n" + connectionState.toString());
//				connectionState.setNeedToUpdateOptimisticNeighbor(false);
//				RegularMessage unchokeMessage = new RegularMessage(1, MessageType.unchoke, null);
//				// Now wait for a request
//				connectionState.setWaiting(true);
//
//				return unchokeMessage;
//			} 
			
			// If we received a message and are not in a state to send anything back, return null.
			else {
				System.out.println("Waiting for further implementation.");
				System.out.println("Final client state:");
				System.out.println(connectionState.toString());
				while(true) {}
			}
		}
		
		private void serverShouldBeWaitingForMessages() {
			ConnectionState stateOfConnectionToClient = myServer.getClientConnectionState(p2pProtocol.getConnectedPeerId());
			if (stateOfConnectionToClient == null) {
				
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
						ConnectionState connectionState = myServer.getClientConnectionState(p2pProtocol.getConnectedPeerId());
						p2pProtocol.reset();
						
						p2pProtocol.receiveMessage(in);
						Message messageToSend = getNextMessageToSend();
						p2pProtocol.sendMessage(out, messageToSend);
//						
//						if (connectionState == null || connectionState.isWaiting()) {
//							System.out.println("Inside the server waiting block.");
//							
//							p2pProtocol.receiveMessage(in);
//
//							Message messageToSend = getNextMessageToSend();
//							if (messageToSend != null) {
//								System.out.println("Sending message to client: " + p2pProtocol.getConnectedPeerId());
//								p2pProtocol.sendMessage(out, messageToSend);
//							}
//						} else {
//							System.out.println("Inside the server sending block.");
//
//							// Wait until (We need to send choke or unchoke messages) OR (We receive a have message).
//							System.out.println("Beginning the in.available() wait loop.");
//							while (in.available() == 0) {
//								if (connectionState.needToUpdateOptimisticNeighbor() || connectionState.needToUpdatePreferredNeighbors()) break;
//							}
//							System.out.println("Exiting the in.available() wait loop.");
//							
//							if (in.available() == 0) {
//								System.out.println("Broke out of message existence checking to send unchoke message.");
//								Message messageToSend = getNextMessageToSend();
//								p2pProtocol.sendMessage(out, messageToSend);
//							} else {
//								System.out.println("Broke out of message existence checking to accept have message.");
//								p2pProtocol.receiveMessage(in);
//								Message messageToSend = getNextMessageToSend();
//								if (messageToSend != null) p2pProtocol.sendMessage(out, messageToSend);
//							}
//
//						}
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
