package main;
//Server Creation, listens for connections coming from the browser

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.lang.Math;
import java.util.Map;
import main.utils.FileUtils;

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

                   switch(httpRequest.getMethod()){
                        case "GET":
                            handleGetRequest(httpRequest, outputStream);
                            break;

                        case "POST":
                        //TODO: implement 
                            break;
                   }
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
            outputStream.write(buff, 0, read); //this copies the data from the input file to the output file 
             
        }
    }


    public static HttpRequest parseMetadata(InputStream data) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data));
        final String firstLine = bufferedReader.readLine();
        final String method = firstLine.split("\\s+")[0];
        final String url = firstLine.split("\\s+")[1];

        final Map<String, String> headers = new HashMap<>();
        String headerLine;
        while((headerLine = bufferedReader.readLine()) != null){
            if (headerLine.trim().isEmpty()){
                break;
            }

            String key = headerLine.split(":\\s")[0];
            String value = headerLine.split(":\\s")[1];

            headers.put(key, value);
        }   

        return new HttpRequest(method, url, headers);
    }


public static void handleGetRequest(HttpRequest request, OutputStream outputStream) throws IOException{
        String fileName = request.geturl();
        if (FileUtils.exist("webapp" + fileName)) {
            fileName = "webapp" + fileName;
        }
        else if(FileUtils.exist("uploaded" + fileName)){
            fileName = "uploaded"  + fileName;
        }
        else{
            outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n<h1>FileNot Found</h1>".getBytes(StandardCharsets.UTF_8));
             return; //file doesn't exist because it is not found in any of the both folders
        }


        final StringBuilder responseMetadata = new StringBuilder();

        responseMetadata.append("HTTP/1.1 200 OK\r\n");

        responseMetadata.append(String.format("Content-Type: %s\r\n", FileUtils.probeContentType(fileName)));

        final InputStream fileStream = FileUtils.getInputStream(fileName);
        responseMetadata.append(String.format("Content-Length: %d\r\n", fileStream.available()));
        responseMetadata.append("\r\n");

        outputStream.write(responseMetadata.toString().getBytes(StandardCharsets.UTF_8));

        
        byte[] buffer = new byte[2048]; 
        int bytesRead;
        while ((bytesRead = fileStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        fileStream.close(); 
        
        
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

    public String geturl(){
        return url;
    }

    public Map<String, String> getHeader(){
        return headers;
    }
}
}
