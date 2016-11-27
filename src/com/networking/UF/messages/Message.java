package com.networking.UF.messages;

/**
 * Created by clayhausen on 10/19/16.
 */
public interface Message {

    byte[] toByteArray();
    String getMessageType();

}
