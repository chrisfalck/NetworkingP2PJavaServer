package com.networking.UF.handlers;

import java.util.Arrays;
import java.util.BitSet;

import com.networking.UF.FileManager;
import com.networking.UF.client.Client;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;
import com.networking.UF.server.ConnectionState;
import com.networking.UF.server.Server;

/**
 * Created by clayhausen on 10/18/16.
 */
public class BitfieldMessageHandler implements MessageHandler {
	private Server myServer;
	private Client myClient;
	private int peerId;
	private static FileManager fileManager = FileManager.getInstance();

	public BitfieldMessageHandler(Server server, int peerId) {
		this.myServer = server;
		this.peerId = peerId;
	}
	
	public BitfieldMessageHandler(Client client) {
		this.myClient = client;
	}
	
    @Override
    public boolean receiveMessage(Message message) {
    	
    	RegularMessage messageCast = (RegularMessage)message;    	

    	BitSet newBitfield = BitSet.valueOf(messageCast.getMessagePayload());
    	
    	// Because every Peer to Peer connection will be connected by client to server and server to client
    	// for each Peer, it isn't necessary for the client to track any of the state infor when receiving 
    	// a bitfield message. 
    	if (myClient != null) {
    		myClient.setHaveReceivedBitfield(true);
    	} else {
    		ConnectionState connectionState = myServer.getConnectionState(peerId);
    		connectionState.setBitfield(newBitfield);
    		myServer.setConnectionState(peerId, connectionState);
    	}
    	
        return false;
    }

    @Override
    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
