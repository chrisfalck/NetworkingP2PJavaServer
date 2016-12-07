package com.networking.UF;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.naming.directory.InitialDirContext;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.networking.UF.client.Client;
import com.networking.UF.handlers.BitfieldMessageHandler;
import com.networking.UF.handlers.ChokeMessageHandler;
import com.networking.UF.handlers.HandshakeMessageHandler;
import com.networking.UF.handlers.HaveMessageHandler;
import com.networking.UF.handlers.InterestedMessageHandler;
import com.networking.UF.handlers.MessageHandler;
import com.networking.UF.handlers.PieceMessageHandler;
import com.networking.UF.handlers.RequestMessageHandler;
import com.networking.UF.handlers.UnchokeMessageHandler;
import com.networking.UF.handlers.UninterestedMessageHandler;
import com.networking.UF.messages.HandshakeMessage;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;
import com.networking.UF.server.Server;

/**
 * Each P2PProtocol object will belong to either a client or server object and will contain unique state. 
 * The purpose of the P2PProtocol is to convert raw messages into Message objects for receiving and 
 * to convert Message objects into raw messages for sending. 
 * To achieve this, the P2PProtocol object uses various Message Handlers.
 * @author Chris
 *
 */
public class P2PProtocol implements Protocol {
	
	private Client myClient;
	private Server myServer;
	
	// The peer on the other end of the TCP connection. 
	private int connectedPeerId;

	public int getConnectedPeerId() {
		return connectedPeerId;
	}
	
	public void setConnectedPeerId(int connectedPeerId) {
		this.connectedPeerId = connectedPeerId;
	}
	
	// Have we already made a handshake connection for this protocol instance. 
	private boolean firstHandshake = true;
	
	// The peer invoking this protocol instance.
	private int peerId;
	
	// Whether this protocol instance originated from a client or server.
	private String origin;

	// The raw message we receive and then convert into a Message to be handled.
	private byte[] rawReceivedMessage = null;
	
	private static Logger logger = Logger.getInstance();

	public P2PProtocol(int peerId, String protocolOrigin, Server server) {
		this.peerId = peerId;
		this.origin = protocolOrigin;
		this.myServer = server;
	}
	
	public P2PProtocol(int peerId, String protocolOrigin, Client client) {
		this.peerId = peerId;
		this.origin = protocolOrigin;
		this.myClient = client;
	}


	/** Convert the received byte array into a Message object and pass to the appropriate handler. */
	public void receiveMessage(ObjectInputStream in) throws Exception {

		// Receive the raw byte array and determine the download rate
		Long startTime = System.nanoTime();
		rawReceivedMessage = (byte[])in.readObject();
		Long endTime = System.nanoTime();
		if (myClient != null) {
			myClient.setDownloadSpeed(rawReceivedMessage.length / (endTime - startTime)); // bytes / nanosecond
		}

		
		if (origin.equals("server")) System.out.println("\n\n\nStart-Server----------------------------------------------------------------------");

		if (origin.equals("client")) {

			System.out.println("Received message from server: " + connectedPeerId);
		} 
		else {
			System.out.println("Received message from client: " + connectedPeerId);
		}

		// Create an appropriate message type from the raw array and call it's handler.
		callAppropriateReceivingMessageHandler(rawReceivedMessage);
	}

	/** Reset the p2p protocol state in between messages. */
	public void reset() {
		rawReceivedMessage = null;
	}


