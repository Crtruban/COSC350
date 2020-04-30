import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;

public class SntpClient {
	public static void main(String[] args) throws IOException {
		String serverName = "localhost";

		// 1.2.1 - Store the current local time by using the system clock
		long startTime = System.currentTimeMillis();

		DatagramSocket socket = new DatagramSocket();
		InetAddress address = InetAddress.getByName(serverName);
		byte[] buf = new NtpMessage().toByteArray();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 1000);
		NtpMessage.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);
		// 1.2.2 - Send the NTP request to the localhost server listening on port 1000
		socket.send(packet);

		// 1.2.3 - Count the number of bytes in the NTP request sent over the socket to the server
		int numBytes = buf.length;

		System.out.println("NTP request sent, waiting for response...\n");
		packet = new DatagramPacket(buf, buf.length);
		// 1.2.4 - Receives the NTP response
		socket.receive(packet);

		// 1.2.4 - Store the current local time in destTimestamp
		double destTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;

		// Process response
		NtpMessage msg = new NtpMessage(packet.getData());
		double roundTripDelay = (destTimestamp - msg.orgTimestamp) - (msg.tranTimestamp - msg.rcvTimestamp);
		double localClockOffset = ((msg.rcvTimestamp - msg.orgTimestamp) + (msg.tranTimestamp - destTimestamp)) / 2;

		// 1.2.4 - Print the relevant time values
		System.out.println("NTP server: " + serverName);
		System.out.println(msg.toString());
		System.out.println("Dest. timestamp:     " + NtpMessage.timestampToString(destTimestamp));
		System.out.println("Round-trip delay: " + new DecimalFormat("0.00").format(roundTripDelay * 1000) + " ms");
		System.out.println("Local clock offset: " + new DecimalFormat("0.00").format(localClockOffset * 1000) + " ms");

		// 1.2.5 - Print “delay=” destinationTimestamp-startTime in milliseconds
		destTimestamp = (destTimestamp - 2208988800.0) * 1000;
		double delay = destTimestamp - startTime;
		System.out.println("delay=" + new DecimalFormat("0.00").format(delay) + " ms");

		// 1.2.6 - Print "datarate=" numBytes/(destTimestamp-startTime) in bits per second
		double datarate = (numBytes * 8) / (delay * 1000);
		System.out.println("datarate=" + new DecimalFormat("0.00").format(datarate) + " bits/sec");

		socket.close();
	}
}