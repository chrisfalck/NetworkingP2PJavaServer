package com.networking.UF.handlers;

import java.nio.ByteBuffer;
import java.util.BitSet;

import com.google.common.primitives.Ints;
import com.networking.UF.FileManager;
import com.networking.UF.client.Client;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;
import com.networking.UF.server.ConnectionState;
import com.networking.UF.server.Server;

/**
 * Created by clayhausen on 10/19/16.
 */
public class RequestMessageHandler implements MessageHandler {
	Server myServer;
	Client myClient; 
	int peerId;

	
	public RequestMessageHandler(Server server, int peerId) {
		myServer = server;
		this.peerId = peerId;
	}

    public boolean receiveMessage(Message message) {
    	RegularMessage messageCast = (RegularMessage)message;
    	
    	if(myServer != null){
    	
	    	BitSet filePiece = BitSet.valueOf(messageCast.getMessagePayload());
	    	byte[] filePieceIndex = (messageCast.getPieceIndex(filePiece));
	    	int result = Ints.fromByteArray(filePieceIndex);
	    	
	    	ConnectionState connectionState = myServer.getConnectionState(peerId);
	    	connectionState.setFileIndexToSend(result);
	    	myServer.setConnectionState(peerId, connectionState);
    	
    	}
    	return false;
    }

    @Override
    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
