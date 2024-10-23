//Server Creation, listens for connections coming from the browser

import java.io.*;
import java.net.*;
import java.util.Map;
import java.lang.Math;

public class StartUp{
    public static void main(String[] args) throws Exception{ //throws exception after main method
            final int port = 8080;
            final ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Listening on port: " + port); 

            while(true){ //classic server method, will continue getting sockets until the request has been processed and you keep listening to one till the end of time
              Socket client = serverSocket.accept();
              InputStream inputStream = client.getInputStream();// what we are receiving from the browser, InputStream class method
              OutputStream outputStream = client.getOutputStream(); // what we are sending to the browser, OutputStream class method

              new Thread(() -> { //we don't want all these connections to happen on the same thread, so we create another thread
                try{
                    final ByteArrayOutputStream request = new ByteArrayOutputStream(); //read and write from the streams, do it in the thread class, so you can read multiple clients
                    transfer(inputStream, request);

                    final HttpRequest httpRequest = parseMetadata(new ByteArrayInputStream(request.toByteArray())); 
                }
                catch(IOException e){
                    e.printStackTrace();
                } finally{
                    try{
                        client.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
              }).start();
            }
        }

    public static void transfer(InputStream inputStream, OutputStream outputStream) throws IOException { //method to make the transfer of streams
        final byte[] buff = new byte[2048]; //2kb

        int read;
        while((read = inputStream.read(buff, 0, Math.min(inputStream.available(), 2048))) > 0){
            outputStream.write(buff, 0, read); 
             
        }
    }


    public static HttpRequest parseMetadata(InputStream data){
        
    }

    static class HttpRequest{
        private final String method;
        private final String url;
        private final Map<String, String> headers;

        HttpRequest(String method, String url, Map<String, String> headers){
            this.method = method;
            this.url = url;
            this.headers = headers;
        }

        public String getMethod(){
            return method;
        }

        public String getURl(){
            return url;
        }

        public Map<String, String> getHeader(){
            return headers;
        }
    }
}