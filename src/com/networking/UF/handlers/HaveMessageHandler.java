package com.networking.UF.handlers;

import com.google.common.primitives.Ints;
import com.networking.UF.BitfieldUtils;
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
public class HaveMessageHandler implements MessageHandler {
	Server myServer;
	Client myClient; 
	int peerId;
	private static FileManager fileManager = FileManager.getInstance();
	Logger logger = Logger.getInstance();
	
	
	public HaveMessageHandler(Server server, int peerId){
		myServer = server;
		this.peerId = peerId;
	}
	
    public boolean receiveMessage(Message message) {
    
    	RegularMessage messageCast = (RegularMessage)message;
    	
    	if (myServer != null){
	    	byte[] index = messageCast.getMessagePayload();

	    	ConnectionState connectionState = myServer.getClientConnectionState(peerId);
	    	ConnectionState updatedConnectionState = BitfieldUtils.updateServerOwnedClientConnectionState(index, connectionState);

	    	if(fileManager.getFilePieceAtIndex(Ints.fromByteArray(index)).length == 0){
	    		updatedConnectionState.setClientIsInterested(true);
	    	}
	    	else{
	    		updatedConnectionState.setClientIsInterested(false);
	    	}

	    	myServer.setClientConnectionState(peerId, updatedConnectionState);
			logger.logReceiptOfHaveMessage(peerId, Ints.fromByteArray(index));
    	}
    	 return false;
    }

    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
