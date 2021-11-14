package jLHS.readers;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class SimpleInputStream extends FilterInputStream {
    protected int read_content_count = 0;

    public SimpleInputStream(InputStream in) {
        super(in);
    }

    public String readLine() throws IOException {
        read_content_count = 0;
        StringBuilder s = new StringBuilder(50);
        int read;
        char prev = (char) (read = in.read());
        char curr = (char) (read = in.read());
        read_content_count += 2;
        while (prev != '\r' && curr != '\n') {
            if (read == -1) throw new IOException("Reached end of stream");
            s.append(prev);
            prev = curr;
            curr = (char) (read = in.read());
            read_content_count++;
        }
        return s.toString();
    }

    public int getReadContentLength() {
        return read_content_count;
    }

    public long fillCompletely() throws IOException {
        System.err.println("Are you sure you want to discard this stream tho");
        return this.transferTo(new OutputStream() {
            @Override
            public void write(int b) {
            }
        });
    }

    public long transferTo(OutputStream out) throws IOException {
        return transfer(this, out);
    }

    public static long transfer(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0L;

        int read;
        for(byte[] buffer = new byte[8192]; (read = in.read(buffer, 0, 8192)) >= 0; transferred += (long)read) {
            out.write(buffer, 0, read);
        }

        return transferred;
    }
}
