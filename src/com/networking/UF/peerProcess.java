package com.networking.UF;

import java.io.IOException;
import java.util.Scanner;

import javax.lang.model.element.VariableElement;

import com.networking.UF.Peer;

/**
 * Driver of the program. Creates a Peer with a peer ID provided as the first command line argument.
 * Exits the program if either the peer ID was not provided, or the initial configuration files do not exist.
 */
public class peerProcess {
    public static void main(String[] args) throws IOException {
    	
    	int peerId;

        if (args.length == 0) {
//        	System.out.println("Enter the peer id for this computer:");
//        	Scanner scanner = new Scanner(System.in);
//        	peerId = scanner.nextInt();
//        	scanner.close();
        	peerId = 1001;
        } else {
        	peerId = Integer.parseInt(args[0]);
        }
        
        FileManager fileManager = FileManager.getInstance();
		fileManager.confirmConfigFilesExist();
		fileManager.initializeDirectoriesAndFiles(peerId);
		
        // Check that peerId matches one from PeerList.cfg
		PeerInfoConfig peerInfoConfig = ConfigParser.parsePeerInfoFile();
		boolean foundPeerIdOfSelfInConfig = false;
		for (int i = 0; i < peerInfoConfig.getConfigLength(); ++i) {
			if (peerInfoConfig.getPeerId(i).equals(peerId)) {
				foundPeerIdOfSelfInConfig = true;
				break;
			}
		}
		
		if (!foundPeerIdOfSelfInConfig) {
			System.err.println("This peers peer id was not found in the config file, exiting...");
			System.exit(0);
		}
		
        Peer peer = new Peer(peerId);
        peer.start();
    }
}