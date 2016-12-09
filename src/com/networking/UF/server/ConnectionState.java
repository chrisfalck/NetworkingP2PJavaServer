package com.networking.UF.server;

import java.util.BitSet;

import javax.swing.text.DefaultEditorKit.CutAction;

public class ConnectionState {
	
	private int peerId;
	private boolean haveSentHandshake = false;
	private boolean haveSentBitfield = false;
	private boolean choked = true;
	private boolean optimisticallyUnchoked = false;
	private boolean interested = false;
	private int fileIndexToSend = -1;
	private BitSet bitfield = new BitSet();
	private boolean haveReceivedHave = false;
	private boolean needToUpdatePreferredNeighbors = false;
	private boolean needToUpdateOptimisticNeighbor = false;
	private boolean needToRespondToClientInterestedStatus = false;
	private boolean needToRespondToClientRequestForPiece = false;
	
	public String toString() {
		return new String("Connection state for peerId: " + peerId + "\n" +
						  "Have sent handshake to client: " + haveSentHandshakeToClient() + "\n" +
						  "Have sent bitfield to client: " + haveSentBitfieldToClient() + "\n" +
						  "Client has sent a have message: " + getHaveReceivedHaveMsgFromClient() + "\n" +
						  "Client is currently choked: " + clientIsChoked() + "\n" + 
						  "Client is currently optimistically unchoked: " + clientIsOptimisticallyUnchoked() + "\n" + 
						  "Client is interested in this server: " + clientIsInterested() + "\n" + 
						  "Server needs to update this client's preferred neighbor status: " + needToUpdatePreferredNeighbors() + "\n" + 
						  "Server needs to update this client's optimistic neighbor status: " + needToUpdateOptimisticNeighbor() + "\n" + 
						  "File index to send to client: " + getFileIndexToSendToClient() + "\n" + 
						  "Bitfield size: " + ((getClientBitfield().length() == 0) ? "0" : bitfield.size()));
	}
	
	public ConnectionState(int peerId) {
		this.peerId = peerId;
	}
	
	public boolean haveSentHandshakeToClient() {
		return haveSentHandshake;
	}

	public void setHaveSentHandshakeToClient(boolean haveSentHandshake) {
		this.haveSentHandshake = haveSentHandshake;
	}
	
	public boolean haveSentBitfieldToClient() {
		return haveSentBitfield;
	}

	public void setHaveSentBitfieldToClient(boolean haveSendBitfield) {
		this.haveSentBitfield = haveSendBitfield;
	}

	public int getPeerIdOfConnectedClient() {
		return peerId;
	}
	public void setPeerIdOfConnectedClient(int peerId) {
		this.peerId = peerId;
	}
	
	public boolean clientIsOptimisticallyUnchoked() {
		return optimisticallyUnchoked;
	}
	public void setClientIsOptimisticallyUnchoked(boolean optimisticallyUnchoked) {
		this.optimisticallyUnchoked = optimisticallyUnchoked;
	}

	public boolean clientIsChoked() {
		return choked;
	}
	public void setClientIsChoked(boolean choked) {
		this.choked = choked;
	}

	public boolean clientIsInterested() {
		return interested;
	}
	public void setClientIsInterested(boolean interested) {
		this.interested = interested;
	}

	public BitSet getClientBitfield() {
		return bitfield;
	}
	public void setClientBitfield(BitSet bitfield) {
		this.bitfield = bitfield;
	}

	public int getFileIndexToSendToClient() {
		return fileIndexToSend;
	}
	public void setFileIndexToSendToClient(int fileIndexToSend) {
		this.fileIndexToSend = fileIndexToSend;
	}
	
	public boolean getHaveReceivedHaveMsgFromClient(){
		return haveReceivedHave;
	}
	public void setHaveReceivedHaveMsgFromClient(boolean b) {
		this.haveReceivedHave = b;	
	}

	public boolean needToUpdatePreferredNeighbors() {
		return needToUpdatePreferredNeighbors;
	}
	public void setNeedToUpdatePreferredNeighbors(boolean needToUpdatePreferredNeighbors) {
		this.needToUpdatePreferredNeighbors = needToUpdatePreferredNeighbors;
	}

	public boolean needToUpdateOptimisticNeighbor() {
		return needToUpdateOptimisticNeighbor;
	}
	public void setNeedToUpdateOptimisticNeighbor(boolean needToUpdateOptimisticNeighbor) {
		this.needToUpdateOptimisticNeighbor = needToUpdateOptimisticNeighbor;
	}

	public boolean needToRespondToClientInterestedStatus() {
		return needToRespondToClientInterestedStatus;
	}

	public void setNeedToRespondToClientInterestedStatus(boolean needToRespondToClientInterestedStatus) {
		this.needToRespondToClientInterestedStatus = needToRespondToClientInterestedStatus;
	}

	public boolean needToRespondToClientRequestForPiece() {
		return needToRespondToClientRequestForPiece;
	}

	public void setNeedToRespondToClientRequestForPiece(boolean needToRespondToClientRequestForPiece) {
		this.needToRespondToClientRequestForPiece = needToRespondToClientRequestForPiece;
	}
}