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
	private boolean hasReceivedPiece = false;
	private boolean hasReceivedHave = false;
	private int fileIndexToSend = -1;
	private BitSet bitfield = null;
	private boolean waiting = false;
	private long connectionSpeed = 0;
	private boolean needToUpdatePreferredNeighbors = false;
	private boolean needToUpdateOptimisticNeighbor = false;
	
	public String toString() {
		return new String("Connection state for peerId: " + peerId + "\n" +
						  "hasReceivedPiece: " + hasReceivedPiece + "\n" + 
						  "choked: " + choked + "\n" +
						  "optimisticallyUnchoked: " + optimisticallyUnchoked + "\n" + 
						  "connectionSpeed: " + connectionSpeed + "\n" + 
						  "interested: " + interested + "\n" + 
						  "fileIndexToSend: " + fileIndexToSend + "\n" +
						  "hasReceivedHave: " + hasReceivedHave + "\n" +
						  "waiting: " + waiting + "\n" + 
						  "bitfield size: " + ((bitfield == null) ? "0" : bitfield.size()));
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

	public long getClientConnectionSpeed() {
		return connectionSpeed;
	}
	public void setClientConnectionSpeed(long connectionSpeed) {
		this.connectionSpeed = connectionSpeed;
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
		return hasReceivedHave;
	}
	public void setHaveReceivedHaveMsgFromClient(boolean b) {
		this.hasReceivedHave = b;	
	}

	public boolean serverShouldWaitForMsgFromClient() {
		return waiting;
	}
	public void setServerShouldWaitForMsgFromClient(boolean waiting) {
		this.waiting = waiting;
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
}