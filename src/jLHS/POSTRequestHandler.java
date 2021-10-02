//package jLHS;
//
//import com.sun.net.httpserver.Headers;
//import jLHS.exceptions.MalformedRequestException;
//import jLHS.exceptions.ProtocolFormatException;
//
//import java.io.*;
//import java.net.http.HttpHeaders;
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//
//public class POSTRequestHandler{
//    private Request request;
//    private long content_length;
//    private String boundary;
//    private HashMap<String, FormData> cache = new HashMap<>();
//    public POSTRequestHandler(Request request) throws ProtocolFormatException {
//        this.request = request;
//        try {
//            boundary = request.getHeader("Content-Type").split("=")[1];
//        } catch (Exception e) {
//            throw new ProtocolFormatException("Malformed headers: Expected boundary header describing the boundaries of data being sent.", e);
//        }
//        try {
//            content_length = Long.parseLong(request.getHeader("Content-Length"));
//        } catch (Exception e) {
//            throw new ProtocolFormatException("Malformed headers: Expected boundary header describing the length of data being sent. Chunked encoding is not supported yet.", e);
//        }
//
//        String line;
//        try {
//            request.headers.forEach((k, v) -> System.out.println(k + ": " + v));
//            int count = 0;
//            while ((line = request.getRequestReader().readLine()) != null) {
//                System.out.println(line);
//                count += line.getBytes(StandardCharsets.US_ASCII).length + 2; // +2 is for \r\n. probably should make sure the request contains \r\n and not \n
//                if (line.startsWith(boundary)) {
//                    HashMap<String, String> headers = new HashMap<>();
//                    while ((line = request.getRequestReader().readLine()) != null && !line.isEmpty()) {
//                        headers.put(line.split(": ")[0], line.split(": ")[1]);
//                        count += line.getBytes(StandardCharsets.US_ASCII).length + 2;
//                    }
//                    if (line != null) count += line.getBytes(StandardCharsets.US_ASCII).length + 2; // for the blank line
//
//                    int matchPos = 0; // position until where the boundary matches.
//                    int BUFFER_SIZE = 1024*1024; // 1 KiB buffer, actual will be double size for matching boundary.
//                    byte[] buffer = new byte[BUFFER_SIZE*2];
//                    while (count < content_length) {
//                        request.getRequestReader().
//                    }
//                }
//                if (count == content_length) break;
//            }
//        } catch (Exception w){}
//    }
//    public FormData getFormData (String fieldName) throws ProtocolFormatException, IOException {
//        String name = null;
//        RequestReader reader = request.getRequestReader();
//
//        String line;
//        try {
//            while (true) {
//                boolean toBreak = false;
//                // reading through headers until we find what we are looking for
//                line = reader.readLine();
//                while (!(line).isEmpty()) {
//                    String[] headers = line.split("; ");
//                    for (String header : headers) {
//                        if (header.split("=")[0].equalsIgnoreCase("name")) {
//                            String val = header.split("=")[1];
//                            val = val.substring(1, val.length()-1);
//                            if (val.equals(fieldName)) toBreak = true;
//                        }
//                        if (header.split("=")[0].equalsIgnoreCase("filename")) {
//                            name = header.split("=")[1];
//                            name = name.substring(1, name.length()-1);
//                        }
//                    }
//                    line = reader.readLine();
//                }
//                if (toBreak) break;
//
//                // now skip through all data until we get to next section(aka until we reach next boundary)
//                int b = reader.read();
//                finding_boundary: while (true) {
//                    if (b == -1) {
//                        throw new MalformedRequestException("Unexpected end of stream.", null);
//                    }
//                    if (b == 0x0d) { // '\r' or carriage return
//                        if (reader.read() == 0x0a) { // '\n' or line feed
//                            for (int pos = 0; pos < boundary.getBytes().length; pos++) {
//                                if ((b = reader.read()) != boundary.getBytes()[pos]) {
//                                    continue finding_boundary;
//                                }
//                            }
//                            break;
//                        }
//                    }
//                    b = reader.read();
//                }
//            }
//        } catch (Exception e) {
//            throw new ProtocolFormatException("Request does not follow HTTP protocol.", e.getCause());
//        }
//
//        return new FormData(name, new InputStream() {
//            private int buf;
//            private boolean finished = false;
//            private ByteBuffer buffer = ByteBuffer.wrap(new byte[boundary.getBytes().length + 2]);
//            @Override
//            public int read() throws IOException {
//                if (finished) return -1;
//                if (buffer.hasRemaining()) return buffer.get();
//                if ((buf = reader.read()) == 0x0d) { // '\r' or carriage return
//                    if ((buf = reader.read()) == 0x0a) { // '\n' or line feed
//                        buffer.clear();
//                        buffer.put((byte)0x0a);
//                        for (int pos = 0; pos < boundary.getBytes().length; pos++) {
//                            buffer.put((byte) (buf&0xFF));
//                            if ((buf = reader.read()) != boundary.getBytes()[pos]) {
//                                //boundary does not match, start returning stuff
//                                buffer.flip();
//                                return (byte)0x0d;
//                            }
//                        }
//                        // now we are sure that we have read the boundary. start returning -1s.
//                        finished = true;
//                        return -1;
//                    } else {
//                        buffer.clear();
//                        buffer.put((byte) (buf&0xFF));
//                        buffer.flip();
//                        return (byte)0x0d;
//                    }
//                } else return buf;
//            }
//        });
//    }
//
//    public class FormData {
//        public String resourceName;
//        public InputStream resourceStream;
//        public HashMap<String, String> headers;
//
//        public FormData(String name, InputStream stream) {
//            resourceName = name;
//            resourceStream = stream;
//        }
//    }
//}
