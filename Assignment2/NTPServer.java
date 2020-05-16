import java.net.*;  
import java.io.*;  
import java.util.*;  
import java.lang.*;  
  
public class NTPServer {  
      
   private int NTPPort = 1000;  
   private byte[] NTPData = new byte[48];      
//   byte[] NTPData2;   
        private long seventyOffset;    //offset (in ms) between 1900 and 1970  
        private long transmitMillis;  
  
    //Offsets in NTPData for each timestamp  
        private final byte referenceOffset = 16;  
        private final byte originateOffset = 24;  
        private final byte receiveOffset = 32;  
        private final byte transmitOffset = 40;  
        private long transmitTimestamp;  
          
          
     
   private DatagramPacket NTPPacket;  
   private DatagramSocket NTPSocket;  
     
   public NTPServer(){  
     try{  
       System.out.println("Server started!");    
                           
            seventyOffset = 70*365; //days in 70 years  
            seventyOffset += 17;    //add days for leap years between 1900 and 1970  
            seventyOffset *= 24;    //hours in a day  
            seventyOffset *= 60;    //minutes in an hour  
            seventyOffset *= 60;    //seconds in a minute  
            seventyOffset *= 1000;  //milliseconds in a second    
              
       NTPPacket = new DatagramPacket(NTPData,NTPData.length);        
       NTPSocket = new DatagramSocket(NTPPort);  
                             
       while(true){           
         NTPSocket.receive(NTPPacket);  
  
  
//        if(NTPPacket.getPort() == NTPPort)   
//          {  
             String rcvd="from ddress:"+NTPPacket.getAddress()+  
                         ",port:"+NTPPacket.getPort();  
             System.out.println(rcvd);                
             NTPData = NTPPacket.getData();   
             transmitTimestamp = toLong(transmitOffset);                  
             initPacket();  
             DatagramPacket echo = new DatagramPacket(NTPData,NTPData.length,NTPPacket.getAddress(),NTPPacket.getPort());  
             NTPSocket.send(echo);  
//           }  
                  }  
           }    
                  
     catch(SocketException e){  
         System.err.println("Can't open socket");  
        System.exit(1);  
                             }  
                               
       catch(IOException e){  
         System.err.println("Communication error!");  
         e.printStackTrace();  
       }  
      }   
  
               private void setOrigTime()  
                 {  
                      toBytes(transmitTimestamp,originateOffset);             
                    }  
   
               private void setReferenceTime()  
                   {  
                    toBytes(transmitTimestamp,referenceOffset);   
                    }  
                private void setReceiveTime()  
                    {  
                            GregorianCalendar startCal = new GregorianCalendar();  
                            long startMillis = startCal.getTimeInMillis();  
                            transmitMillis = startMillis + seventyOffset;  
                            toBytes(transmitMillis,receiveOffset);  
                    }  
                
                  
                private void setTransmitTime()  
                    {  
                            GregorianCalendar startCal = new GregorianCalendar();  
                            long startMillis = startCal.getTimeInMillis();  
                            transmitMillis = startMillis + seventyOffset;  
                            toBytes(transmitMillis,transmitOffset);  
                    }  
                
              
                public void toBytes(long n, int offset)  
                {  
                    long intPart = 0;  
                    long fracPart = 0;  
                    intPart = n/1000;  
                    fracPart = ((n % 1000) / 1000) * 0X100000000L;  
              
                    NTPData[offset+0] = (byte)(intPart >>> 24);  
                    NTPData[offset+1] = (byte)(intPart >>> 16);  
                    NTPData[offset+2] = (byte)(intPart >>> 8);  
                    NTPData[offset+3] = (byte)(intPart);  
              
                    NTPData[offset+4] = (byte)(fracPart >>> 24);  
                            NTPData[offset+5] = (byte)(fracPart >>> 16);  
                            NTPData[offset+6] = (byte)(fracPart >>> 8);  
                            NTPData[offset+7] = (byte)(fracPart);  
              
                }  
              
                public long toLong(int offset)   
                {  
              
                long intPart = ((((long) NTPData[offset+3]) & 0xFF)) +  
                            ((((long) NTPData[offset+2]) & 0xFF) << 8) +  
                            ((((long) NTPData[offset+1]) & 0xFF) << 16) +  
                            ((((long) NTPData[offset+0]) & 0xFF) << 24);  
              
                long fracPart = ((((long) NTPData[offset+7]) & 0xFF)) +  
                            ((((long) NTPData[offset+6]) & 0xFF) << 8) +  
                            ((((long) NTPData[offset+5]) & 0xFF) << 16) +  
                            ((((long) NTPData[offset+4]) & 0xFF) << 24);  
                long millisLong = (intPart * 1000) + (fracPart * 1000)/0X100000000L;  
              
                return millisLong;  
                    }                     
        private void initPacket()  
            {  
                try  
                 {  
                NTPData[0] = 0x1C;  
                for(int i=1; i<16; i++)  
                {  
                    NTPData[i] = 0;  
                }  
                  
                setReferenceTime();       
                setOrigTime();  
                setReceiveTime();  
                setTransmitTime();  
        }  
        catch (Exception e) {}  
                  
            }  
              
    public static void main(String[] args) {  
        // TODO: Add your code here  
//      try  
//      {  
          new NTPServer();  
//       }  
//      catch(Exception e)  {};  
    }     
}  