	/** Convert the received Message object into a byte array and return it to be sent. */
	public void sendMessage(ObjectOutputStream out, Message messageToSend) throws Exception {
		try{

			if (origin.equals("client")) {
				// If the rawReceivedMessage is null, we are sending a message from a client object.
				System.out.println("Sending message to server: " + connectedPeerId);
				out.writeObject(messageToSend.toByteArray());
			} else {
				// If rawReceivedMessage contains information, we are sending a message from a server object.
				System.out.println("Sending message to server: " + connectedPeerId);
				out.writeObject(messageToSend.toByteArray());
			}

			out.flush();

		} catch(Exception exception){
			exception.printStackTrace();
			System.err.println(exception.toString());
		}
	}
	
	
	private void callAppropriateReceivingMessageHandler(byte[] rawReceivedMessage) {
		// Will contain "P2PFILESHARINGPROJ" if we're receiving a handshake message.
		String handshakeHeader = new String(Arrays.copyOfRange(rawReceivedMessage, 0, 18));
		
		// Otherwise, the 5th byte of the message will be an integer indicating message type.
		int regularHeader = (int)rawReceivedMessage[4];
		
		// Check if this is a handshake message.
		if (handshakeHeader.equals("P2PFILESHARINGPROJ")) {

			// Pull the required data from the bytes array.
			byte[] peerIdBytes = Arrays.copyOfRange(rawReceivedMessage, rawReceivedMessage.length - 4, rawReceivedMessage.length);
			int peerIdFromMessage = Ints.fromByteArray(peerIdBytes);
			
			if (this.firstHandshake == true) {

				// Don't want to log client TCP connections twice (they already log an outgoing connection).
				if (!this.origin.equals("client")) {
					logger.logTCPCreationEvent(peerIdFromMessage, "incoming");
				}
				this.firstHandshake = false;
			}
			
			// Build the message.
			HandshakeMessage handshakeMessage = new HandshakeMessage(peerIdFromMessage);
			HandshakeMessageHandler handshakeMessageHandler;
			if (myClient != null) {
				handshakeMessageHandler = new HandshakeMessageHandler(myClient, this);
			} else {
				handshakeMessageHandler = new HandshakeMessageHandler(myServer, this);
			}
			
			// Handle the message.
			handshakeMessageHandler.receiveMessage(handshakeMessage);
			return;
		} 
		
		// Parse out the regular message fields. 
		int messageLength = Ints.fromByteArray(Arrays.copyOfRange(rawReceivedMessage, 0, 4));
		byte[] messagePayload = Arrays.copyOfRange(rawReceivedMessage, regularHeader, rawReceivedMessage.length);
		RegularMessage regularMessage = new RegularMessage(messageLength, regularHeader, messagePayload);

		// Otherwise see what kind of message it is. 
		switch (regularHeader) {
		// Choke.
		case 0:
			ChokeMessageHandler chokeMessageHandler;

			chokeMessageHandler = new ChokeMessageHandler(myClient);

			chokeMessageHandler.receiveMessage(regularMessage);

			break;

		// Unchoke.
		case 1:
			UnchokeMessageHandler unchokeMessageHandler;

			unchokeMessageHandler = new UnchokeMessageHandler(myClient);

			unchokeMessageHandler.receiveMessage(regularMessage);

			break;

		// Interested.
		case 2:

			InterestedMessageHandler interestedMessageHandler;
			if (myClient != null) {
				interestedMessageHandler = new InterestedMessageHandler(myClient);
			} else {
				interestedMessageHandler = new InterestedMessageHandler(myServer, connectedPeerId);
			}
			interestedMessageHandler.receiveMessage(regularMessage);

			break;

		// Not Interested.
		case 3:
			
			UninterestedMessageHandler uninterestedMessageHandler;
			if (myClient != null) {
				uninterestedMessageHandler = new UninterestedMessageHandler(myClient);
			} else {
				uninterestedMessageHandler = new UninterestedMessageHandler(myServer, connectedPeerId);
			}
			uninterestedMessageHandler.receiveMessage(regularMessage);

			break;

		// Have.
		case 4:

			break;

		// Bitfield.
		case 5:
			BitfieldMessageHandler bitfieldMessageHandler;
			if (myClient != null) {
				bitfieldMessageHandler = new BitfieldMessageHandler(myClient);
			} else {
				bitfieldMessageHandler = new BitfieldMessageHandler(myServer, connectedPeerId);
			}
			bitfieldMessageHandler.receiveMessage(regularMessage);
			break;

		// Request.
		case 6:
			RequestMessageHandler requestMessageHandler;
			
			requestMessageHandler = new RequestMessageHandler(myServer, connectedPeerId);
			
			requestMessageHandler.receiveMessage(regularMessage);
			break;

		// Piece.
		case 7:
			HaveMessageHandler haveMessageHandler;

			haveMessageHandler = new HaveMessageHandler(myServer, connectedPeerId);

			haveMessageHandler.receiveMessage(regularMessage);

			break;

		default:
			System.err.println("Unrecognized message format: " + new String(rawReceivedMessage));
			reset();
			break;
		}
	}


}




