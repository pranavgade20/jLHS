package jLHS.writers;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SimpleOutputStream extends BufferedOutputStream {
    public SimpleOutputStream(OutputStream out) {
        super(out);
    }

    public void end() throws IOException {
        write("\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        super.close();
    }
}
