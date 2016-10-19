package com.networking.UF;

import com.networking.UF.handlers.*;

public class P2PProtocol implements Protocol {

    private MessageHandler chokeMessageHandler;
    private MessageHandler unchokeMessageHandler;
    private MessageHandler interestedMessageHandler;
    private MessageHandler uninterestedMessageHandler;
    private MessageHandler haveMessageHandler;
    private MessageHandler bitfieldMessageHandler;
    private MessageHandler requestMessageHandler;
    private MessageHandler pieceMessageHandler;

    public void receiveMessage(String message) {}
    public void sendMessage(String message) {}

    public P2PProtocol() {
        this.chokeMessageHandler = new chokeMessageHandler();
        this.unchokeMessageHandler = new unchokeMessageHandler();
        this.interestedMessageHandler = new interestedMessageHandler();
        this.uninterestedMessageHandler = new uninterestedMessageHandler();
        this.haveMessageHandler = new haveMessageHandler();
        this.bitfieldMessageHandler = new bitfieldMessageHandler();
        this.requestMessageHandler = new RequestMessageHandler();
        this.pieceMessageHandler = new pieceMessageHandler();
    }



}