package com.networking.UF.handlers;

import com.networking.UF.client.Client;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;
import com.networking.UF.server.ConnectionState;
import com.networking.UF.server.Server;

/**
 * Created by clayhausen on 10/18/16.
 */
public class UninterestedMessageHandler implements MessageHandler {
	Server myServer;
	Client myClient; 
	int peerId;
	
	public UninterestedMessageHandler(Client client) {
		myClient = client;
	}
	
	public UninterestedMessageHandler(Server server, int peerId) {
		myServer = server;
		this.peerId = peerId;
	}
	
    public boolean receiveMessage(Message message) {
    	RegularMessage messageCast = (RegularMessage)message;
    	if (myClient != null) {
    		myClient.setInterested(false);
    	} else {
    		ConnectionState connectionState = myServer.getConnectionState(peerId);
    		connectionState.setInterested(false);
    		myServer.setConnectionState(peerId, connectionState);
    	}
        return false;
    }

    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
