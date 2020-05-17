import java.io.*;
import java.net.*;

public class RichardServerA2{
    public static void main(String args[]) throws Exception{  
        RichardServerA2 udpserver= new RichardServerA2();
        
        try {
            // 1.1.1
            DatagramSocket serverSocket=new DatagramSocket(1000);
            byte[] receiveData=new byte[1024];
            int count=0;
            System.out.println("Server active at localhost:1000");
            DatagramPacket receivePacket=new DatagramPacket(receiveData, receiveData.length);

            while(true){
                serverSocket.receive(receivePacket);
                System.out.println("Packet recieved from client");

                String udpmessage=new String(receivePacket.getData());
                System.out.println("sentence "+udpmessage);
                
                InetAddress address=receivePacket.getAddress();
                int port=receivePacket.getPort();
                
                // 1.1.2
                udpserver.nonStatic(udpmessage,address,port);
                count++;
                System.out.println("Thread count: "+count);    
            }
        }
        
        catch (SocketException e){
          System.err.println("SocketException: Can't open socket");
          e.printStackTrace();
          System.exit(1);
        }
        
        catch (IOException e) {
          System.err.println("IOException: Communication error!");
          e.printStackTrace();
        } 
    }
    
    public void nonStatic(String udpmessage, InetAddress address, int port) {
        Thread t = new Thread(new ServerThread(udpmessage,address,port));
        t.start();  
    }
    
    private class ServerThread implements Runnable{
        String sentence;
        InetAddress address;
        int port;
     
        public ServerThread(String sentence, InetAddress address, int port) {
          this.sentence=sentence;
          this.address=address;
          this.port=port;
        }
    
        public void run() {   
          byte[] sendData=new byte[1024];
          try{
            // 1.1.3
            String threadName = Thread.currentThread().getName();
            String message="Created new thread";
            System.out.format("%s: %s in ClientHandler%n", message, threadName);
            
            long cstarttime = System.currentTimeMillis();
            System.out.println("Client socket start time: " +cstarttime);
            
            DatagramSocket csocket=new DatagramSocket();
            String capitalizedSentence=new String(sentence.toUpperCase());
            sendData=capitalizedSentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            csocket.send(sendPacket);
            
            System.out.println("Thread for IPAddress="+address+" port="+port +" finished.");
            long cendtime = System.currentTimeMillis();
            System.out.println("Thread time="+(cendtime-cstarttime) +"ms");
            csocket.close();
          }
          catch (IOException e) {}
        }
    }
}                

