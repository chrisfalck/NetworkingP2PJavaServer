package com.networking.UF.handlers;

import java.util.concurrent.TimeUnit;

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
public class InterestedMessageHandler implements MessageHandler {
	Server myServer;
	Client myClient; 
	int peerId;
	Logger logger = Logger.getInstance();
	
	public InterestedMessageHandler(Client client) {
		myClient = client;
	}
	
	public InterestedMessageHandler(Server server, int peerId) {
		myServer = server;
		this.peerId = peerId;
	}
	//
    public boolean receiveMessage(Message message) {
    	RegularMessage messageCast = (RegularMessage)message;
    	if (myClient != null) {
    		System.out.println("Client " + FileManager.getInstance().getThisPeerIdentifier() + " received interested message.");
    		myClient.setInterested(true);
    	} else {

    		System.out.println("Server " + FileManager.getInstance().getThisPeerIdentifier() + " received interested message from " + peerId);
    		ConnectionState connectionState = myServer.getConnectionState(peerId);
    		connectionState.setInterested(true);
    		connectionState.setWaiting(false);
    		myServer.setConnectionState(peerId, connectionState);
			logger.logReceiptOfInterestedMessage(peerId);
    	}
        return false;
    }

    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}