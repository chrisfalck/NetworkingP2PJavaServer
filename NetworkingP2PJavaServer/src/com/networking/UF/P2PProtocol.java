package com.networking.UF;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.naming.directory.InitialDirContext;

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

	private MessageHandler chokeMessageHandler;
	private MessageHandler unchokeMessageHandler;
	private MessageHandler interestedMessageHandler;
	private MessageHandler uninterestedMessageHandler;
	private MessageHandler haveMessageHandler;
	private MessageHandler bitfieldMessageHandler;
	private MessageHandler requestMessageHandler;
	private MessageHandler pieceMessageHandler;
	private MessageHandler handshakeMessageHandler;
	
	private Client myClient;
	private Server myServer;
	
	public int connectedPeerId;
	
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
		this.chokeMessageHandler = new ChokeMessageHandler();
		this.unchokeMessageHandler = new UnchokeMessageHandler();
		this.interestedMessageHandler = new InterestedMessageHandler();
		this.uninterestedMessageHandler = new UninterestedMessageHandler();
		this.haveMessageHandler = new HaveMessageHandler();
		this.bitfieldMessageHandler = new BitfieldMessageHandler(server, connectedPeerId);
		this.requestMessageHandler = new RequestMessageHandler();
		this.pieceMessageHandler = new PieceMessageHandler();
		this.handshakeMessageHandler = new HandshakeMessageHandler(server, this);
		this.peerId = peerId;
		this.origin = protocolOrigin;
		this.myServer = server;
	}
	
	public P2PProtocol(int peerId, String protocolOrigin, Client client) {
		this.chokeMessageHandler = new ChokeMessageHandler();
		this.unchokeMessageHandler = new UnchokeMessageHandler();
		this.interestedMessageHandler = new InterestedMessageHandler();
		this.uninterestedMessageHandler = new UninterestedMessageHandler();
		this.haveMessageHandler = new HaveMessageHandler();
		this.bitfieldMessageHandler = new BitfieldMessageHandler(client);
		this.requestMessageHandler = new RequestMessageHandler();
		this.pieceMessageHandler = new PieceMessageHandler();
		this.handshakeMessageHandler = new HandshakeMessageHandler(client);
		this.peerId = peerId;
		this.origin = protocolOrigin;
		this.myClient = client;
	}


	/** Convert the received byte array into a Message object and pass to the appropriate handler. */
	public void receiveMessage(ObjectInputStream in) throws Exception {

		// Receive the raw byte array.
		rawReceivedMessage = (byte[])in.readObject();

		if (origin.equals("client")) {
			System.out.println("Received message from server: " + new String(Arrays.copyOfRange(rawReceivedMessage, 0, 28)) 
					+ Ints.fromByteArray((Arrays.copyOfRange(rawReceivedMessage, 28, 32))));
		} else {
			System.out.println("Received message from client: " + new String(Arrays.copyOfRange(rawReceivedMessage, 0, 28)) 
					+ Ints.fromByteArray((Arrays.copyOfRange(rawReceivedMessage, 28, 32))));
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
				out.writeObject(messageToSend.toByteArray());
			} else {
				// If rawReceivedMessage contains information, we are sending a message from a server object.
				out.writeObject(messageToSend.toByteArray());
			}

			out.flush();

		} catch(Exception exception){
			exception.printStackTrace();
			System.err.println(exception.toString());
		}
	}
	
	
	private void callAppropriateReceivingMessageHandler(byte[] rawReceivedMessage) {
		System.out.println("Handling message.");
		
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
			
			// Handle the message.
			this.handshakeMessageHandler.receiveMessage(handshakeMessage);
			return;
		} 
		
		// Parse out the regular message fields. 
		int messageLength = Ints.fromByteArray(Arrays.copyOfRange(rawReceivedMessage, 0, 4));
		byte[] messagePayload = Arrays.copyOfRange(rawReceivedMessage, 5, rawReceivedMessage.length);
		RegularMessage regularMessage = new RegularMessage(messageLength, 5, messagePayload);

		// Otherwise see what kind of message it is. 
		switch (regularHeader) {
		// Choke.
		case 0:

			break;

		// Unchoke.
		case 1:

			break;

		// Interested.
		case 2:

			break;

		// Not Interested.
		case 3:

			break;

		// Have.
		case 4:

			break;

		// Bitfield.
		case 5:
			this.bitfieldMessageHandler.receiveMessage(regularMessage);
			break;

		// Request.
		case 6:

			break;

		// Piece.
		case 7:

			break;

		default:
			System.err.println("Unrecognized message format: " + new String(rawReceivedMessage));
			reset();
			break;
		}
	}


}




