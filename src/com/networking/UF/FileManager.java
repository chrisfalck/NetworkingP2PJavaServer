package com.networking.UF;

import java.awt.List;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import javax.lang.model.element.VariableElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;

import com.google.common.io.Files;
import com.google.common.primitives.Bytes;

public class FileManager {

	private static FileManager instance = null;
	
	/** The array of bits that contains information about what file parts this Peer has. */
	private BitSet bitfield = null;
	
	public synchronized BitSet getBitfield() {
		return bitfield;
	}
	
	public synchronized void setBitfiled(BitSet newBitField) {
		this.bitfield = newBitField;
	}
	
	/** The actual file pieces to transfer. 
	* We know what indices are valid by checking the bitfield.
	* The size of the byte array at each index is (FileSize / PieceSize) with the possible exception of the last index.
	* If the above line does not yield an even number, the size of the final array in filePieces is (FileSize % PieceSize).
	*/
	private byte[][] filePieces = null;

	private FileManager(){}

	public static FileManager getInstance() {
		if (instance == null) {
			instance = new FileManager();
		}

		return instance;
	}

	/** This Peer's int identifier. */
	private static Integer thisPeerIdentifier = null;

	/** File where all logs are appended. */
	private static File thisPeerLogFile = null;

	/** Directory where all non-log files are placed. */
	private static File thisPeerFileDirectory = null;
	
	private static File thisPeerDataFile = null;

	/** Directory the jar is run from is established as the project root. */
	private static String currentDirectoryPath = Paths.get(".").toAbsolutePath().normalize().toString();
	
	public String toString() {
		String currentState = thisPeerIdentifier + "\n" + thisPeerLogFile + "\n" + thisPeerFileDirectory + "\n" + currentDirectoryPath;
		return currentState;
	}

	/**
	 * Creates all directories and files that will be used throughout the life of the program.
	 * Called once at the start of the program.
	 * @param thisPeerId The id of this peer. Used to initialize static variables and direcotory/file names.
	 * @throws IOException
	 */
	public void initializeDirectoriesAndFiles(Integer thisPeerId) throws IOException {
		thisPeerIdentifier = thisPeerId;
		
		// Create a directory at {currentDir}/peer_{peerId}/
		// This directory will hold this peer's non-logging files.
		thisPeerFileDirectory = new File(currentDirectoryPath + File.separator + "peer_" + thisPeerIdentifier.toString());
		File tempFile = new File(thisPeerFileDirectory.getAbsolutePath() + "/tmp.txt");
		Files.createParentDirs(tempFile);

		// Create an empty log file to hold all log messages.
		thisPeerLogFile = new File(currentDirectoryPath + File.separator + "peer_" + thisPeerIdentifier.toString() + ".log");
		Files.touch(thisPeerLogFile);
		
		handleSourceFileCreation();
	}
	
	/**
	 * Checks if the peer has the completed source file upon program initiation, and then makes sure a file
	 * (empty or full) exists with the appropriate name read in from common config in the peer's file directory.
	 * @throws IOException
	 */
	private void handleSourceFileCreation() throws IOException {
		PeerInfoConfig peerInfoConfig = ConfigParser.parsePeerInfoFile();
		CommonConfig commonConfig = ConfigParser.parseCommonFile();
		
		// Check if the peer info file indicates that this peer has a complete file. 
		boolean peerHasCompletedSourceFile = false;
		for (int i = 0; i < peerInfoConfig.getConfigLength(); ++i) {
			boolean reachedCurrentPeerConfigLine = thisPeerIdentifier.equals(peerInfoConfig.getPeerId(i));
			if (reachedCurrentPeerConfigLine && peerInfoConfig.getHasFileOrNot(i)) {
				peerHasCompletedSourceFile = true;
				break;
			}
		}
		
		// Make sure this Peer's data file exists, even if it's going to be empty.
		String dataFileName = commonConfig.getFileName();
		thisPeerDataFile = new File(getThisPeerFileDirectory(), dataFileName);
		Files.touch(thisPeerDataFile);
		
		initializeFilePiecesAndBitfield(peerHasCompletedSourceFile);
		
	}
	
