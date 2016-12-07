package com.networking.UF.handlers;

import com.networking.UF.FileManager;
import com.networking.UF.Logger;
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
	Logger logger = Logger.getInstance();
	
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
    		System.out.println("Client " + FileManager.getInstance().getThisPeerIdentifier() + " received interested message.");
    		myClient.setInterested(false);
    	} else {
			System.out.println("Waiting for further implementation.");
    		while(true){}
    		System.out.println("Peer " + FileManager.getInstance().getThisPeerIdentifier() + " received interested message from " + peerId);
    		ConnectionState connectionState = myServer.getConnectionState(peerId);
    		connectionState.setInterested(false);
    		myServer.setConnectionState(peerId, connectionState);
			logger.logReceiptOfNotInterestedMessage(peerId);
    	}
        return false;
    }

    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
