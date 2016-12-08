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
import com.networking.UF.server.ConnectionState;

public class Client implements Runnable {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
	ObjectInputStream in;          //stream read from the socket
	static FileManager fileManager = FileManager.getInstance();
	static Logger logger = Logger.getInstance();
	private P2PProtocol p2pProtocol = new P2PProtocol(fileManager.getThisPeerIdentifier(), "client", this);
	private String serverAddress;
	private int portNumber;
	private int serverPeerId;
	Peer myPeer;
	private boolean shouldSendHaveMessage = false;
	private byte[] currentHaveMessageIndexToSend;
	boolean bitfieldReceived = false;
	
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

	// Track this client's state.
	private ConnectionState connectionState;

	/**
	 * Return this client's server
	 * @return the peerID of the server this client is connected to
	 */
	public int getServerPeerId() {
		return this.serverPeerId;
	}

	/**
	 * Return this client's ConnectionState
	 * @return the ConnectionState of this client
	 */
	public ConnectionState getConnectionState() {
		return this.connectionState;
	}

	public void setConnectionState(ConnectionState connectionState) {
		this.connectionState = connectionState;
	}
	
	public boolean haveReceivedHandshake() {
		return this.connectionState.haveReceivedHandshake();
	}
	
	public void setInterested(boolean interested) {
		this.connectionState.setInterested(interested);;
	}
	
	// Other fields on the client ConnectionState object are for the client itself.
	// The bitfield on the client ConnectionState object referrs to the Server. 
	public void setServerBitfield(BitSet serverBitset) {
		connectionState.setBitfield(serverBitset);
	}
	
	public void setHaveReceivedBitfield(boolean haveReceivedBitfield) {
		this.connectionState.setHaveReceivedBitfield(haveReceivedBitfield);
	}

	public void setHaveReceivedHandshake(boolean haveReceivedHandshake) {
		this.connectionState.setHaveReceivedHandshake(haveReceivedHandshake);
	}
	
	public void setHasReceivedPiece(boolean haveReceivedPiece){
		this.connectionState.setHasReceivedPiece(haveReceivedPiece);
	}
	
	public void setChoked(boolean isChoked){
		this.connectionState.setChoked(isChoked);
	}

	public void setDownloadSpeed(long downloadSpeed) {
		this.connectionState.setConnectionSpeed(downloadSpeed);
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
		this.connectionState = new ConnectionState(fileManager.getThisPeerIdentifier());
		this.myPeer = myPeer;
	}
	
	/**
	 * Determines the next message the client should send by analysing the client's state variables. 
	 * Returns an appropriate built message to be sent. 
	 * @return
	 * @throws InterruptedException
	 */
	private Message getNextMessageToSend() throws InterruptedException {
		System.out.println("Client " + fileManager.getThisPeerIdentifier() + " waiting status: " + connectionState.isWaiting());
		if (!connectionState.haveReceivedHandshake()) {
			// Send Handshake
			System.out.println("Building handshake message to send to server.");
			return new HandshakeMessage(fileManager.getThisPeerIdentifier());

		} 
		
		else if (connectionState.haveReceivedHandshake() && !connectionState.haveReceivedBitfield()) {
			// Send Bitfield
			System.out.println("Building bitfield message to send to server.");
			BitSet bitfield = fileManager.getBitfield();
			int messageLength = 1 + bitfield.size();
			RegularMessage bitfieldMessage = new RegularMessage(messageLength, MessageType.bitfield, bitfield.toByteArray());
			return bitfieldMessage;

		} 
		
		else if (connectionState.haveReceivedHandshake() && connectionState.haveReceivedBitfield() && !bitfieldReceived) {
			// Send interested / not interested
			// Wait for unchoked message
			connectionState.setWaiting(true);
			int indexOfMissingPiece = BitfieldUtils.compareBitfields(fileManager.getBitfield(), connectionState.getBitfield());
			bitfieldReceived = true;
			if (indexOfMissingPiece != -1) {
				// We want a piece from the Server.
				System.out.println("Client " + fileManager.getThisPeerIdentifier() + " just got the bitfield and is sending interested message.");
				return new RegularMessage(1, MessageType.interested, null);
			} else {
				System.out.println("Client " + fileManager.getThisPeerIdentifier() + " just got the bitfield and is sending not interested message.");
				return new RegularMessage(1, MessageType.notInterested, null);
			}
		} 
		
		else if (connectionState.getHasReceivedPiece() == true) {
			// Let Peer know so it can broadcast its updated bitmap
			System.out.println("Peer has received piece...client preparing to send Have message.");
			myPeer.broadcastShouldSendHaveMessages(currentHaveMessageIndexToSend);
			shouldSendHaveMessage = false;
			connectionState.setHasReceivedPiece(false);
			return new RegularMessage(1 + currentHaveMessageIndexToSend.length, MessageType.have, currentHaveMessageIndexToSend);
		} 
		
		else if (shouldSendHaveMessage) {
			// broadcast the updated bitmap
			shouldSendHaveMessage = false;
			return new RegularMessage(1 + currentHaveMessageIndexToSend.length, MessageType.have, currentHaveMessageIndexToSend);
		} 
		
		else if (/*connectionState.isInterested() == true &&*/connectionState.isChoked() == false || connectionState.isOptimisticallyUnchoked() == true) {
			// Send request messages until choked
			connectionState.setWaiting(false);
			int indexOfMissingPiece = BitfieldUtils.compareBitfields(fileManager.getBitfield(), connectionState.getBitfield());
			System.out.println("index of missing piece is: " + indexOfMissingPiece);
			if (indexOfMissingPiece == -1) {
				connectionState.setInterested(false);
				System.out.println("Not interested in any file pieces from " + this.serverAddress);
				return new RegularMessage(1, MessageType.notInterested, null);
			} else {
				System.out.println("Interested in a file piece from " + this.serverAddress);
				return new RegularMessage(1 + 4, MessageType.request, Ints.toByteArray(indexOfMissingPiece));
			}
		} 
		
		else if (connectionState.isChoked() == true && connectionState.isOptimisticallyUnchoked() == false) {
			// Wait until unchoked to send more request messages
			System.out.println("Client " + fileManager.getThisPeerIdentifier() + " is choked and waiting to be unchoked");
			connectionState.setWaiting(true);
		}
		return null;
	}

	public void run()
	{
		try{
			
			// Time for all servers to start before clients start sending messages. 
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

				if (this.connectionState != null && this.connectionState.isWaiting()) {

					System.out.println("Client " + fileManager.getThisPeerIdentifier() + "is interested in " + getServerPeerId() + ": " + connectionState.isInterested());
					System.out.println("Client " + fileManager.getThisPeerIdentifier() + "is choked by " + getServerPeerId() + ": " + connectionState.isChoked());
					p2pProtocol.receiveMessage(in);

					Message messageToSend = getNextMessageToSend();


					p2pProtocol.sendMessage(out, messageToSend);

				} else {

					Message messageToSend = getNextMessageToSend();
					System.out.println("Sending message to server peer " + this.serverPeerId + " from client " + fileManager.getThisPeerIdentifier() + "\n");

					if (messageToSend != null) {
						p2pProtocol.sendMessage(out, messageToSend);
						p2pProtocol.receiveMessage(in);
					} 
				}

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
