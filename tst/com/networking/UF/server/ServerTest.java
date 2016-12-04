package com.networking.UF.server;

import com.networking.UF.CommonConfig;
import com.networking.UF.ConfigParser;
import com.networking.UF.FileManager;

import java.io.IOException;
import java.util.Enumeration;

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
        FileManager fileManager = FileManager.getInstance();
        try {
            fileManager.initializeDirectoriesAndFiles(5);
        } catch (IOException e) {
            System.out.println("Couldn't read file");
        }
        server = new Server();
        connectionState1 = new ConnectionState(1);
        connectionState1.setConnectionSpeed(5);
        server.setConnectionState(1, connectionState1);
        connectionState2 = new ConnectionState(2);
        connectionState2.setConnectionSpeed(10);
        server.setConnectionState(2, connectionState2);
        connectionState3 = new ConnectionState(3);
        connectionState3.setConnectionSpeed(15);
        server.setConnectionState(3, connectionState3);
    }

    @org.junit.Test
    public void updatePreferredNeighbors() throws Exception {
        server.setNumPreferredNeighbors(2);
        server.updatePreferredNeighbors();
        // Only the connection with the largest delay should be choked
        assertFalse(connectionState1.isChoked());
        assertFalse(connectionState2.isChoked());
        assertTrue(connectionState3.isChoked());

        ConnectionState connectionState4 = new ConnectionState(4);
        connectionState4.setConnectionSpeed(10);
        server.setConnectionState(4, connectionState4);
        server.updatePreferredNeighbors();
        // In the case of a tie, only one should be choked
        assertTrue(connectionState2.isChoked() ^ connectionState4.isChoked());
    }

}