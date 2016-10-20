package com.networking.UF;

import com.networking.UF.handlers.*;
import com.networking.UF.messages.Message;

/**
 * The Peer2Peer Protocol's primary responsibility is to
 */
public class P2PProtocol implements Protocol {

    private MessageHandler chokeMessageHandler;
    private MessageHandler unchokeMessageHandler;
    private MessageHandler interestedMessageHandler;
    private MessageHandler uninterestedMessageHandler;
    private MessageHandler haveMessageHandler;
    private MessageHandler bitfieldMessageHandler;
    private MessageHandler requestMessageHandler;
    private MessageHandler pieceMessageHandler;

    private MessageHandler handshakeMessageHandler;

    /** Initialize with all the necessary message handlers */
    public P2PProtocol() {
        this.chokeMessageHandler = new chokeMessageHandler();
        this.unchokeMessageHandler = new unchokeMessageHandler();
        this.interestedMessageHandler = new interestedMessageHandler();
        this.uninterestedMessageHandler = new uninterestedMessageHandler();
        this.haveMessageHandler = new haveMessageHandler();
        this.bitfieldMessageHandler = new bitfieldMessageHandler();
        this.requestMessageHandler = new RequestMessageHandler();
        this.pieceMessageHandler = new pieceMessageHandler();
        this.handshakeMessageHandler = new HandshakeMessageHandler();
    }

    public void receiveMessage(Message message) {}
    public void sendMessage(Message message) {}

}