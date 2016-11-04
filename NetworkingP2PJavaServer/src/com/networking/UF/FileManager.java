package com.networking.UF;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.google.common.io.Files;

public class FileManager {

	private static FileManager instance = null;

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

	/** Directory the jar is run from is established as the project root. */
	private static String currentDirectoryPath = Paths.get(".").toAbsolutePath().normalize().toString();

	/**
	 * Creates all directories and files that will be used throughout the life of the program.
	 * Called once at the start of the program.
	 * @param thisPeerId The id of this peer. Used to initialize static variables and directory/file names.
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
			System.err.println("Make sure both Common.cfg and PeerInfo.cfg exist in the current working directory. Exiting...");
			System.exit(-1);
		}
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
