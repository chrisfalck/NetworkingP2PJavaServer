package com.networking.UF.handlers;

import java.util.BitSet;

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
	private static FileManager fileManager = FileManager.getInstance();
	
	public RequestMessageHandler(Client client) {
		myClient = client;
	}
	
	public RequestMessageHandler(Server server, int peerId) {
		myServer = server;
		this.peerId = peerId;
	}

    public boolean receiveMessage(Message message) {
    	RegularMessage messageCast = (RegularMessage)message;
    	
    	BitSet filePiece = BitSet.valueOf(messageCast.getMessagePayload());
    	byte[] filePieceIndex = messageCast.getPieceIndex(filePiece);
    	byte[] filePieceContent = messageCast.getPieceContent(filePiece);
    	
    	if(myClient != null){
    		myClient.setHasReceivedPiece(true);
    	}
    	else{
    		ConnectionState connectionState = myServer.getConnectionState(peerId);
    		connectionState.setHasReceivedPiece(true);
    		myServer.setConnectionState(peerId, connectionState);
    	}
    	
    	return false;
    }

    @Override
    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
