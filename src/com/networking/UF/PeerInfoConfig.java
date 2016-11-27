package com.networking.UF;

import java.util.ArrayList;
import java.util.HashMap;

public class PeerInfoConfig {
	/** An ArrayList where each index corresponds to a line of the file.
	 *  At each index, a HashMap contains specific information parsed from the line. */
	private ArrayList<HashMap<String, String>> fileLines = new ArrayList<HashMap<String, String>>();
	
	public PeerInfoConfig(ArrayList<HashMap<String, String>> fileLines) {
		this.fileLines = fileLines;
	}
	
	public Integer getConfigLength() {
		return this.fileLines.size();
	}
	
	public Integer getPeerId(Integer lineNumber) {
		HashMap<String, String> line = fileLines.get(lineNumber);
		return Integer.valueOf(line.get("peerId"));
	}
	
	public String getHostName(Integer lineNumber) {
		HashMap<String, String> line = fileLines.get(lineNumber);
		return line.get("hostName");
	}
	
	public Integer getListeningPort(Integer lineNumber) {
		HashMap<String, String> line = fileLines.get(lineNumber);
		return Integer.valueOf(line.get("listeningPort"));
	}
	
	public Boolean getHasFileOrNot(Integer lineNumber) {
		HashMap<String, String> line = fileLines.get(lineNumber);
		if (line.get("hasFileOrNot").equals("1")) {
			return new Boolean(true);
		} else {
			return new Boolean(false);
		}
	}
	
	
}
