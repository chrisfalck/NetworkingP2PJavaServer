package com.networking.UF.server;

import java.util.BitSet;

public class ConnectionState {
	
	private int peerId;

	private boolean haveReceivedHandshake = false;
	private boolean haveReceivedBitfield = false;
	private boolean choked = true;
	private boolean optimisticallyUnchoked = false;
	private boolean interested = false;
	private boolean hasReceivedPiece = false;
	private BitSet bitfield = null;
	private int connectionSpeed = 0;
	
	public String toString() {
		return new String("Connection state for peerId: " + peerId + "\n" +
						  "haveReceivedHandshake: " + haveReceivedHandshake + "\n" +
						  "hasReceivedPiece: " + hasReceivedPiece + "\n" + 
						  "haveReceivedBitfield " + haveReceivedBitfield + "\n" +
						  "choked: " + choked + "\n" +
						  "optimisticallyUnchoked: " + optimisticallyUnchoked + "\n" + 
						  "connectionSpeed: " + connectionSpeed + "\n" + 
						  "interested: " + interested + "\n" + 
						  "bitfield size: " + ((bitfield == null) ? "0" : bitfield.size()));
	}
	
	public int getPeerId() {
		return peerId;
	}

	public void setPeerId(int peerId) {
		this.peerId = peerId;
	}
	
	public boolean isOptimisticallyUnchoked() {
		return optimisticallyUnchoked;
	}

	public void setOptimisticallyUnchoked(boolean optimisticallyUnchoked) {
		this.optimisticallyUnchoked = optimisticallyUnchoked;
	}

	public int getConnectionSpeed() {
		return connectionSpeed;
	}

	public void setConnectionSpeed(int connectionSpeed) {
		this.connectionSpeed = connectionSpeed;
	}

	public ConnectionState(int peerId) {
		this.peerId = peerId;
	}
	
	public boolean haveReceivedBitfield() {
		return haveReceivedBitfield;
	}
	public void setHaveReceivedBitfield(boolean haveReceivedBitfield) {
		this.haveReceivedBitfield = haveReceivedBitfield;
	}
	public boolean haveReceivedHandshake() {
		return haveReceivedHandshake;
	}
	public void setHaveReceivedHandshake(boolean haveReceivedHandshake) {
		this.haveReceivedHandshake = haveReceivedHandshake;
	}
	public boolean isChoked() {
		return choked;
	}
	public void setChoked(boolean choked) {
		this.choked = choked;
	}
	public boolean isInterested() {
		return interested;
	}
	public void setInterested(boolean interested) {
		this.interested = interested;
	}
	public BitSet getBitfield() {
		return bitfield;
	}
	public void setBitfield(BitSet bitfield) {
		this.bitfield = bitfield;
	}
	public boolean getHasReceivedPiece() {
		return hasReceivedPiece;
	}
	public void setHasReceivedPiece(boolean hasReceivedPiece) {
		this.hasReceivedPiece = hasReceivedPiece;
	}
	
}
