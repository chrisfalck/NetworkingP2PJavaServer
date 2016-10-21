package com.networking.UF.client;

import com.networking.UF.Logger;
import com.networking.UF.Peer;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyClient {
    Socket requestSocket;     //socket connect to the server
    FileOutputStream fout;    //stream write to file
    OutputStream out;         //stream write to the socket
    InputStream in;           //stream read from the socket
    byte[] rawData;           // the raw data
    String data;              // data from the file

    int nRead = 0;
    int total = 0;

    public MyClient() {}

    void run()
    {
        try{
            //create a socket to connect to the server
            requestSocket = new Socket("localhost", 8000);
            System.out.println("Connected to localhost in port 8000");

            //initialize inputStream and outputStream
            out = requestSocket.getOutputStream();
            out.flush();
            in = requestSocket.getInputStream();

            //handshake


            //initialize fileInputStream
            fout = new FileOutputStream("xfer_names.dat");

            rawData = new byte[1000];

            nRead = in.read(rawData);
            total += nRead;
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
                System.out.println("Bytes read: " + total);
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

    //main method
    public static void main(String args[])
    {
        MyClient client = new MyClient();
        client.run();
    }

}

