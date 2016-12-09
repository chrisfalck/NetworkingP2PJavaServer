package com.networking.UF.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.TimeUnit;

import com.google.common.primitives.Ints;
import com.networking.UF.BitfieldUtils;
import com.networking.UF.FileManager;
import com.networking.UF.Logger;
import com.networking.UF.MessageType;
import com.networking.UF.P2PProtocol;
import com.networking.UF.Peer;
import com.networking.UF.messages.HandshakeMessage;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;

public class Client implements Runnable {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
	ObjectInputStream in;          //stream read from the socket
	static FileManager fileManager = FileManager.getInstance();
	static Logger logger = Logger.getInstance();
	private String serverAddress;
	private int portNumber;
	Peer myPeer;
	
	// Return the peer id of the server we're connected to. 
	private int serverPeerId;
	
	// Initialize a p2p protocol object with a reference to this client object and our peer id. 
	private P2PProtocol p2pProtocol = new P2PProtocol(fileManager.getThisPeerIdentifier(), "client", this);

	// Whether or not we established a connection with the Server. 
	private boolean haveReceivedHandshake = false;
	
	// Whether or note we've received a bit field message. 
	private boolean haveReceivedBitfield = false;

	// The Have message index to send in a Have message to the server (set after receiving a file piece). 
	private byte[] currentHaveMessageIndexToSend = new byte[0];

	// Whether or not we need to send a Have message (set after receiving a file piece). 
	private boolean shouldSendHaveMessage = false;

	// Whether or not we are interested in the server.
	private boolean interested = false;

	// True if we've received a piece message and haven't yet dealt with it.
	private boolean haveReceivedPiece = false;

	// Whether the server has choked us or not.
	private boolean isChoked = false;

	// The download speed of pieces from this server.
	private long downloadSpeed = 0;
	
	// Whether or not we should be waiting for a message from the server.
	private boolean shouldWaitForMessage = false;
	
	// Whether we've received a piece message.
	private boolean shouldDealWithPieceMessage = false;
	
	public boolean shouldDealWithPieceMessage() {
		return shouldDealWithPieceMessage;
	}
	public void setShouldDealWithPieceMessage(boolean shouldDealWithPieceMessage) {
		this.shouldDealWithPieceMessage = shouldDealWithPieceMessage;
	}

	private BitSet bitfieldOfServer = new BitSet();

	public BitSet getBitfieldOfServer() {
		return bitfieldOfServer;
	}
	public void setBitfieldOfServer(BitSet bitfieldOfServer) {
		this.bitfieldOfServer = bitfieldOfServer;
	}
	public boolean shouldWaitForMessage() {
		return shouldWaitForMessage;
	}
	public void setShouldWaitForMessage(boolean shouldWaitForMessage) {
		this.shouldWaitForMessage = shouldWaitForMessage;
	}
	public byte[] getCurrentHaveMessageIndexToSend() {
		return currentHaveMessageIndexToSend;
	}
	public void setCurrentHaveMessageIndexToSend(byte[] currentHaveMessageIndexToSend) {
		this.currentHaveMessageIndexToSend = currentHaveMessageIndexToSend;
	}
	public boolean isShouldSendHaveMessage() {
		return shouldSendHaveMessage;
	}
	public void setShouldSendHaveMessage(boolean shouldSendHaveMessage) {
		this.shouldSendHaveMessage = shouldSendHaveMessage;
	}
	public int getServerPeerId() {
		return this.serverPeerId;
	}
	public boolean haveReceivedHandshake() {
		return this.haveReceivedHandshake;
	}
	public void setHaveReceivedHandshake(boolean haveReceivedHandshake) {
		this.haveReceivedHandshake = haveReceivedHandshake;
	}
	public boolean isInterested() {
		return interested;
	}
	public void setInterested(boolean interested) {
		this.interested = interested;
	}
	public boolean haveReceivedBitfield() {
		return this.haveReceivedBitfield;
	}
	public void setHaveReceivedBitfield(boolean haveReceivedBitfield) {
		this.haveReceivedBitfield = haveReceivedBitfield;
	}
	public void setHasReceivedPiece(boolean haveReceivedPiece){
		this.haveReceivedPiece = haveReceivedPiece;
	}
	public boolean isChoked() {
		return isChoked;
	}
	public void setChoked(boolean isChoked){
		this.isChoked = isChoked;
	}
	public void setDownloadSpeedFromServer(long downloadSpeed) {
		this.downloadSpeed = downloadSpeed;
	}

