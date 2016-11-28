package com.networking.UF.server;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageTypeSpecifier;

import com.google.common.primitives.Ints;
import com.networking.UF.FileManager;
import com.networking.UF.Logger;
import com.networking.UF.MessageType;
import com.networking.UF.P2PProtocol;
import com.networking.UF.messages.HandshakeMessage;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;

public class Server implements Runnable {

	private static final int sPort = 6009;   //The server will be listening on this port number
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
		// The Socket representing our TCP connectin. 
		private Socket connection;
		
		// The instream from our TCP connection. 
		private ObjectInputStream in;	
		
		// The outstream for our TCP connection. 
		private ObjectOutputStream out;    

		// The Server that started this Handler thread. 
		private Server myServer;
		
		// The P2PProtocol created for use by this Handler thread. 
		private P2PProtocol p2pProtocol;


		public Handler(Socket connection, int no, Server server) {
			this.connection = connection;
			this.myServer = server;
			this.p2pProtocol = new P2PProtocol(fileManager.getThisPeerIdentifier(), "server", myServer);
		}
		
		/**
		 * Determines the next message the client should send by analysing the client's state variables. 
		 * Returns an appropriate built message to be sent. 
		 * @return
		 * @throws InterruptedException
		 */
		private Message getNextMessageToSend() throws InterruptedException {
			ConnectionState connectionState = myServer.getConnectionState(p2pProtocol.getConnectedPeerId());
			if (connectionState.haveReceivedHandshake() && !connectionState.haveReceivedBitfield()) {
				System.out.println("Building handshake message to send to client.");

				return new HandshakeMessage(fileManager.getThisPeerIdentifier());
			} 
			else if (connectionState.haveReceivedHandshake() && connectionState.haveReceivedBitfield()) {
				System.out.println("Building bitfield message to send to client.");
				BitSet bitfield = fileManager.getBitfield();
				int messageLength = 4 + 1 + bitfield.size();
				RegularMessage bitfieldMessage = new RegularMessage(messageLength, MessageType.bitfield, bitfield.toByteArray());

				return bitfieldMessage;
			} 
			else {
				System.out.println("Waiting for further implementation.");
				while(true){}
			}
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
						
						// Wait for the client messages to arrive. 
						p2pProtocol.receiveMessage(in);
						
						Message messageToSend = getNextMessageToSend();

						System.out.println("Sending message to client: " + p2pProtocol.getConnectedPeerId());

						p2pProtocol.sendMessage(out, messageToSend);
						System.out.println("End-Server------------------------------------------------------------------------\n\n\n");
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
