package jLHS.simpleserver;

import jLHS.Method;
import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.ProtocolFormatException;
import jLHS.exceptions.URLFormatException;
import jLHS.readers.SimpleInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class RequestReader extends BufferedInputStream implements jLHS.RequestReader {
    HashMap<String, String> headers = new HashMap<>();
    String path;
    HashMap<String, String> params = new HashMap<>();
    Method method;

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getPath() {
        return path;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public Method getMethod() {
        return method;
    }

    public RequestReader(InputStream in) throws MalformedRequestException, ProtocolFormatException, IOException {
        super(in);
        try {
            String line = readLine();
            path = URLDecoder.decode(line.split(" ")[1]);
            method = Method.valueOf(line.split(" ")[0]);

            parseParams(path);
        } catch (Exception exception) {
            throw new MalformedRequestException("The client request does not follow the HTTP protocol.", exception);
        }

        parseHeaders();

        if (method == Method.POST) {
            parseFormData();
        }
    }

    /**
     * Parses the headers of the http request.
     * @throws MalformedRequestException
     */
    protected void parseHeaders() throws MalformedRequestException{
        try {
            String line;
            while ((line = readLine()) != null && !line.isEmpty()) {
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
    protected void parseParams(String requestPath) throws URLFormatException{
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

    protected long content_length;
    long read_content_count = 0;
    protected String boundary;
    protected HashMap<String, FormData> cache = new HashMap<>();
    public void parseFormData() throws ProtocolFormatException, IOException {
        try {
            boundary = "--" + headers.get("Content-Type").split("=")[1];
        } catch (Exception e) {
            throw new ProtocolFormatException("Malformed headers: Expected header describing the boundary of data being sent.", e);
        }
        try {
            content_length = Long.parseLong(headers.get("Content-Length"));
        } catch (Exception e) {
            throw new ProtocolFormatException("Malformed headers: Expected header describing the length of data being sent. Chunked encoding is not supported yet.", e);
        }
        read_content_count = 0;

        String line = readLine();
        while (!line.equals(boundary)) line = readLine();
    }

    public Optional<FormData> getFormData(String name) throws IOException, ProtocolFormatException {
        if (cache.containsKey(name)) return Optional.of(cache.get(name));
        for (FormData formData : cache.values()) {
            formData.getFormData().fillCompletely();
        }
        while (read_content_count < content_length) {
            HashMap<String, String> headers = new HashMap<>();
            String line;
            while (!(line = readLine()).isEmpty()) {
                String[] h = line.split(": ");
                headers.put(h[0], h[1]);
            }
            FormData formData = new FormData(headers, new FormDataInputStream((boundary).getBytes(StandardCharsets.US_ASCII)));

            if (!headers.containsKey("Content-Disposition"))
                throw new ProtocolFormatException("Malformed headers: Expected header describing the type of content.", null);
            String[] content_disposition = headers.get("Content-Disposition").split("; ");
            String content_name =
                    Arrays.stream(content_disposition)
                    .filter(s -> s.startsWith("name=\""))
                    .findAny()
                    .orElseThrow(() -> new ProtocolFormatException("Content name expected, but not found", null));
            content_name = content_name.substring("name=\"".length(), content_name.length() - 1);
            cache.put(content_name, formData);
            if (content_name.equals(name)) {
                return Optional.of(formData);
            } else {
                formData.getFormData().fillCompletely();
            }
        }
        return Optional.empty();
    }

    public class FormData {
        HashMap<String, String> headers;
        FormDataInputStream formData;

        public FormData(HashMap<String, String> headers, FormDataInputStream formData) {
            this.formData = formData;
            this.headers = headers;
        }

        public HashMap<String, String> getHeaders() {
            return headers;
        }

        public FormDataInputStream getFormData() {
            return formData;
        }
    }

    String readLine() throws IOException {
        StringBuilder s = new StringBuilder(50);
        int read;
        char prev = (char) (read = read());
        char curr = (char) (read = read());
        RequestReader.this.read_content_count += 2;
        while (prev != '\r' && curr != '\n') {
            if (read == -1) throw new IOException("Reached end of stream");
            s.append(prev);
            prev = curr;
            if (this.pos >= this.count) {
                curr = (char) (read = read()); // makes it call fill(), I wish there was a better way here
            } else {
                curr = (char) buf[this.pos++];
            }
            RequestReader.this.read_content_count++;
        }
        return s.toString();
    }

    public class FormDataInputStream extends InputStream {
        byte[] buf;
        int pos = 0;
        int count = 0;
        byte[] boundary;

        FormDataInputStream(byte[] boundary) {
            this.boundary = boundary;
            buf = new byte[8*1024 + boundary.length];
        }

        boolean filledCompletely = false;
        public void fillCompletely() throws IOException {
            if (filledCompletely) return;
            synchronized (buf) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(); // maybe doing this manually will be faster
                SimpleInputStream.transfer(this, bos);
                bos.write(boundary);
                this.buf = bos.toByteArray();
                this.pos = 0;
                this.count = this.buf.length;
            }
            filledCompletely = true;
        }

        protected void fill(int max_bytes) throws IOException {
            synchronized (buf) {
                if (pos == 0 && count == buf.length) {
                    throw new OutOfMemoryError("Buffer is too small, can't fill anymore."); // This should never happen
                }
                if (count + boundary.length > buf.length) {
                    // we don't need to move data around unless we might run out of buffer space
                    System.arraycopy(buf, pos, buf, 0, count - pos);
                    count -= pos;
                    pos = 0;
                }
                if (RequestReader.this.count == RequestReader.this.pos) {
                    if (RequestReader.this.read_content_count == RequestReader.this.content_length) return;
                    int read = RequestReader.this.read();
                    RequestReader.this.read_content_count++;
                    if (read == -1) throw new IOException("Can't read more bytes.");
                    buf[count++] = (byte) read;
                } else {
                    int len = Math.min(max_bytes, Math.min(buf.length - count, RequestReader.this.count - RequestReader.this.pos));
                    System.arraycopy(RequestReader.this.buf, RequestReader.this.pos, buf, count, len);
                    RequestReader.this.pos += len;
                    RequestReader.this.read_content_count += len;
                    count += len;
                }
            }
        }
        boolean trailerRead = false;
        @Override
        public int read() throws IOException {
            synchronized (buf) {
                if (this.pos >= this.count) {
                    this.fill(boundary.length);
                    if (this.pos >= this.count) {
                        return -1;
                    }
                }

                byte ret = buf[this.pos];
                if (ret == boundary[0]) {
                    //trying to match..
                    for (int i = 1; i < boundary.length; i++) {
                        if (this.pos + i >= this.count) this.fill(boundary.length - i);
                        if (buf[pos + i] != boundary[i]) { // TODO use a better string matching algo, perhaps KMP?
                            this.pos++;
                            return ret & 255;
                        }
                    }
                    // we have a match
                    if (!trailerRead) {
                        trailerRead = true;
                        RequestReader.this.read(); // get rid of trailing \r\n or --
                        RequestReader.this.read();
                        RequestReader.this.read_content_count += 2;
                    }
                    return -1;
                }
                this.pos++;
                return ret & 255;
            }
        }
    }
}