	/**
	 * Steps for to follow P2P Protocol.
	 * Establish a TCP connection with the server and log its creation.
	 * Send a handshake message.
	 * Handle the handshake response. 
	 * Send a bitfield message.
	 * 		The bitfield message payload is a bit array where each bit represents whether this peer has the corresponding file piece or not. 
	 * Start sending regular messages.
	 *   
	 * 
	 * @param serverAddress
	 * @param portNumber
	 * @param serverPeerId
	 */
	public Client(String serverAddress, int portNumber, int serverPeerId, Peer myPeer) {
		this.serverAddress = serverAddress;
		this.portNumber = portNumber;
		this.serverPeerId = serverPeerId;
		this.myPeer = myPeer;
	}
	
	
	/**
	 * Determines the next message the client should send by analysing the client's state variables. 
	 * Returns an appropriate built message to be sent. 
	 * @return
	 * @throws InterruptedException
	 */
	private Message getNextMessageToSend() throws InterruptedException {
		if (!haveReceivedHandshake()) {
			System.out.println("Building handshake message to send to server.");
			return new HandshakeMessage(fileManager.getThisPeerIdentifier());
		} 
		
		else if (haveReceivedHandshake() && !haveReceivedBitfield()) {
			System.out.println("Building bitfield message to send to server.");
			BitSet bitfield = fileManager.getBitfield();
			return new RegularMessage(1 + bitfield.size(), MessageType.bitfield, bitfield.toByteArray());
		} 
		
		// Decide if we should send another interested message or now send an uninterested message.
		else if (shouldDealWithPieceMessage()) {
			System.out.println("Dealing with receipt of piece message from server.");
			System.out.println("New length of filePieces: " + fileManager.getLengthOfFilePieces());
			if (fileManager.getLengthOfFilePieces() == fileManager.getCapacityOfFilePieces()){
				fileManager.reconstructFile();
				while(true){}
			}
			setShouldDealWithPieceMessage(false);

//			int indexOfMissingPiece = BitfieldUtils.compareBitfields(fileManager.getBitfield(), getBitfieldOfServer());
			int indexOfMissingPiece = fileManager.getRandomFilePieceNeeded();
			// If the server has pieces we want, we send an interested message.
			if (indexOfMissingPiece != -1) {
				System.out.println("Client " + fileManager.getThisPeerIdentifier() + " is sending an interested message");
				return new RegularMessage(1, MessageType.interested, null);
			} 
			// Otherwise,  we send a not interested message.
			else {
				System.out.println("Client " + fileManager.getThisPeerIdentifier() + " is sending a not interested message.");
				return new RegularMessage(1, MessageType.notInterested, null);
			}
		}
		
		// If we aren't choked and we are interested, send a request message for a file piece. 
		else if (!isChoked() && isInterested()) {
			int indexOfMissingPiece = BitfieldUtils.compareBitfields(fileManager.getBitfield(), getBitfieldOfServer());
			System.out.println("Building request message to send to server for piece: " + indexOfMissingPiece);
			return new RegularMessage(1 + 4, MessageType.request, Ints.toByteArray(indexOfMissingPiece));
		}
		
		// Send either interested or uninterested messages to the server.
		else if (haveReceivedHandshake() && haveReceivedBitfield()) {
			// Determine if the server has a bitfiled with pieces not in our bitfield. 
			int indexOfMissingPiece = BitfieldUtils.compareBitfields(fileManager.getBitfield(), getBitfieldOfServer());
			System.out.println("Index of missing piece: " + indexOfMissingPiece + "\nFrom bitfield size: " + getBitfieldOfServer().size());
			System.out.println("Client is choked: " + isChoked() + "\nClient is interested in server: " + isInterested());
			TimeUnit.SECONDS.sleep(10);
			// If the server has pieces we want, we send an interested message.
			if (indexOfMissingPiece != -1) {
				System.out.println("Client " + fileManager.getThisPeerIdentifier() + " is sending an interested message");
				setInterested(true);
				return new RegularMessage(1, MessageType.interested, null);
			} 
			// Otherwise,  we send a not interested message.
			else {
				System.out.println("Client " + fileManager.getThisPeerIdentifier() + " is sending a not interested message.");
				setInterested(false);
				return new RegularMessage(1, MessageType.notInterested, null);
			}
		} 
		
//		// This spcific client received a full piece, so we should tell the Peer the tell all other clients to send have messages. 
//		else if (connectionState.hasReceivedPiece() == true) {
//			System.out.println("Peer has received piece...client preparing to send Have message.");
//			myPeer.broadcastShouldSendHaveMessages(currentHaveMessageIndexToSend);
//			shouldSendHaveMessage = false;
//			connectionState.setHasReceivedPiece(false);
//			return new RegularMessage(1 + currentHaveMessageIndexToSend.length, MessageType.have, currentHaveMessageIndexToSend);
//		} 
//		
//		// A have message was received by some Client on this Peer so we should also send a have message. 
//		else if (shouldSendHaveMessage) {
//			shouldSendHaveMessage = false;
//			return new RegularMessage(1 + currentHaveMessageIndexToSend.length, MessageType.have, currentHaveMessageIndexToSend);
//		} 
//		
//		else if (/*connectionState.isInterested() == true &&*/connectionState.isChoked() == false || connectionState.isOptimisticallyUnchoked() == true) {
//			// Send request messages until choked
//			int indexOfMissingPiece = BitfieldUtils.compareBitfields(fileManager.getBitfield(), connectionState.getBitfield());
//			System.out.println("index of missing piece is: " + indexOfMissingPiece);
//			if (indexOfMissingPiece == -1) {
//				connectionState.setInterested(false);
//				System.out.println("Not interested in any file pieces from " + this.serverAddress);
//				return new RegularMessage(1, MessageType.notInterested, null);
//			} else {
//				System.out.println("Interested in a file piece from " + this.serverAddress);
//				return new RegularMessage(1 + 4, MessageType.request, Ints.toByteArray(indexOfMissingPiece));
//			}
//		} 
//		
//		else if (connectionState.isChoked() == true && connectionState.isOptimisticallyUnchoked() == false) {
//			// Wait until unchoked to send more request messages
//			System.out.println("Client " + fileManager.getThisPeerIdentifier() + " is choked and waiting to be unchoked");
//		}
		else {
			System.out.println("Waiting for further implementation.");
			while(true) {}
		}
	}
	
//	private void clientShouldBeWaitingForMessages() {
//
//		if (!connectionState.haveReceivedHandshake()) {
//			connectionState.setWaiting(false);
//		}
//
//		else if (connectionState.haveReceivedHandshake() && !connectionState.haveReceivedBitfield()) {
//			connectionState.setWaiting(false);
//		}
//
//		else if (connectionState.haveReceivedHandshake() && connectionState.haveReceivedBitfield() && !haveHandledBitfieldMessage) {
//			connectionState.setWaiting(false);
//		}
//		
//		else if (connectionState.hasReceivedPiece() == true) {
//			connectionState.setWaiting(false);
//		}
//		
//		else if (shouldSendHaveMessage) {
//			connectionState.setWaiting(false);
//		}
//		
//		else if (/*connectionState.isInterested() == true &&*/connectionState.isChoked() == false || connectionState.isOptimisticallyUnchoked() == false) {
//			connectionState.setWaiting(true);
//		}
//
//		else if (connectionState.isChoked() == true && connectionState.isOptimisticallyUnchoked() == true) {
//			connectionState.setWaiting(false);
//		}
//
//		else 
//			connectionState.setWaiting(false);
//	}

