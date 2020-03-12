import java.io.*;
import java.net.*;
import java.util.*;

class RichardA1TCPServer{

  public static void main(String argv[]) throws Exception{
      //1.1.1
      try(ServerSocket welcomeSocket = new ServerSocket(20120)){
                
        while(true) {
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream  outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
            String clientResponse = inFromClient.readLine();
            System.out.println("Client response: " +clientResponse);
            //1.1.2
            if(clientResponse.equals("request")){
                System.out.println("Sending \"connected\" response.");
                //1.1.3
                outToClient.writeBytes("connected\n");
                clientResponse=inFromClient.readLine();
                System.out.println(clientResponse);   
            }
            else{
              System.out.println("Bad request. Closing connection.");  
              welcomeSocket.close();
            }
        }
      }
      catch(Exception e){
        System.out.println("Encountered Exception: " +e);
      }
    }
}