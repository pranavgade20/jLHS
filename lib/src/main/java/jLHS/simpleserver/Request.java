package jLHS.simpleserver;

import jLHS.Method;
import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.ProtocolFormatException;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Request implements jLHS.Request {
    protected RequestReader requestReader;
    public Socket socket;

    public Request(Socket clientSocket) throws IOException, MalformedRequestException, ProtocolFormatException {
        requestReader = new RequestReader(clientSocket.getInputStream());
        socket = clientSocket;
    }

    public RequestReader getRequestReader() {
        return requestReader;
    }

    public HashMap<String, String> getHeaders() {
        return requestReader.getHeaders();
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
