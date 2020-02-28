package package1;

import java.io.*;
import java.net.*;

class RichardA1TCPServer {
	public static final int PORT = 20120;

	public static void main(String argv[]) throws Exception {
		String clientMessage;

		Socket connectionSocket;
		BufferedReader inFromClient;
		DataOutputStream outToClient;

		// 1.2.1 - Listen for and accept a connection on port 20120
		ServerSocket welcomeSocket = new ServerSocket(PORT);

		while (true) {
			connectionSocket = welcomeSocket.accept();

			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			// 1.2.2 - Send the message "connected" when it gets the message "request"
			clientMessage = inFromClient.readLine();
			if (clientMessage.equals("request"))
				outToClient.writeBytes("connected\n");

			// 1.2.3 - Print the IP address and delay time
			clientMessage = inFromClient.readLine();
			System.out.print("ip address=" + clientMessage);
			clientMessage = inFromClient.readLine();
			System.out.println(" delay=" + clientMessage);
		}
	}
}