	/**
	 * Initializes the bitfield for this peer.
	 * Initializes the filePieces for this peer. 
	 * If a file exists already, the file will be split and placed into filePieces.
	 * NOTE: bitfield.size() does not represent the number of filePieces accurately. 
	 * filePieces.length must be used for the correct number of filePieces.
	 * @param peerHasCompletedSourceFile
	 * @throws IOException
	 */
	private void initializeFilePiecesAndBitfield(boolean peerHasCompletedSourceFile) throws IOException {
		CommonConfig commonConfig = ConfigParser.parseCommonFile();

		double fileSize = (double)commonConfig.getFileSize();
		double normalFilePieceSize = (double)commonConfig.getPieceSize();
		
		// Math.ceil because we'll use this as the size for the constructor of our filePieces array.
		// We need an additional index even if the last piece is partial. 
		int numberOfFilePieces = (int)(Math.ceil(fileSize / normalFilePieceSize));
		
		// Figure out the number of bytes in the last file piece.
		int lastFilePieceSize;
		if ((fileSize % normalFilePieceSize) != 0) {
			lastFilePieceSize = (int)(fileSize % normalFilePieceSize);
		} else lastFilePieceSize = (int)normalFilePieceSize;

		// Default bitfield init.
		bitfield = new BitSet(numberOfFilePieces);
		
		// Default filePieces init.
		filePieces = new byte[numberOfFilePieces][];
		for (int i = 0; i < filePieces.length; ++i) {
			if (i == (filePieces.length - 1)) filePieces[i] = new byte[lastFilePieceSize];
			else filePieces[i] = new byte[(int)normalFilePieceSize];
		}
		
		// If this peer doesn't have the source file, we're done.
		if (!peerHasCompletedSourceFile) return;
		
		// Otherwise we need to parse the file into the filePieces array for distribution by our peer. 
		populateFilePieces();
	}
	
	private void populateFilePieces() throws IOException {
		CommonConfig commonConfig = ConfigParser.parseCommonFile();
		byte[] wholeFile = new byte[commonConfig.fileSize];
		
		// Place the whole file's content in a byte array.
		DataInputStream fileIn = new DataInputStream(new FileInputStream(FileManager.thisPeerDataFile));
		fileIn.readFully(wholeFile);
		fileIn.close();
		
		// Set all bits corresponding to file pieces to true because we have all the file pieces. 
		for (int i = 0; i < filePieces.length; i++) {
			bitfield.set(i);
		}
		
		// Populate the filePieces from the file. 
		int locationInWholeFile = 0;
		for (byte[] filePiece : filePieces) {
			for (int i = 0; i < filePiece.length; ++i, ++locationInWholeFile) {
				filePiece[i] = wholeFile[locationInWholeFile];
			}
		}
		
//		for (byte character : filePieces[10]) {
//			System.out.println((char)character + "");
//		}
//		
//		for (int i = 0; i < bitfield.size(); i++) {
//			System.out.print(bitfield.get(i) ? "1" : "0");
//		}
	}
	
	public void reconstructFile() throws IOException {
		byte[] wholeFile = Bytes.concat(filePieces);
		Files.write(wholeFile, thisPeerDataFile);
	}
	
	/**
	 * Checks whether or not the required config files, Common.cfg and PeerInfo.cfg, exist in the current working directory
	 * Called once at the start of the program
	 * @throw IOException
	 */
	public void confirmConfigFilesExist() throws IOException {
		boolean commonExists = new File(currentDirectoryPath, "Common.cfg").exists();
		boolean peerInfoExists = new File(currentDirectoryPath, "PeerInfo.cfg").exists();
		if (!commonExists || !peerInfoExists) {
			System.err.println("Error initializing files and directories.");
			System.err.println("Make sure both Common.cfg and PeerInfo.cfg exist in "
					+ " the current working directory.\n" + currentDirectoryPath +"\nExiting...");
			System.exit(-1);
		} else {
			System.out.println("Config files found.");
		}
	} 
	
	public void addFilePiece(int index, byte[] content){
		filePieces[index] = content;
		bitfield.set(index);
	}
	
	public int getLengthOfFilePieces() {
		int count = 0;
		for (byte[] piece: filePieces) {
			if (piece.length > 0) ++count;
		}
		return count;
	}

	public byte[] getFilePieceAtIndex(int index) {
		return filePieces[index];
	}

	public Integer getThisPeerIdentifier() {
		return thisPeerIdentifier;
	}

	public File getThisPeerLogFile() {
		return thisPeerLogFile;
	}

	public File getThisPeerFileDirectory() {
		return thisPeerFileDirectory;
	}

	public String getCurrentDirectoryPath() {
		return currentDirectoryPath;
	}

}
