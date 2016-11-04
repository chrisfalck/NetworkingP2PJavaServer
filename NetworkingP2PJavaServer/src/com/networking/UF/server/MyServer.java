package com.networking.UF.server;


import com.networking.UF.Logger;
import com.networking.UF.P2PProtocol;
import com.networking.UF.Protocol;
import com.networking.UF.messages.HandshakeMessage;
import com.networking.UF.messages.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MyServer extends Thread {

    private int peerId;

    private static final Logger logger = Logger.getInstance();
    private static final int sPort = 8000;   //The server will be listening on this port number

    public MyServer(int peerId) {
        this.peerId = peerId;
    }

    public void run() {

        System.out.println("The server is running.");

        //TODO
        // Check that peerId matches one from PeerList.cfg

        try {

            ServerSocket listener = new ServerSocket(sPort);
            int clientNum = 1;

            try {
                while (true) {
                    new Handler(peerId, listener.accept(), clientNum).start();
                    logger.logTCPCreationEvent(peerId, "incoming");
                    System.out.println("Client " + clientNum + " is connected!");
                    clientNum++;
                }
            } finally {
                listener.close();
            }
        } catch (IOException e) {
            System.err.println("Server: socket creation error. Exiting...");
            System.exit(0);
        }
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for dealing with a single client's requests.
     */
    private static class Handler extends Thread {
        int peerId;
        private static final Protocol protocol = P2PProtocol.getInstance();

        private Message inMessage;   // POD for structuring incoming Messages
        private Message outMessage;  // POD for structuring outgoing messages

        private byte[] fileBuffer = new byte[1000]; // data from the file
        private Socket connection;
        private FileInputStream fin;  //stream read from file
        private InputStream in;	      //stream read from the socket
        private OutputStream out;     //stream write to the socket
        private int no;		          //The index number of the client
        byte[] rawData;               //temporarily used to store incoming/outgoing data in byte form

        // Keep track of amount of data transferred
        int bytesRead = 0;
        int totalBytesRead = 0;

        public Handler(int peerId, Socket connection, int no) {
            this.peerId = peerId;
            this.connection = connection;
            this.no = no;
        }

        public void run() {
            try {
                //initialize Input and Output streams
                out = connection.getOutputStream();
                out.flush();
                in = connection.getInputStream();

                //receive handshake
                boolean waitingForHandshake = true;

                //TODO move logic into Protocol object
                while (waitingForHandshake) {
                    System.out.println("Server: waiting for handshake...");
                    rawData = new byte[32];

                    try {
                        bytesRead = in.read(rawData, 0, 32);
                        System.out.println("Server: bytes read [" + bytesRead + "]");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                    if (bytesRead == 32) {
                        byte[] rawHeader = Arrays.copyOfRange(rawData, 0, 18);
                        byte[] zeroBits = Arrays.copyOfRange(rawData, 18, 27);
                        byte[] rawIncomingPeerId = Arrays.copyOfRange(rawData, 27, 31);

                        String header = new String(rawHeader);
                        System.out.println("Server: header received: " + header);
                        int incomingPeerId = ByteBuffer.wrap(rawIncomingPeerId).getInt();

                        String expectedHeader = "P2PFILESHARINGPROJ";

                        //TODO add check for correct peerId
                        if (header.equalsIgnoreCase(expectedHeader)) {
                            waitingForHandshake = false;
                            System.out.println("Server: handshake received");
                        }

                    }

                }

                //send handshake response
                outMessage = new HandshakeMessage(peerId);
                System.out.println("Server: sending handshake response...");
                out.write(outMessage.toByteArray());

                //initialize file input stream
                fin = new FileInputStream("names.dat");
                while ((bytesRead = fin.read(fileBuffer)) != -1) {
                    totalBytesRead += bytesRead;
                }
                System.out.println("Server: total bytes read from file [" + totalBytesRead + "]");

                System.out.println("Server: sending file...");
                sendBytes(Arrays.copyOfRange(fileBuffer, 0, totalBytesRead));


            }
            catch(IOException ioException){
                System.out.println("Disconnect with Client " + no);
            }
            finally{
                //Close connections
                try{
                    in.close();
                    out.close();
                    fin.close();
                    connection.close();
                    System.out.println("Bytes sent: " + totalBytesRead);
                }
                catch(IOException ioException){
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }

        //send bytes to the output stream
        public void sendBytes(byte[] data) {
            try {
                out.write(data);
                out.flush();
                System.out.println("Sent " + data.length + " bytes to Client");
            } catch(IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

}
