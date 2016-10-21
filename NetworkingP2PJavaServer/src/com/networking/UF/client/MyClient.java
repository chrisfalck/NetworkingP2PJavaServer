package com.networking.UF.client;

import com.google.common.primitives.Ints;
import com.networking.UF.Logger;
import com.networking.UF.P2PProtocol;
import com.networking.UF.Protocol;
import com.networking.UF.messages.HandshakeMessage;
import com.networking.UF.messages.Message;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MyClient {

    int peerId;

    private static final Protocol protocol = new P2PProtocol();
    private static final Logger logger = Logger.getInstance();

    private Message inMessage;   // POD for structuring incoming Messages
    private Message outMessage;  // POD for structuring outgoing messages

    Socket requestSocket;     //socket connect to the server
    FileOutputStream fout;    //stream write to file
    OutputStream out;         //stream write to the socket
    InputStream in;           //stream read from the socket
    byte[] rawData;           //temporarily used to store incoming/outgoing data in byte form

    // Keep track of amount of data transferred
    int bytesRead = 0;
    int totalBytesRead = 0;

    public MyClient(int peerId) { this.peerId = peerId; }

    void run()
    {
        try{
            //create a socket to connect to the server
            requestSocket = new Socket("localhost", 8000);
            logger.logTCPCreationEvent(peerId, "outgoing");
            System.out.println("Connected to localhost in port 8000");

            //initialize inputStream and outputStream
            out = requestSocket.getOutputStream();
            out.flush();
            in = requestSocket.getInputStream();

            //handshake
            outMessage = new HandshakeMessage(peerId);
            out.write(outMessage.toByteArray());

            boolean waitingForHandshakeResponse = false;

            //TODO move logic into Protocol object
            while (waitingForHandshakeResponse) {
                rawData = new byte[32];

                try {
                    bytesRead = in.read(rawData, 0, 31);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                if (bytesRead == 32) {
                    byte[] rawHeader = Arrays.copyOfRange(rawData, 0, 18);
                    byte[] zeroBits = Arrays.copyOfRange(rawData, 18, 27);
                    byte[] rawIncomingPeerId = Arrays.copyOfRange(rawData, 27, 31);

                    String header = new String(rawHeader);
                    int incomingPeerId = ByteBuffer.wrap(rawIncomingPeerId).getInt();

                    String expectedHeader = "P2PFILESHARINGPROJ";

                    //TODO add check for correct peerId
                    if (header.equalsIgnoreCase(expectedHeader)) {
                        waitingForHandshakeResponse = true;
                        System.out.println("Client: handshake response received");
                    }

                }

            }

            //initialize fileInputStream
            fout = new FileOutputStream("xfer_names.dat");

            rawData = new byte[1000];

            bytesRead = in.read(rawData);
            totalBytesRead += bytesRead;
            // write data to file
            fout.write(rawData);
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
        finally{
            //Close connections
            try{
                in.close();
                out.close();
                fout.close();
                requestSocket.close();
                System.out.println("Bytes read: " + totalBytesRead);
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

    //main method
    public static void main(String args[])
    {
        if (args.length == 0) {
            System.out.println("A peer ID must be provided as an argument. Exiting...");
            System.exit(0);
        }

        //TODO
        // Check that peerId matches one from PeerList.cfg

        int peerId = Integer.parseInt(args[0]);
        MyClient client = new MyClient(peerId);
        client.run();
    }

}

