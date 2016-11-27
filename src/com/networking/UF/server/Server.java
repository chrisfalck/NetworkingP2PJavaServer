package com.networking.UF.server;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.primitives.Ints;
import com.networking.UF.FileManager;
import com.networking.UF.Logger;
import com.networking.UF.P2PProtocol;
import com.networking.UF.messages.HandshakeMessage;

public class Server implements Runnable {

	private static final int sPort = 6008;   //The server will be listening on this port number
	static FileManager fileManager = FileManager.getInstance();
	static Logger logger = Logger.getInstance();
	
	ConcurrentHashMap<Integer, ConnectionState> connectionStates = new ConcurrentHashMap<Integer, ConnectionState>();
	
	public ConnectionState getConnectionState(Integer peerId) {
		return connectionStates.get(peerId);
	}
	
	public synchronized void setConnectionState(Integer peerId, ConnectionState connectionState) {
		connectionStates.put(peerId, connectionState);
		System.out.println("\nServer for peer " + fileManager.getThisPeerIdentifier() + " updated connection state for peer " + peerId + ".\n" + connectionState.toString() + "\n");
	}

	public void run() {
		try {
			System.out.println("The server is running."); 
			ServerSocket listener = new ServerSocket(sPort);
			int clientNum = 1;
			try {
				while(true) {
					new Handler(listener.accept(), clientNum, this).start();
					clientNum++;
				}
			} finally {
				listener.close();
			} 

		} catch (Exception exception) {
			System.out.println(exception.getStackTrace());
		}

	}

	/**
	 * A handler thread class.  Handlers are spawned from the listening
	 * loop and are responsible for dealing with a single client's requests.
	 */
	public static class Handler extends Thread {
		private Socket connection;
		private ObjectInputStream in;	//stream read from the socket
		private ObjectOutputStream out;    //stream write to the socket
		private Server myServer;
		private P2PProtocol p2pProtocol;


		public Handler(Socket connection, int no, Server server) {
			this.connection = connection;
			this.myServer = server;
			this.p2pProtocol = new P2PProtocol(fileManager.getThisPeerIdentifier(), "server", myServer);
		}

		public void run() {
			try{
				//initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				in = new ObjectInputStream(connection.getInputStream());
				
				try{
					
					// Most of the business logic of the server will happen in this while loop.
					while(true)
					{
						p2pProtocol.reset();
						
						//receive the message sent from the client
						p2pProtocol.receiveMessage(in);
						
						HandshakeMessage messageToSend = new HandshakeMessage(fileManager.getThisPeerIdentifier());

						System.out.println("Sending message to client: " + new String(Arrays.copyOfRange(messageToSend.toByteArray(), 0, 28)) 
								+ Ints.fromByteArray((Arrays.copyOfRange(messageToSend.toByteArray(), 28, 32))));

						p2pProtocol.sendMessage(out, messageToSend);
					}
				}
				catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				} 
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + fileManager.getThisPeerIdentifier());
			}
			finally{
				//Close connections
				try{
					in.close();
					out.close();
					connection.close();
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Client " + fileManager.getThisPeerIdentifier());
				}
			}
		}
	}

}
