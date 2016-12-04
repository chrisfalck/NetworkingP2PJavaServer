package com.networking.UF.server;

import com.networking.UF.FileManager;
import com.networking.UF.Peer;
import com.networking.UF.client.Client;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by clayhausen on 12/3/16.
 */
public class ServerTest {

    private Peer peer;

    private Server server;
    private ConnectionState clientConnectionState1;
    private ConnectionState clientConnectionState2;
    private ConnectionState clientConnectionState3;

    private ConnectionState serverConnectionState1;
    private ConnectionState serverConnectionState2;
    private ConnectionState serverConnectionState3;

    @org.junit.Before
    public void Setup() {
        FileManager fileManager = FileManager.getInstance();
        try {
            fileManager.initializeDirectoriesAndFiles(5);
        } catch (IOException e) {
            System.out.println("Couldn't read file");
        }
        peer = new Peer(5);

        clientConnectionState1 = new ConnectionState(1);
        clientConnectionState1.setConnectionSpeed(5);
        Client client1 = new Client("", 4000, 1, peer);
        client1.setConnectionState(clientConnectionState1);
        peer.addClient(client1);

        clientConnectionState2 = new ConnectionState(2);
        clientConnectionState2.setConnectionSpeed(10);
        Client client2 = new Client("", 4000, 2, peer);
        client2.setConnectionState(clientConnectionState2);
        peer.addClient(client2);

        clientConnectionState3 = new ConnectionState(3);
        clientConnectionState3.setConnectionSpeed(15);
        Client client3 = new Client("", 4000, 3, peer);
        client3.setConnectionState(clientConnectionState3);
        peer.addClient(client3);

        server = new Server(peer);
        serverConnectionState1 = new ConnectionState(1);
        server.setConnectionState(1, serverConnectionState1);
        serverConnectionState2 = new ConnectionState(2);
        server.setConnectionState(2, clientConnectionState2);
        serverConnectionState3 = new ConnectionState(3);
        server.setConnectionState(3, clientConnectionState3);
    }

    @org.junit.Test
    public void updatePreferredNeighbors() throws Exception {
        server.setNumPreferredNeighbors(2);
        server.updatePreferredNeighbors();
        // Only the connection with the largest delay should be choked
        assertTrue(clientConnectionState1.isChoked());
        assertFalse(clientConnectionState2.isChoked());
        assertFalse(clientConnectionState3.isChoked());

        ConnectionState connectionState4 = new ConnectionState(4);
        connectionState4.setConnectionSpeed(10);
        server.setConnectionState(4, connectionState4);
        server.updatePreferredNeighbors();
        // In the case of a tie, only one should be choked
        assertTrue(clientConnectionState2.isChoked() ^ connectionState4.isChoked());
    }

}