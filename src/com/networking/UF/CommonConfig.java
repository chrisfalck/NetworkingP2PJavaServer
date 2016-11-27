package com.networking.UF;

public class CommonConfig {

	public Integer numberOfPreferredNeighbors = 0;
	public Integer unchokingInterval = 0;
	public Integer optimisticUnchokingInterval = 0;
	public String fileName = "";
	public Integer fileSize = 0;
	public Integer pieceSize = 0;

	/**
	 * The CommonConfig object is created by parsing the Common.cfg file. 
	 * It isn't static, but all instances of the object should contain the same information
	 * because they're being created from a static file. 
	 * @param numberOfPreferredNeighbors
	 * @param unchokingInterval
	 * @param optimisticUnchokingInterval
	 * @param fileName
	 * @param fileSize
	 * @param pieceSize
	 */
	public CommonConfig(Integer numberOfPreferredNeighbors, Integer unchokingInterval,
			Integer optimisticUnchokingInterval, String fileName, Integer fileSize, Integer pieceSize) 
	{
		this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
		this.unchokingInterval = unchokingInterval;
		this.optimisticUnchokingInterval = optimisticUnchokingInterval;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.pieceSize = pieceSize;
	}
	
	public Integer getNumberOfPreferredNeighbors() {
		return numberOfPreferredNeighbors;
	}

	public Integer getUnchokingInterval() {
		return unchokingInterval;
	}

	public Integer getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}

	public String getFileName() {
		return fileName;
	}

	public Integer getFileSize() {
		return fileSize;
	}

	public Integer getPieceSize() {
		return pieceSize;
	}

}
