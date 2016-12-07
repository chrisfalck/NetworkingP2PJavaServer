package com.networking.UF.handlers;

import com.networking.UF.messages.Message;

/**
 * Created by clayhausen on 10/18/16.
 */
public class HaveMessageHandler implements MessageHandler {
	
    public boolean receiveMessage(Message message) {
        return false;
    }

    public byte[] prepareMessageForSending(Message message) {
        return null;
    }
}
