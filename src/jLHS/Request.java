package jLHS;

import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.ProtocolFormatException;
import jLHS.exceptions.URLFormatException;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Request {
    private RequestReader requestReader;
    public Socket socket;


    public Request(Socket clientSocket) throws IOException, MalformedRequestException, URLFormatException, ProtocolFormatException {
        requestReader = new RequestReader(clientSocket.getInputStream());
        socket = clientSocket;
    }


    public RequestReader getRequestReader() {
        return requestReader;
    }

    public String getHeader(String header) {
        return requestReader.getHeaders().get(header);
    }

    public HashMap<String, String> getHeaders() {
        return requestReader.getHeaders();
    }

    public String getParam(String param) {
        return requestReader.getParams().get(param);
    }

    public HashMap<String, String> getParams() {
        return requestReader.getParams();
    }

    public String getPath() {
        return requestReader.getPath();
    }

    public Method getMethod() {
        return requestReader.getMethod();
    }
}
