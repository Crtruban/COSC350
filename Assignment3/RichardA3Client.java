import java.util.Scanner;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.Socket;

public class RichardA3Client {
	public static void main(String[] args) throws Exception {
		Scanner fileIn=null;
		int[][] values;
		int size;
		String message="";
		Socket clientSocket = new Socket("localhost", 6789);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
		// Insert a Scanner in dvr.txt
		try {
			fileIn=new Scanner(new File("dvr.txt"));
		} catch (FileNotFoundException e) {
			System.err.println("--File not Found--");
			System.exit(-1);
		}
		
		// Determine the amount of DVR messages
		size=fileIn.nextInt();
		values=new int[2][size];
		
		// Read and store the destinations
		for(int i=0;i<size;i++)
			values[0][i]=fileIn.nextInt();
		
		// Read and store the distance
		for(int i=0;i<size;i++)
			values[1][i]=fileIn.nextInt();
		
		// Create the message
		message=message+""+size+" ";
		for(int i=0;i<size;i++) {
			message+="("+values[0][i]+", "+values[1][i]+")";
			if(i!=size-1)
				message+=", ";
		}
		
		// Send the message to local host listening on port 6789
		outToServer.writeBytes(message+"\n");
	}
}
