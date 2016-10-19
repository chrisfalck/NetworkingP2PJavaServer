package com.networking.UF;

import com.networking.UF.messages.RegularMessage;

public interface Protocol {

    public void receiveMessage(String message); // update type
    public void sendMessage(String message); // update type


}