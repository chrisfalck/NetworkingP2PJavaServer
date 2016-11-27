package com.networking.UF;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.io.Files;

public class ConfigParser {
	
	private ConfigParser(){}
	
	private static String commonConfigPath = Paths.get(".").toAbsolutePath().normalize().toString() + "/Common.cfg";
	private static String peerInfoPath = Paths.get(".").toAbsolutePath().normalize().toString() + "/PeerInfo.cfg";
	
	public static CommonConfig parseCommonFile() throws IOException {
		Integer numberOfPreferredNeighbors = 0;
		Integer unchokingInterval = 0;
		Integer optimisticUnchokingInterval = 0;
		String fileName = "";
		Integer fileSize = 0;
		Integer pieceSize = 0;

		List<String> fileLines = Files.readLines(new File(commonConfigPath), Charset.defaultCharset());
		
		numberOfPreferredNeighbors = Integer.valueOf(fileLines.get(0).split(" ")[1]);
		unchokingInterval = Integer.valueOf(fileLines.get(1).split(" ")[1]);
		optimisticUnchokingInterval = Integer.valueOf(fileLines.get(2).split(" ")[1]);
		fileName = fileLines.get(3).split(" ")[1];
		fileSize = Integer.valueOf(fileLines.get(4).split(" ")[1]);
		pieceSize = Integer.valueOf(fileLines.get(5).split(" ")[1]);
		
		CommonConfig commonConfig = new CommonConfig(numberOfPreferredNeighbors, unchokingInterval, optimisticUnchokingInterval, fileName, fileSize, pieceSize);
		return commonConfig;
	}
	
	public static PeerInfoConfig parsePeerInfoFile() throws IOException {
		List<String> fileLines = Files.readLines(new File(peerInfoPath), Charset.defaultCharset());
		
		ArrayList<HashMap<String, String>> parsedLines = new ArrayList<HashMap<String, String>>();
		
		for(String line : fileLines) {
			String[] linePieces = line.split(" ");
			HashMap<String, String> parsedLine = new HashMap<String, String>();
			parsedLine.put("peerId", linePieces[0]);
			parsedLine.put("hostName", linePieces[1]);
			parsedLine.put("listeningPort", linePieces[2]);
			parsedLine.put("hasFileOrNot", linePieces[3]);
			parsedLines.add(parsedLine);
		}
	
		return new PeerInfoConfig(parsedLines);
	}
	
}
