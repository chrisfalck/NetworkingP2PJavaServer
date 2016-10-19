package com.networking.UF.messages;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

public class RegularMessage {
    private int messageLength;
    private int messageType;
    private byte[] messagePayload;

    public RegularMessage(int messageLength, int messageType, byte[] messagePayload) {
        this.messageLength = messageLength;
        this.messageType = messageType;
        this.messagePayload = messagePayload;
    }

    public byte[] toByteArray() {
        byte[] messageLengthBytes = Ints.toByteArray();

        return Bytes.concat(messageLengthBytes, Ints.toByteArray(messageType), messagePayload);
    }
}
