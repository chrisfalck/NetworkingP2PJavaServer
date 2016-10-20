package com.networking.UF;

import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;

public interface Protocol {

    void receiveMessage(Message message);
    void sendMessage(Message message);


}