package com.networking.UF.handlers;

import java.nio.ByteBuffer;
import java.util.BitSet;

import com.google.common.primitives.Bytes;
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

	    	int filePieceIndex = Ints.fromByteArray(messageCast.getMessagePayload());
	    	
	    	ConnectionState connectionState = myServer.getClientConnectionState(peerId);
			System.out.println("Server is handling request message: Index received " + filePieceIndex);
	    	connectionState.setFileIndexToSendToClient(filePieceIndex);
	    	connectionState.setNeedToRespondToClientRequestForPiece(true);
	    	myServer.setClientConnectionState(peerId, connectionState);

    	}

    	return false;
    }

    @Override
    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
