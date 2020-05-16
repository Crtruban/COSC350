package Assignment2;

import java.io.*;
import java.net.*;
import java.util.*;

public class UDPServerThreads {

    public class UDPClientHandler1 implements Runnable {

        String sentence;
        InetAddress address;
        int port;

        public UDPClientHandler1(String sentence, InetAddress address, int port) {
            this.sentence = sentence;
            this.address = address;
            this.port = port;
        }

        public void run() {
            byte[] sendData = new byte[1024];
            // 1.1.3 - The CH reads the NTP request from the client and sends the response
            try {
                String threadName =
                    Thread.currentThread().getName();
                String message = "in HandleClient";
                System.out.format("%s: %s%n", threadName, message);
                long cstarttime = System.currentTimeMillis();
                System.out.println("before csocket");
                DatagramSocket csocket = new DatagramSocket();
                String capitalizedSentence = new String(sentence.toUpperCase());
                sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, address, port);
                csocket.send(sendPacket);
                System.out.println("after send in thread " + "IPAddress=" + address + " port=" + port);
                long cendtime = System.currentTimeMillis();
                System.out.println("time=" + (cendtime - cstarttime));
            } catch (IOException e) {}
        }
    }

    public void nonStatic(String udpmessage, InetAddress address, int port) {
        Thread t = new Thread(new UDPClientHandler1(udpmessage, address, port));
        t.start();
    }

    public static void main(String args[]) throws Exception {
        UDPServerThreads udpserver = new UDPServerThreads();
        try {
        	// 1.1.1 - Listens on UDP port 1000 for clients
            DatagramSocket serverSocket = new DatagramSocket(1000);
            byte[] receiveData = new byte[1024];
            int count = 0;
            while (true) {
                DatagramPacket receivePacket =
                    new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                System.out.println("after rcv in server");
                String udpmessage = new String(receivePacket.getData());
                System.out.println("sentence" + udpmessage);
                InetAddress address = receivePacket.getAddress();
                int port = receivePacket.getPort();
                // 1.1.2 - Starts a client handler CH (new thread) to handle the client
                udpserver.nonStatic(udpmessage, address, port);
                count++;
                System.out.println("after start thread" + count);
                serverSocket.close();
            }
        } catch (IOException e) {}
    }
}