package com.networking.UF.messages;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

public class RegularMessage implements Message {
    private int messageLength;
    private int messageType;
    private byte[] messagePayload;

    public RegularMessage(int messageLength, int messageType, byte[] messagePayload) {
        this.messageLength = messageLength;
        this.messageType = messageType;
        this.messagePayload = messagePayload;
    }

    public int getMessageLength() {
		return messageLength;
	}

	public byte[] getMessagePayload() {
		return messagePayload;
	}

	public byte[] toByteArray() {
        byte[] messageLengthBytes = Ints.toByteArray(messageLength);
        byte[] lastByteOfConvertedMessageType = new byte[] {Ints.toByteArray(messageType)[3]};

        return Bytes.concat(messageLengthBytes, lastByteOfConvertedMessageType, messagePayload);
    }
    
    public String getMessageType() {
		switch (this.messageType) {
		case 0:
			return "choke";
		case 1:
			return "unchoke";
		case 2:
			return "interested";
		case 3:
			return "not interested";
		case 4:
			return "have";
		case 5:
			return "bitfield";
		case 6:
			return "request";
		case 7:
			return "piece";
		default:
			return "unknown";
		}
    }
}
