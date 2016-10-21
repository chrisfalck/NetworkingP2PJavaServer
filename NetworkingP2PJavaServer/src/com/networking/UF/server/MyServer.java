package com.networking.UF.server;

import com.networking.UF.Peer;

import java.io.*;
        import java.net.ServerSocket;
        import java.net.Socket;
        import java.util.Arrays;

public class MyServer {

    private static final int sPort = 8000;   //The server will be listening on this port number

    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        ServerSocket listener = new ServerSocket(sPort);
        int clientNum = 1;
        try {
            while(true) {
                new Handler(listener.accept(),clientNum).start();
                System.out.println("Client "  + clientNum + " is connected!");
                clientNum++;
            }
        } finally {
            listener.close();
        }

    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for dealing with a single client's requests.
     */
    private static class Handler extends Thread {
        private byte[] fileBuffer = new byte[1000]; // data from the file
        private Socket connection;
        private FileInputStream fin;      //stream read from file
        private InputStream in;	      //stream read from the socket
        private OutputStream out;         //stream write to the socket
        private int no;		      //The index number of the client

        int nRead = 0;
        int total = 0;

        public Handler(Socket connection, int no) {
            this.connection = connection;
            this.no = no;
        }

        public void run() {
            try{
                //initialize file input stream
                fin = new FileInputStream("names.dat");
                while ((nRead = fin.read(fileBuffer)) != -1) {
                    total += nRead;
                }
                System.out.println("Total bytes read: " + total);

                //initialize Input and Output streams
                out = connection.getOutputStream();
                out.flush();
                in = connection.getInputStream();

                sendBytes(Arrays.copyOfRange(fileBuffer, 0, total));


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
                    System.out.println("Bytes sent: " + total);
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

