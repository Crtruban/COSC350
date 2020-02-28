import java.io.*;
import java.net.*;

class RichardA1TCPClient {
	public static final String IP = "localhost";
	public static final int PORT = 20120;

	public static void main(String argv[]) throws Exception {
		String messageFromServer, website, ip, lineFromWeb;
		long startTime, endTime, totalTime;

		// 1.1.1 - Make a TCP socket connection to localhost on port 20120
		Socket clientSocket = new Socket(IP, PORT);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		// 1.1.2 - Send message "request" to the server
		outToServer.writeBytes("request\n");

		// 1.1.3 - Print the message “local host connected” when connected
		messageFromServer = inFromServer.readLine();
		if (messageFromServer.equals("connected"))
			System.out.println("local host connected");

		// 1.1.4 - Ask the user to enter a web server name
		System.out.println("Enter a web server name using the format www.name.suf");
		website = inFromUser.readLine();

		// 1.1.8 - Starting the timer
		startTime = System.currentTimeMillis();

		// 1.1.5 - Connect to the web server www.name.suf on port 443
		URL url = new URL("https://" + website);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		// 1.1.6 - Print the IP address of the web server
		ip = InetAddress.getByName(url.getHost()).getHostAddress();
		System.out.println("IP Address: " + ip);

		// 1.1.7 - Print each line of page text received from the web server
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		while ((lineFromWeb = in.readLine()) != null)
			System.out.println(lineFromWeb);

		// 1.1.8 - Stopping the time and print the delay time
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("delay=" + totalTime);

		// 1.1.9 - Send the IP and delay time
		outToServer.writeBytes(ip + "\n");
		outToServer.writeBytes(totalTime + "\n");

		// 1.1.10 - Print the message "done"
		System.out.println("done");

		con.disconnect();
		clientSocket.close();

	}
}

