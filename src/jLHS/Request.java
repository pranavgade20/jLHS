package jLHS;

import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.URLFormatException;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Request {
    private BufferedReader requestReader;
    public Socket socket;

    HashMap<String, String> headers = new HashMap<>();
    public String path;
    HashMap<String, String> params = new HashMap<>();
    Method method;

    public Request(Socket clientSocket) throws IOException, MalformedRequestException, URLFormatException {
        requestReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        socket = clientSocket;
        
        try {
            String line = requestReader.readLine();
            path = line.split(" ")[1];
            method = Method.valueOf(line.split(" ")[0]);

            parseParams(path);
        } catch (Exception exception) {
            throw new MalformedRequestException("The client request does not follow HTTP protocol.", exception);
        }

        parseHeaders(requestReader);
    }

    /**
     * Parses the headers of this http request.
     * @param requestReader
     * @throws MalformedRequestException
     */
    private void parseHeaders(BufferedReader requestReader) throws MalformedRequestException{
        try {
            String line;
            while ((line = requestReader.readLine()) != null && !line.isEmpty()) {
                String header = line.split(": ")[0];
                String value = line.split(": ")[1];
                headers.put(header, value);
            }
        } catch (Exception exception) {
            throw new MalformedRequestException("The client request does not follow HTTP protocol.", exception);
        }
    }

    /**
     * Parsed the parameters from the request path.
     * @param requestPath
     * @throws URLFormatException
     */
    //TODO: support params starting with & etc
    private void parseParams(String requestPath) throws URLFormatException{
        try {
            String[] list = requestPath.substring(requestPath.lastIndexOf("/")+1).split("\\?");
            for (int i = 1; i < list.length; i++) {
                if (list[i] != null && !list[i].isEmpty()) {
                    String key = list[i].split("=")[0];
                    String value = list[i].split("=")[1];
                    params.put(key, value);
                }
            }
        } catch (StringIndexOutOfBoundsException indexOutOfBoundsException) {
          //we are cool, do nothing
        } catch (Exception exception) {
            throw new URLFormatException("Error parsing URL", exception);
        }
    }

    public BufferedReader getRequestReader() {
        return requestReader;
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public HashMap<String, String> getHeaders() {
        return (HashMap<String, String>) headers.clone();
    }

    public String getParam(String param) {
        return params.get(param);
    }

    public HashMap<String, String> getParams() {
        return (HashMap<String, String>) params.clone();
    }
}
