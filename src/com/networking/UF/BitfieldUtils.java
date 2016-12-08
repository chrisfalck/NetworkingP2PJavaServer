package com.networking.UF;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

import com.google.common.primitives.Ints;
import com.networking.UF.server.ConnectionState;

//TODO: Requested list. 
public class BitfieldUtils {
	
	public static int compareBitfields(BitSet thisBitfield, BitSet otherBitfield) {
		ArrayList<Integer> missingPieceIndices = new ArrayList<Integer>();

		for (int i = 0; i < thisBitfield.size(); ++i) {
			if (thisBitfield.get(i) == false && otherBitfield.get(i) == true) {
				missingPieceIndices.add(i);
			}
		}
		
		if (missingPieceIndices.size() == 0) {
			return -1;
		}
		
		Random generator = new Random(); 
		return missingPieceIndices.get(generator.nextInt(missingPieceIndices.size()));
		
	}
	
	public static ConnectionState updateServerOwnedClientConnectionState(byte[] newPieceIndex, ConnectionState clientConnectionState) {
		clientConnectionState.getBitfield().set(Ints.fromByteArray(newPieceIndex));
		return clientConnectionState;
	}
	
}
