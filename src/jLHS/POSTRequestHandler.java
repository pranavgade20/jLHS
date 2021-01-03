package jLHS;

import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.ProtocolFormatException;

import java.io.*;
import java.nio.ByteBuffer;

public class POSTRequestHandler{
    private Request request;
    private byte[] boundary;
    public POSTRequestHandler(Request request) throws ProtocolFormatException {
        this.request = request;
        try {
            boundary = request.getHeader("Content-Type").split("=")[1].getBytes();
        } catch (Exception e) {
            throw new ProtocolFormatException("Malformed headers: Expected boundary header describing the boundaries of data being sent.", e);
        }
    }
    public FormData getFormData (String fieldName) throws ProtocolFormatException, IOException {
        String name = null;
        BufferedReader reader = request.getRequestReader();

        String line;
        try {
            while (true) {
                boolean toBreak = false;
                // reading through headers until we find what we are looking for
                line = reader.readLine();
                while (!(line).isEmpty()) {
                    String[] headers = line.split("; ");
                    for (String header : headers) {
                        if (header.split("=")[0].equalsIgnoreCase("name")) {
                            String val = header.split("=")[1];
                            val = val.substring(1, val.length()-1);
                            if (val.equals(fieldName)) toBreak = true;
                        }
                        if (header.split("=")[0].equalsIgnoreCase("filename")) {
                            name = header.split("=")[1];
                            name = name.substring(1, name.length()-1);
                        }
                    }
                    line = reader.readLine();
                }
                if (toBreak) break;

                // now skip through all data until we get to next section(aka until we reach next boundary)
                int b = reader.read();
                finding_boundary: while (true) {
                    if (b == -1) {
                        throw new MalformedRequestException("Unexpected end of stream.", null);
                    }
                    if (b == 0x0d) { // '\r' or carriage return
                        if (reader.read() == 0x0a) { // '\n' or line feed
                            for (int pos = 0; pos < boundary.length; pos++) {
                                if ((b = reader.read()) != boundary[pos]) {
                                    continue finding_boundary;
                                }
                            }
                            break;
                        }
                    }
                    b = reader.read();
                }
            }
        } catch (Exception e) {
            throw new ProtocolFormatException("Request does not follow HTTP protocol.", e.getCause());
        }

        return new FormData(name, new InputStream() {
            private int buf;
            private boolean finished = false;
            private ByteBuffer buffer = ByteBuffer.wrap(new byte[boundary.length + 2]);
            @Override
            public int read() throws IOException {
                if (finished) return -1;
                if (buffer.hasRemaining()) return buffer.get();
                if ((buf = reader.read()) == 0x0d) { // '\r' or carriage return
                    if ((buf = reader.read()) == 0x0a) { // '\n' or line feed
                        buffer.clear();
                        buffer.put((byte)0x0a);
                        for (int pos = 0; pos < boundary.length; pos++) {
                            buffer.put((byte) (buf&0xFF));
                            if ((buf = reader.read()) != boundary[pos]) {
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
        });
    }

    public class FormData {
        public String resourceName;
        public InputStream resourceStream;

        public FormData(String name, InputStream stream) {
            resourceName = name;
            resourceStream = stream;
        }
    }
}
