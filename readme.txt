General timeline for P2P file sharing operation. 
The numbers roughly correspond to what the client and serve
--------------------------------------------------
FileManager: Make sure all files and directories exist. 
			 Create initial bit field and file pieces.
			 If the peer info config indicates this peer has the file, 
			 	set the bit field and file pieces appropriately.

Peer: Start server thread with indicated port number.
	  Send a client thread to each peer indicated on the peer info config excluding the current peer. 

Client: Each client for this peer sends a handshake message to the server it was instantiated with.
Server: Upon receiving a handshake message from a client, the server instantiates a connection state object
		to track that client's connection state with this server. Each client that connects to the server will
		be assigned a connection state object retrievable by peer id. 
		The server then sends a handshake message back to the client. 

Client: When the client receives a return handshake message from the server, it records that a handshake has
		been completed successfully by updating a state variable. 
Server: Waits for bitfield message.

Client: Having completed the handshake, the client sends a bitfield message to the server.
Server: The server receives the bitfield message and updates the client's connection state object. 
		The server then sends a return bitfield message to the client. 
		








