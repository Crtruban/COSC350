import java.io.*;
import java.net.*;
class WebClient18 {
 public static void main(String argv[]) throws Exception
 {
   String inputLine;
   URL obj = new URL("http://www.umd.edu");
   HttpURLConnection con = (HttpURLConnection) obj.openConnection();
   con.setRequestMethod("GET");
   con.setRequestProperty("User-Agent","Mozilla/5.0");
   BufferedReader in = new BufferedReader(
        new InputStreamReader(con.getInputStream())); 
                while ((inputLine = in.readLine()) != null) 
                   System.out.println(inputLine);        
 }
}