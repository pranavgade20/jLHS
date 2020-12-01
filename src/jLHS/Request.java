package jLHS;

import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.URLFormatException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

public class Request {
    private InputStream inputStream;
    public Socket socket;

    HashMap<String, String> headers = new HashMap<>();
    public String path;
    HashMap<String, String> params = new HashMap<>();
    Method method;

    public Request(Socket clientSocket) throws IOException, MalformedRequestException, URLFormatException {
        inputStream = clientSocket.getInputStream();
        socket = clientSocket;

        parseHeaders(inputStream);
        parseParams(path);
    }

    /**
     * Parses the headers of this http request.
     * @param inputStream
     * @throws MalformedRequestException
     */
    private void parseHeaders(InputStream inputStream) throws MalformedRequestException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line = reader.readLine();
            path = line.split(" ")[1];
            method = Method.valueOf(line.split(" ")[0]);
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
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

    public InputStream getStream() {
        return inputStream;
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
