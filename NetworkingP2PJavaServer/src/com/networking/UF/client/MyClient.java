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

public class MyClient extends Thread {

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

    public MyClient(int peerId) {
        this.peerId = peerId;
    }

    public void run()
    {
        try {
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
            System.out.println("Client: sending handshake...");
            out.write(outMessage.toByteArray());

            boolean waitingForHandshakeResponse = true;

            //TODO move logic into Protocol object
            while (waitingForHandshakeResponse) {
                System.out.println("Client: waiting for handshake response...");
                rawData = new byte[32];

                try {
                    bytesRead = in.read(rawData, 0, 32);
                    System.out.println("Client: bytes read [" + bytesRead + "]");
                } catch (IOException ioException) {
                    System.out.println("Client: Error reading data...");
                    ioException.printStackTrace();
                }

                if (bytesRead == 32) {
                    byte[] rawHeader = Arrays.copyOfRange(rawData, 0, 18);
                    byte[] zeroBits = Arrays.copyOfRange(rawData, 18, 27);
                    byte[] rawIncomingPeerId = Arrays.copyOfRange(rawData, 27, 31);

                    String header = new String(rawHeader);
                    System.out.println("Client: header received: " + header);
                    int incomingPeerId = ByteBuffer.wrap(rawIncomingPeerId).getInt();

                    String expectedHeader = "P2PFILESHARINGPROJ";

                    //TODO add check for correct peerId
                    if (header.equalsIgnoreCase(expectedHeader)) {
                        waitingForHandshakeResponse = false;
                        System.out.println("Client: handshake response received");
                    }

                }

            }

            String filename = "xfer_names.dat";
            //initialize fileInputStream
            System.out.println("Client: creating empty file [" + filename + "]");
            fout = new FileOutputStream(filename);

            rawData = new byte[1000];

            System.out.println("Client: downloading file...");
            bytesRead = in.read(rawData);
            totalBytesRead += bytesRead;

            // write data to file
            System.out.println("Client: writing file...");
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

}

