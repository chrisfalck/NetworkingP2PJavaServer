package com.networking.UF.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.TimeUnit;

import com.google.common.primitives.Ints;
import com.networking.UF.FileManager;
import com.networking.UF.Logger;
import com.networking.UF.MessageType;
import com.networking.UF.P2PProtocol;
import com.networking.UF.messages.HandshakeMessage;
import com.networking.UF.messages.Message;
import com.networking.UF.messages.RegularMessage;

public class Client implements Runnable {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
	ObjectInputStream in;          //stream read from the socket
	static FileManager fileManager = FileManager.getInstance();
	static Logger logger = Logger.getInstance();
	private P2PProtocol p2pProtocol = new P2PProtocol(fileManager.getThisPeerIdentifier(), "client", this);
	private String serverAddress;
	private int portNumber;
	private int serverPeerId;

	// Client state variables. 
	private boolean haveReceivedHandshake = false;
	private boolean haveReceivedBitfield = false;
	
	public void setHaveReceivedBitfield(boolean haveReceivedBitfield) {
		this.haveReceivedBitfield = haveReceivedBitfield;
	}

	public void setHaveReceivedHandshake(boolean haveReceivedHandshake) {
		this.haveReceivedHandshake = haveReceivedHandshake;
	}

	/**
	 * Steps for to follow P2P Protocol.
	 * Establish a TCP connection with the server and log its creation.
	 * Send a handshake message.
	 * Handle the handshake response. 
	 * Send a bitfield message.
	 * 		The bitfield message payload is a bit array where each bit represents whether this peer has the corresponding file piece or not. 
	 * Start sending regular messages.
	 *   
	 * 
	 * @param serverAddress
	 * @param portNumber
	 * @param serverPeerId
	 */
	public Client(String serverAddress, int portNumber, int serverPeerId) {
		this.serverAddress = serverAddress;
		this.portNumber = portNumber;
		this.serverPeerId = serverPeerId;
	}
	
	private Message getNextMessageToSend() throws InterruptedException {
		if (!haveReceivedHandshake) {
			return new HandshakeMessage(fileManager.getThisPeerIdentifier());
		} else if (haveReceivedHandshake && !haveReceivedBitfield) {
			BitSet bitfield = fileManager.getBitfield();
			int messageLength = 4 + 1 + bitfield.size();
			RegularMessage bitfieldMessage = new RegularMessage(messageLength, MessageType.bitfield, bitfield.toByteArray());
			return bitfieldMessage;
		} else if (haveReceivedHandshake && haveReceivedBitfield) {
			TimeUnit.MINUTES.wait(5);
		}
		
		return null;
	}

	public void run()
	{
		try{
			
			// Time for all servers to start before clients start sending messages. 
			TimeUnit.SECONDS.sleep(10);

			// Create a socket to connect to the server.
			requestSocket = new Socket(this.serverAddress, this.portNumber);
			System.out.println("Client from peer " + fileManager.getThisPeerIdentifier() + " connected to " + this.serverAddress + " on port " + this.portNumber);
			logger.logTCPCreationEvent(this.serverPeerId, "outgoing");

			// Initialize inputStream and outputStream.
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			while(true)
			{
				p2pProtocol.reset();

				Message messageToSend = getNextMessageToSend();
				System.out.println("Sending message to server peer " + this.serverPeerId + " from client " + fileManager.getThisPeerIdentifier() + "\n");

				p2pProtocol.sendMessage(out, messageToSend);
				p2pProtocol.receiveMessage(in);
				System.out.println("\n\n\n\n\n\n");
				TimeUnit.SECONDS.sleep(10);
			}
		}
		catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				System.err.println(ioException.toString());
				ioException.printStackTrace();
			}
		}
	}

}