	public void run()
	{
		try{
			
			// Time for all servers to start before clients start sending initial messages. 
			TimeUnit.SECONDS.sleep(5);

			// Create a socket to connect to the server.
			System.out.println("Client from peer " + fileManager.getThisPeerIdentifier() + " connecting to " + this.serverAddress + " on port " + this.portNumber);
			requestSocket = new Socket(this.serverAddress, this.portNumber);
			System.out.println("Client from peer " + fileManager.getThisPeerIdentifier() + " connected to " + this.serverAddress + " on port " + this.portNumber);
			logger.logTCPCreationEvent(this.serverPeerId, "outgoing");

			// Initialize inputStream and outputStream.
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			while(true)
			{
				System.out.println("\n\n\nStart-Client----------------------------------------------------------------------");
				p2pProtocol.reset();
				
				Message messageToSend = getNextMessageToSend();
				
				p2pProtocol.sendMessage(out, messageToSend);
				p2pProtocol.receiveMessage(in);

//				// If we haven't completed a handshake or should be waiting for messages. 
//				if (this.connectionState != null && this.connectionState.isWaiting()) {
//					System.out.println("Client is in waiting state.");
//
//					p2pProtocol.receiveMessage(in);
//
//					Message messageToSend = getNextMessageToSend();
//
//
//					p2pProtocol.sendMessage(out, messageToSend);
//
//				} 
//				
//				// We have completed a handshake and should not be waiting for messages.
//				else {
//					System.out.println("Client is in sending state.");
//
//					Message messageToSend = getNextMessageToSend();
//					System.out.println("Sending message to server peer " + this.serverPeerId + " from client " + fileManager.getThisPeerIdentifier() + "\n");
//
//					if (messageToSend != null) {
//						p2pProtocol.sendMessage(out, messageToSend);
//						p2pProtocol.receiveMessage(in);
//					} 
//				}

				System.out.println("End-Client----------------------------------------------------------------------------\n\n\n");
			}
		}
		catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				System.err.println(ioException.toString());
				ioException.printStackTrace();
			}
		}
	}

}
