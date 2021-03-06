package com.networking.UF.handlers;

import com.networking.UF.Logger;
import com.networking.UF.client.Client;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;
import com.networking.UF.server.ConnectionState;
import com.networking.UF.server.Server;

/**
 * Created by clayhausen on 10/18/16.
 */
public class UnchokeMessageHandler implements MessageHandler {
	Server myServer;
	Client myClient; 
	int peerId;
	Logger logger = Logger.getInstance();
	
	public UnchokeMessageHandler(Client client){
		myClient = client;
	}
	
	public UnchokeMessageHandler(Server server, int peerId){
		myServer = server;
		this.peerId = peerId;
	}
	
	@Override
    public boolean receiveMessage(Message message) {
		RegularMessage messageCast = (RegularMessage)message;
    	if(myClient != null){
    		myClient.setChoked(false);
			logger.logUnchokingEvent(myClient.getServerPeerId());
    	}
    	else{
	    	ConnectionState connectionState = myServer.getClientConnectionState(peerId);
	    	connectionState.setClientIsChoked(false);
	    	myServer.setClientConnectionState(peerId, connectionState);
    	}
        return false;
    }

    @Override
    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
