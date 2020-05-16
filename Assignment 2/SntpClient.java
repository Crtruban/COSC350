package Assignment2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;

/**
 * NtpClient - an NTP client for Java.  This program connects to an NTP server
 * and prints the response to the console.
 * 
 * The local clock offset calculation is implemented according to the SNTP
 * algorithm specified in RFC 2030.  
 * 
 * Note that on windows platforms, the curent time-of-day timestamp is limited
 * to an resolution of 10ms and adversely affects the accuracy of the results.
 * 
 * 
 * This code is copyright (c) Adam Buckley 2004
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.  A HTML version of the GNU General Public License can be
 * seen at http://www.gnu.org/licenses/gpl.html
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *  
 * @author Adam Buckley
 */
public class SntpClient
{
	public static void main(String[] args) throws IOException
	{
		String serverName = "localhost";
		
		// 1.2.1 - Stores the current local time startTime by using the system clock
		double startTime = System.currentTimeMillis();
		
		// Send request
		DatagramSocket socket = new DatagramSocket();
		InetAddress address = InetAddress.getByName(serverName);
		byte[] buf = new NtpMessage().toByteArray();
		DatagramPacket packet =
			new DatagramPacket(buf, buf.length, address, 1000); // port 1000
		
		NtpMessage.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);
		
		// 1.2.2 - Sends the NTP request to the localhost server listening on port 1000
		socket.send(packet);
		
		// 1.2.3 - Counts the number of bytes numBytes in the NTP request sent over the socket to the server
		int numBytes = buf.length;
		
		// 1.2.4 - Receives the NTP response, stores the current local time in destinationTimestamp and prints relevant time values
		// Get response
		System.out.println("NTP request sent, waiting for response...\n");
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		
		// Immediately record the incoming timestamp
		double destinationTimestamp = (System.currentTimeMillis() / 1000) + 2208988800.0;
		
		// Process response
		NtpMessage msg = new NtpMessage(packet.getData());
		
		// Corrected, according to RFC2030 errata
		double roundTripDelay = (destinationTimestamp - msg.originateTimestamp) -
			(msg.transmitTimestamp-msg.receiveTimestamp);
			
		double localClockOffset =
			((msg.receiveTimestamp - msg.originateTimestamp) +
			(msg.transmitTimestamp - destinationTimestamp)) / 2;
		
		// Display response
		System.out.println("NTP server: " + serverName);
		System.out.println(msg.toString());
		
		System.out.println("Dest. timestamp:     " +
			NtpMessage.timestampToString(destinationTimestamp));
		
		System.out.println("Round-trip delay: " +
			new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");
		
		System.out.println("Local clock offset: " +
			new DecimalFormat("0.00").format(localClockOffset*1000) + " ms");
		
		destinationTimestamp = (destinationTimestamp - 2208988800.0) * 1000;
		
		// 1.2.5 - Prints “delay=” followed by the value destinationTimestamp-startTime in milliseconds
		System.out.println("delay=" + new DecimalFormat("0.00").format(destinationTimestamp - startTime) + " ms");
		
		// 1.2.6 - Prints “datarate=” followed by the value numBytes/(destinationTimestamp-startTime) in bits per second
		System.out.println("datarate=" + new DecimalFormat("0.00000").format(numBytes / ((destinationTimestamp - startTime) * 1000)) + " bytes/sec");
		
		socket.close();
	}
	
	// I'm still receiving incorrect values for "delay" and "datarate" (both negative),
	// but all the directions were followed and everything else prints out correctly.
	
	
	
	/**
	 * Prints usage
	 */
	static void printUsage()
	{
		System.out.println(
			"NtpClient - an NTP client for Java.\n" +
			"\n" +
			"This program connects to an NTP server and prints the response to the console.\n" +
			"\n" +
			"\n" +
			"Usage: java NtpClient server\n" +
			"\n" +
			"\n" +
			"This program is copyright (c) Adam Buckley 2004 and distributed under the terms\n" +
			"of the GNU General Public License.  This program is distributed in the hope\n" +
			"that it will be useful, but WITHOUT ANY WARRANTY; without even the implied\n" +
			"warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU\n" +
			"General Public License available at http://www.gnu.org/licenses/gpl.html for\n" +
			"more details.");
	}
}
