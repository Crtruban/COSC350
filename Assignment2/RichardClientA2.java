import java.io.*;
import java.net.*;
import java.text.*;

public class RichardClientA2{
    
    public static void main(String[] args) throws IOException{
		//String serverName="localhost";
		String serverName;
		
		try{
			// Process command-line args
			if(args.length==1){
				serverName = args[0];
			}
			else{
				return;
			}
			
			// 1.2.1
			long startTime=System.currentTimeMillis();
			
			//Send Request
			DatagramSocket socket = new DatagramSocket();
			InetAddress address = InetAddress.getByName(serverName);
			byte[] buff = new NtpMessage().toByteArray();
			DatagramPacket packet = new DatagramPacket(buff, buff.length, address, 1000);
			
			// Set the transmit timestamp *just* before sending the packet
			NtpMessage.encodeTimestamp(packet.getData(), 40,(System.currentTimeMillis()/1000.0) + 2208988800.0);
			
			// 1.2.2
			socket.send(packet);
			
			// 1.2.3
			int numBytes = buff.length;
			System.out.println("numBytes: " +numBytes);

			System.out.println("NTP request sent, waiting for response...\n");
			packet = new DatagramPacket(buff, buff.length);
			
			// 1.2.4
			socket.receive(packet);
			double destinationTimestamp = (System.currentTimeMillis()/1000.0) + 2208988800.0;

			// Process response
			NtpMessage msg = new NtpMessage(packet.getData());
			// Corrected, according to RFC2030 errata
			double roundTripDelay = (destinationTimestamp-msg.originateTimestamp) - (msg.transmitTimestamp-msg.receiveTimestamp);
			double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;
			
			// Display response
			System.out.println("NTP server: " + serverName);
			System.out.println(msg.toString());
			System.out.println("Dest. timestamp:     " + NtpMessage.timestampToString(destinationTimestamp));
			System.out.println("Round-trip delay: " + new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");
			System.out.println("Local clock offset: " + new DecimalFormat("0.00").format(localClockOffset*1000) + " ms");

			destinationTimestamp = (destinationTimestamp -2208988800.0) * 1000;
			System.out.println("delay =" +new DecimalFormat("0.00").format(destinationTimestamp-startTime) +" ms");
			System.out.println("datarate =" +new DecimalFormat("0.00").format((numBytes*8)/((destinationTimestamp-startTime)*1000)) +" bps");
			
			socket.close();
		}
        catch (IOException e) {
			System.err.println("IOException: Communication error!");
			e.printStackTrace();
		  } 
	}
}