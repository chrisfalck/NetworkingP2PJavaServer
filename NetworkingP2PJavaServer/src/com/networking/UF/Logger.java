package com.networking.UF;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.common.io.Files;

/**
 * This Logger is a singleton that is intended to localize all logging logic. Any object in the program should be able to obtain the Logger instance and utilized the methods.
 * The Logger.initializeDirectoriesAndFiles method must be called by the Peer object before any other work is done to ensure the application is ready to start.
 * @author Chris Falck
 */
public class Logger {

	/** Directory the jar is run from is established as the project root. */
	private static String currentDirectoryPath = Paths.get(".").toAbsolutePath().normalize().toString();
	
	/** This Peer's int identifier. */
	private static Integer thisPeerIdentifier = null;

	/** File where all logs are appended. */
	private static File thisPeerLogFile = null;
	
	/** Directory where all non-log files are placed. */
	private static File thisPeerFileDirectory = null;
	
	/** Singleton instance. */
	private static Logger instance = null;

	/** Make sure the class can't be instantiated by external objects. */
	private Logger() {}

	/**
	 * Retrieve the singleton object or return a new Logger.
	 * @return Logger
	 */
	public static Logger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}

		return instance;
	}

	/**
	 * Creates all directories and files that will be used throughout the life of the program.
	 * Called once at the start of the program.
	 * @param thisPeerId The id of this peer. Used to initialize static variables and direcotory/file names.
	 * @throws IOException
	 */
	public static void initializeDirectoriesAndFiles(Integer thisPeerId) throws IOException {
		thisPeerIdentifier = thisPeerId;
		// Create a directory at {currentDir}/peer_{peerId}/
		// This directory will hold this peer's non-logging files.
		thisPeerFileDirectory = new File(currentDirectoryPath + File.separator + "peer_" + thisPeerIdentifier.toString());
		File tempFile = new File(thisPeerFileDirectory.getAbsolutePath() + "/tmp.txt");
		Files.createParentDirs(tempFile);

		// Create an empty log file to hold all log messages.
		thisPeerLogFile = new File(currentDirectoryPath + File.separator + "peer_" + thisPeerIdentifier.toString() + ".log");
		Files.touch(thisPeerLogFile);
	}

	/**
	 * Write to the log file that was created in initializeDirectories().
	 * Used by all logging methods.
	 * @param message
	 * @throws IOException
	 */
	private static void writeToLogFile(String message) {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		Date currentTime = new Date();
		String messageWithTimestamp = "\n\n[" + dateFormat.format(currentTime) + "]: " + message + ".";
		try {
			Files.append(messageWithTimestamp, thisPeerLogFile, Charset.defaultCharset());
		} catch (IOException exception) {
			System.err.println("Could not append " + thisPeerLogFile.getAbsolutePath() + " with " + messageWithTimestamp);
		}
	}
	
	/**
	 * Log the creation of a TCP connection to or from another peer.
	 * @param peerTwoId
	 */
	public static void logTCPCreationEvent(Integer peerTwoId, String direction) {
		String message = "";
	
		if (direction.toLowerCase().equals("outgoing")) {
			message = "Peer " + thisPeerIdentifier.toString() + " makes a connection to Peer " + peerTwoId.toString();
		} else if (direction.toLowerCase().equals("incoming")) {
			message = "Peer " + thisPeerIdentifier.toString() + " is connected from Peer " + peerTwoId.toString();
		} else {
			System.err.println("Invalid TCP connectin direction: " + direction);
		}
		
		writeToLogFile(message);
	}
	
	/**
	 * Helper method to turn the preferred neighbors into a comma delimited String.
	 * @param preferredNeighborIds
	 * @return String
	 */
	private static String stringifyPreferredNeighbors(List<Integer> preferredNeighborIds) {
		String preferredNeighborsString = "";
		for (Integer neighborId : preferredNeighborIds) {
			preferredNeighborsString += neighborId.toString() + ", ";
		}

		// Remove the last comma and space.
		preferredNeighborsString = preferredNeighborsString.substring(0, preferredNeighborsString.length() - 2);
		
		return preferredNeighborsString;
	}

	/**
	 * Log a change of preferred neighbors each timeout.
	 * @param preferredNeighborIds
	 */
	public static void logChangeOfPreferredNeighbors(List<Integer> preferredNeighborIds) {
		String preferredNeighborsString = stringifyPreferredNeighbors(preferredNeighborIds);
		
		String message = "Peer " + thisPeerIdentifier.toString() + " has the preferred neighbors" + preferredNeighborsString;
		
		writeToLogFile(message);
	}
	
	/**
	 * Log changes of the optimistically unchoked neighbor.
	 * @param unchokedNeighborId
	 */
	public static void logChangeOfOptimisticallyUnchokedNeighbor(Integer optimisticallyUnchokedNeighborId) {
		String message = "Peer " + thisPeerIdentifier.toString() + " has the optimistically unchoked neighbor " + optimisticallyUnchokedNeighborId.toString();
		writeToLogFile(message);
	}
	
	/**
	 * Log each time this peer is receives an unchoking message from some other peer. 
	 * @param peerTwoId
	 */
	public static void logUnchokingEvent(Integer peerTwoId) {
		String message = "Peer " + thisPeerIdentifier.toString() + " is unchoked by Peer " + peerTwoId.toString();
		writeToLogFile(message);
	}
	
	/**
	 * Log each time this peer is receives a choking message from some other peer. 
	 * @param peerTwoId
	 */
	public static void logChokingEvent(Integer peerTwoId) {
		String message = "Peer " + thisPeerIdentifier.toString() + " is choked by Peer " + peerTwoId.toString();
		writeToLogFile(message);
	}
	
	/**
	 * Log the receipt of a have message from another peer indicating it has the file piece in question. 
	 * @param peerTwoId
	 * @param pieceIndex
	 */
	public static void logReceiptOfHaveMessage(Integer peerTwoId, Integer pieceIndex) {
		String message = "Peer " + thisPeerIdentifier.toString() + " received the 'have' message from Peer " + peerTwoId.toString() + " for the piece " + pieceIndex.toString();
		writeToLogFile(message);
	}
	
	/**
	 * Log the receipt of a message from another peer indicating interest in working together.
	 * @param peerTwoId
	 */
	public static void logReceiptOfInterestedMessage(Integer peerTwoId) {
		String message = "Peer " + thisPeerIdentifier.toString() + " received the 'interested' message from Peer " + peerTwoId.toString();
		writeToLogFile(message);
	}
	
	/**
	 * Log the receipt of a message from another peer indicating no interest in working together.
	 * @param peerTwoId
	 */
	public static void logReceiptOfNotInterestedMessage(Integer peerTwoId) {
		String message = "Peer " + thisPeerIdentifier.toString() + " received the 'not interested' message from Peer " + peerTwoId.toString();
		writeToLogFile(message);
	}
	
	/**
	 * Log the piece number after fully downloading a piece and also log the updated number of pieces this peer now has.
	 * @param peerTwoId
	 * @param pieceIndex
	 * @param currentNumberOfPieces
	 */
	public static void logPieceFullyDownloaded(Integer peerTwoId, Integer pieceIndex, Integer currentNumberOfPieces) {
		String message = "Peer " + thisPeerIdentifier.toString() + " has downloaded the piece " + pieceIndex.toString() + " from Peer " + peerTwoId.toString() + "." +
						 "\nNow the number of pieces it has is " + currentNumberOfPieces.toString();
		writeToLogFile(message);
	}
	
	/**
	 * Log the full download of a file by this peer.
	 */
	public static void logFileFullyDownloaded() {
		String message = "Peer " + thisPeerIdentifier.toString() + " has downloaded the complete file";
		writeToLogFile(message);
	}
	
}















