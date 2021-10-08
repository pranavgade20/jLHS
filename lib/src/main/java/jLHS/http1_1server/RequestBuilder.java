package jLHS.http1_1server;

import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.ProtocolFormatException;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RequestBuilder implements Iterable<Request>{
    Socket client;
    RequestInputStream inputStream;
    boolean active = true;
    Request prev;
    Iterator<Request> it;
    public RequestBuilder(Socket client) throws IOException {
        this.client = client;
        inputStream = new RequestInputStream(client.getInputStream());
        it = new Iterator<>() {
            @Override
            public boolean hasNext() {
                if (client.isInputShutdown() || client.isClosed()) active = false;
                return active;
            }

            @Override
            public Request next() {
                try {
                    if (prev != null) prev.requestReader.fillCompletely();
                    prev = new Request(inputStream);
                    return prev;
                } catch (IOException | MalformedRequestException | ProtocolFormatException e) {
                    e.printStackTrace();
                    throw new NoSuchElementException(e.getLocalizedMessage());
                }
            }
        };
    }


    @Override
    public Iterator<Request> iterator() {
        return it;
    }
}
