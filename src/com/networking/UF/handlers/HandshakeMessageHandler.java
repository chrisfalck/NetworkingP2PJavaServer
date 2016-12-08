package com.networking.UF.handlers;

import com.networking.UF.P2PProtocol;
import com.networking.UF.client.Client;
import com.networking.UF.messages.HandshakeMessage;
import com.networking.UF.messages.Message;
import com.networking.UF.server.ConnectionState;
import com.networking.UF.server.Server;

/**
 * Created by clayhausen on 10/19/16.
 */
public class HandshakeMessageHandler implements MessageHandler {
	Client myClient;
	Server myServer;
	P2PProtocol myProtocol;

	public HandshakeMessageHandler(Server server, P2PProtocol protocol) {
		this.myServer = server;
		this.myProtocol = protocol;
	}

	public HandshakeMessageHandler(Client client, P2PProtocol protocol) {
		this.myClient = client;
		this.myProtocol = protocol;
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getMessageType() != "handshake")
			return false;

		HandshakeMessage messageCast = (HandshakeMessage) message;

		// This is so non HandshakeMessage(s) have access to the connected peer
		// id.
		myProtocol.setConnectedPeerId(messageCast.getPeerId());

		// Determine what state to change depending on if we're a Client or
		// Server handler.
		if (myClient != null) {
			myClient.setHaveReceivedHandshake(true);
		} else if (myServer != null){
			ConnectionState connectionState = myServer.getConnectionState(messageCast.getPeerId());

			// If a connection state doesn't already exist for this peer id, create one.
			if (connectionState == null) {
				connectionState = new ConnectionState(messageCast.getPeerId());
				connectionState.setWaiting(true);
			}

			connectionState.setHaveReceivedHandshake(true);
			myServer.setConnectionState(messageCast.getPeerId(), connectionState);
		} else {
			System.err.println("The handler needs a valid server or client to edit state information.");
		}

		return true;
	}

	@Override
	public byte[] prepareMessageForSending(Message message) {
		return message.toByteArray();
	}
}
