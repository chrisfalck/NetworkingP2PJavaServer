package com.networking.UF.server;

import java.util.BitSet;

public class ConnectionState {
	
	private int peerId;
	private boolean haveReceivedHandshake = false;
	private boolean choked = false;
	private boolean interested = false;
	private BitSet bitfield = null;
	
	public ConnectionState(int peerId) {
		this.peerId = peerId;
	}
	
	public String toString() {
		return new String("Connection state for peerId: " + peerId + "\n" +
						  "haveReceivedHandshake: " + haveReceivedHandshake + "\n" +
						  "choked: " + choked + "\n" +
						  "interested: " + interested + "\n" + 
						  "bitfield size: " + ((bitfield == null) ? "0" : bitfield.size()));
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
	
}
