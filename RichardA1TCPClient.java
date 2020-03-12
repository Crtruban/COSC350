import java.io.*;
import java.net.*;
class RichardA1TCPClient {
    public static void main(String argv[]) throws Exception{

        try(Socket clientSocket = new Socket("localhost", 20120)){
            //1.1.1
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //1.1.2
            outToServer.writeBytes("request\n");
            String connectResponse = inFromServer.readLine();
            //1.1.3
            if(connectResponse.equalsIgnoreCase("connected")){
                System.out.println("Local host connected.");
                //1.1.4
                System.out.print("Input a web server name to be fetched: ");
                String siteRequest=inFromUser.readLine();
                String website=new String("https://" +siteRequest);
                //1.1.5
                URL url = new URL(website);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent","Mozilla/5.0");
                long startTime= System.currentTimeMillis();
                System.out.println("Connected to " +website);
                //1.1.6
                String host = url.getHost();
                InetAddress address = InetAddress.getByName(host);
                String ip = address.getHostAddress();
                System.out.println("Site IP Address: " +ip);
                //1.1.7
                BufferedReader inFromSite = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                System.out.println("Sending html contents...");
                while(((inputLine=inFromSite.readLine())!=null)){
                  System.out.println(inputLine);                
                }
                System.out.println("End of html.");
                //1.1.8
                long endTime   = System.currentTimeMillis();
                long totalDelay = endTime - startTime;
                System.out.println("Delay: " +totalDelay +"ms");
                System.out.println("Web Server's IP: " +ip);
                outToServer.writeBytes("Web Server IP: " +ip +"\tDelay: " +totalDelay +"ms");
                System.out.println("Done!");
            }
            else{
                clientSocket.close();
                System.out.println("Host response invalid. Closing connection.");
            }
        }
        catch(Exception e){
            System.out.println("Encountered exception: " +e);
        }        
    }
}