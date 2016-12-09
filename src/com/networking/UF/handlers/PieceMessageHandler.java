package com.networking.UF.handlers;

import java.util.Arrays;
import java.util.BitSet;

import com.google.common.primitives.Ints;
import com.networking.UF.FileManager;
import com.networking.UF.client.Client;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;
import com.networking.UF.server.ConnectionState;
import com.networking.UF.server.Server;

/**
 * Created by clayhausen on 10/18/16.
 */
public class PieceMessageHandler implements MessageHandler {
	Server myServer;
	Client myClient; 
	int peerId;
	private static FileManager fileManager = FileManager.getInstance();
	
	public PieceMessageHandler(Client client){
		myClient = client;
	}
	
	public PieceMessageHandler(Server server, int peerId){
		myServer = server;
		this.peerId = peerId;
	}

    public boolean receiveMessage(Message message) {
    	RegularMessage messageCast = (RegularMessage)message;
    	
    	byte[] filePieceIndexBytes = Arrays.copyOfRange(messageCast.getMessagePayload(), 0, 4);
    	int filePieceIndex = Ints.fromByteArray(filePieceIndexBytes);

    	byte[] filePieceContentBytes = Arrays.copyOfRange(messageCast.getMessagePayload(), 4, messageCast.getMessagePayload().length);
    	
    	if(myClient != null) {
    		System.out.println("Client received file piece " + filePieceIndex + " from " + myClient.getServerPeerId());
    		myClient.setShouldDealWithPieceMessage(true);

    		// For use in the have message cascade. 
    		myClient.setCurrentHaveMessageIndexToSend(filePieceIndexBytes);

    		fileManager.addFilePiece(filePieceIndex, filePieceContentBytes);
    	}
    	
        return false;
    }

    @Override
    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
