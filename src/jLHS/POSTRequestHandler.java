package jLHS;

import jLHS.exceptions.ProtocolFormatException;

import java.io.*;
import java.nio.ByteBuffer;

public class POSTRequestHandler{
    private Request request;
    private byte[] boundary;
    public POSTRequestHandler(Request request) throws ProtocolFormatException {
        if (!request.getHeaders().containsKey("boundary")) throw new ProtocolFormatException("Malformed headers: Expected boundary header describing the boundaried of data being sent.", null);
        this.request = request;
        boundary = request.getHeader("boundary").getBytes();
    }
    public InputStream getFormData (String fieldName) {
        return new InputStream() {
            private InputStream stream;
            private int buf;
            private boolean finished = false;
            private ByteBuffer buffer = ByteBuffer.wrap(new byte[boundary.length + 2]);
            @Override
            public int read() throws IOException {
                if (finished) return -1;
                if (buffer.hasRemaining()) return buffer.get();
                if ((buf = stream.read()) == 0x0d) { // '\r' or carriage return
                    if ((buf = stream.read()) == 0x0a) { // '\n' or line feed
                        buffer.clear();
                        buffer.put((byte)0x0a);
                        for (int pos = 0; pos < boundary.length; pos++) {
                            buffer.put((byte) (buf&0xFF));
                            if ((buf = stream.read()) != boundary[pos]) {
                                //boundary does not match, start returning stuff
                                buffer.flip();
                                return (byte)0x0d;
                            }
                        }
                        // now we are sure that we have read the boundary. start returning -1s.
                        finished = true;
                        return -1;
                    } else {
                        buffer.clear();
                        buffer.put((byte) (buf&0xFF));
                        buffer.flip();
                        return (byte)0x0d;
                    }
                } else return buf;
            }
        };
    }
}
