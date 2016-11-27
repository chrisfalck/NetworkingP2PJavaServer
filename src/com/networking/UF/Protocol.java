package com.networking.UF;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.networking.UF.messages.Message;

public interface Protocol {

    public void receiveMessage(ObjectInputStream in) throws Exception; // update type
    public void sendMessage(ObjectOutputStream out, Message messageToSend) throws Exception; // update type

}