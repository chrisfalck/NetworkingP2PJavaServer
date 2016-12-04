package com.networking.UF.server;

import static org.junit.Assert.*;

/**
 * Created by clayhausen on 12/3/16.
 */
public class ServerTest {

    private Server server;
    private ConnectionState connectionState1;
    private ConnectionState connectionState2;
    private ConnectionState connectionState3;

    @org.junit.Before
    public void Setup() {
        server = new Server();
        connectionState1 = new ConnectionState(1);
        connectionState1.setConnectionSpeed(5);
        connectionState2 = new ConnectionState(2);
        connectionState1.setConnectionSpeed(10);
        connectionState3 = new ConnectionState(3);
        connectionState1.setConnectionSpeed(15);
    }

    @org.junit.Test
    public void updatePreferredNeighbors() throws Exception {
        server.updatePreferredNeighbors();
    }

}