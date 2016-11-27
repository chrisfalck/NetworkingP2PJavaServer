package com.networking.UF.messages;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Bytes;
import com.networking.UF.*;

/** */
public class HandshakeMessage implements Message {
	private String handshakeHeader = "P2PFILESHARINGPROJ";
	byte[] zeroBytes = new byte[10];
	int peerId = 0;

	public HandshakeMessage(int peerId) {
		this.peerId = peerId;
	}

	public byte[] toByteArray() {
		byte[] handshakeMessageByteArray = new byte[32];
		byte[] headerBytes = handshakeHeader.getBytes();
		byte[] peerIdBytes = Ints.toByteArray(peerId);
		
		handshakeMessageByteArray = Bytes.concat(headerBytes, zeroBytes, peerIdBytes);
		return handshakeMessageByteArray;
	}
	
	public String getMessageType() {
		return "handshake";
	}
	
	public int getPeerId() {
		return this.peerId;
	}

}