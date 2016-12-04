package com.networking.UF.server;

import java.util.BitSet;

public class ConnectionState {
	
	private int peerId;
	private boolean haveReceivedHandshake = false;
	private boolean haveReceivedBitfield = false;
	private boolean choked = true;
	private boolean interested = false;
	private boolean hasReceivedPiece = false;
	private BitSet bitfield = null;
	private int connectionSpeed = 0;
	
	public int getConnectionSpeed() {
		return connectionSpeed;
	}

	public void setConnectionSpeed(int connectionSpeed) {
		this.connectionSpeed = connectionSpeed;
	}

	public ConnectionState(int peerId) {
		this.peerId = peerId;
	}
	
	public String toString() {
		return new String("Connection state for peerId: " + peerId + "\n" +
						  "haveReceivedHandshake: " + haveReceivedHandshake + "\n" +
						  "haveReceivedBitfield " + haveReceivedBitfield + "\n" +
						  "choked: " + choked + "\n" +
						  "interested: " + interested + "\n" + 
						  "bitfield size: " + ((bitfield == null) ? "0" : bitfield.size()));
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
