import java.io.*;
import java.net.*;
import java.util.*;

public class UDPServerThreads {

  public class UDPClientHandler1 implements Runnable {
   
   String sentence;
   InetAddress address;
   int port;
 
   public UDPClientHandler1(String sentence, InetAddress address, int port) {
    this.sentence=sentence;
    this.address=address;
    this.port=port;
   }
   
    public void run() {   
     byte[] sendData=new byte[1024];
     try{
      String threadName =
      Thread.currentThread().getName();
      String message="in HandleClient";
      System.out.format("%s: %s%n", threadName, message);
      long cstarttime = System.currentTimeMillis();
      System.out.println("before csocket");
      DatagramSocket csocket=new DatagramSocket();
      String capitalizedSentence=new String(sentence.toUpperCase());
      sendData=capitalizedSentence.getBytes();
      DatagramPacket sendPacket=
        new DatagramPacket(sendData, sendData.length, address, port);
      csocket.send(sendPacket);
      System.out.println("after send in thread "+"IPAddress="+address+" port="+port);
      long cendtime = System.currentTimeMillis();
      System.out.println("time="+(cendtime-cstarttime));
     }
     catch (IOException e) {}
    }
 }

  public void nonStatic(String udpmessage, InetAddress address, int port) {
   Thread t = new Thread(new UDPClientHandler1(udpmessage,address,port));
     t.start();  
  }
  
 public static void main(String args[]) throws Exception
  {  
    UDPServerThreads udpserver= new UDPServerThreads();
    UDPServerThreads assignment2 = new UDPServerThreads();
    try {
    	// Altered the port from 9876 to 1000
     DatagramSocket serverSocket=new DatagramSocket(1000);
     byte[] receiveData=new byte[1024];
     int count=0;
     while(true)
     {
      DatagramPacket receivePacket=
       new DatagramPacket(receiveData, receiveData.length);
      serverSocket.receive(receivePacket);
      System.out.println("after rcv in server");
      String udpmessage=new String(receivePacket.getData());
      System.out.println("sentence"+udpmessage);
      InetAddress address=receivePacket.getAddress();
      int port=receivePacket.getPort();
      //UDPServer is the default, if doesn't work delete comment '//'
     // udpserver.nonStatic(udpmessage,address,port);
      assignment2.nonStatic(udpmessage, address, port);
      count++;
      System.out.println("after start thread"+count);    
     }
   }
   catch (IOException e) {} 
 }
}