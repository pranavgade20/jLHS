package jLHS.http1_1server;

import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.ProtocolFormatException;
import jLHS.readers.SimpleInputStream;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RequestBuilder implements Iterable<Request>{
    Socket client;
    SimpleInputStream inputStream;
    boolean cached = false;
    Request request;
    Iterator<Request> it;
    public RequestBuilder(Socket client) throws IOException {
        this.client = client;
        inputStream = new SimpleInputStream(client.getInputStream());
        it = new Iterator<>() {
            @Override
            public boolean hasNext() {
                if (cached) return true;
                try {
                    if (request != null) request.requestReader.fillCompletely();
                    request = new Request(inputStream);
                    cached = true;
                } catch (IOException | MalformedRequestException | ProtocolFormatException e) {
                    try {
                        if (!client.isClosed()) client.close();
                    } catch (IOException ignored) {}
                    return false;
                }
                return true;
            }

            @Override
            public Request next() {
                if (!cached) {
                    try {
                        if (request != null) request.requestReader.fillCompletely();
                        request = new Request(inputStream);
                        cached = true;
                    } catch (IOException | MalformedRequestException | ProtocolFormatException e) {
                        try {
                            if (!client.isClosed()) client.close();
                        } catch (IOException ignored) {}
                        throw new NoSuchElementException("The HTTP request is closed.");
                    }
                }
                cached = false;
                Request ret = request;
                request = null;
                return ret;
            }
        };
    }


    @Override
    public Iterator<Request> iterator() {
        return it;
    }
}
