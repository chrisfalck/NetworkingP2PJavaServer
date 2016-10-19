package com.networking.UF.handlers;

import com.networking.UF.messages.Message;

/**
 * Created by clayhausen on 10/19/16.
 */
public interface MessageHandler {
    public boolean receiveMessage(Message message);
    public boolean sendMessage(Message message);
}
