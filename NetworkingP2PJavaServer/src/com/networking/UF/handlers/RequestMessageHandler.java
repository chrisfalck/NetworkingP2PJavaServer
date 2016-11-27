package com.networking.UF.handlers;

import com.networking.UF.messages.Message;

/**
 * Created by clayhausen on 10/19/16.
 */
public class RequestMessageHandler implements MessageHandler {
    @Override
    public boolean receiveMessage(Message message) {
        return false;
    }

    @Override
    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
