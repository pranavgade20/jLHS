package jLHS.http1_1server;

import jLHS.Method;
import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.ProtocolFormatException;
import jLHS.readers.SimpleInputStream;

import java.io.IOException;
import java.util.HashMap;

public class Request implements jLHS.Request {
    protected RequestReader requestReader;

    public Request(SimpleInputStream inputStream) throws IOException, MalformedRequestException, ProtocolFormatException {
        requestReader = new RequestReader(inputStream);
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
