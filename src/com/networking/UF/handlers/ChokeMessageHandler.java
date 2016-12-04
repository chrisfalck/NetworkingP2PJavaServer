package com.networking.UF.handlers;

import com.networking.UF.client.Client;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;
import com.networking.UF.server.ConnectionState;
import com.networking.UF.server.Server;

/**
 * Created by clayhausen on 10/18/16.
 */
public class ChokeMessageHandler implements MessageHandler {
	Server myServer;
	Client myClient; 
	int peerId;
	
	public ChokeMessageHandler(Client client){
		myClient = client;
	}
	
	public ChokeMessageHandler(Server server, int peerId){
		myServer = server;
		this.peerId = peerId;
	}
	
    public boolean receiveMessage(Message message) {
		RegularMessage messageCast = (RegularMessage)message;
    	if(myClient != null){
    		myClient.setChoked(true);    		
    	}
    	else{
	    	ConnectionState connectionState = myServer.getConnectionState(peerId);
	    	connectionState.setChoked(true);
	    	myServer.setConnectionState(peerId, connectionState);
    	}
        return false;
    }

    @Override
    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
