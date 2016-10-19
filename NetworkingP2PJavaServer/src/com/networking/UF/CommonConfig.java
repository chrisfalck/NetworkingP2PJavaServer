package com.networking.UF;

public class CommonConfig {

	public Integer numberOfPreferredNeighbors = 0;
	public Integer unchokingInterval = 0;
	public Integer optimisticUnchokingInterval = 0;
	public String fileName = "";
	public Integer fileSize = 0;
	public Integer pieceSize = 0;

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
