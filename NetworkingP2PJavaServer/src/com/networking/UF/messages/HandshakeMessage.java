package com.networking.UF.messages;

import com.networking.UF.*;

public class HandshakeMessage {
	private String handshakeHeader = "";
	byte[] zeroBytes = new byte[10];
	int peerId = 0;

	public HandshakeMessage(String handshakeHeader, int peerId) {
		this.handshakeHeader = handshakeHeader;
		this.peerId = peerId;
	}
}