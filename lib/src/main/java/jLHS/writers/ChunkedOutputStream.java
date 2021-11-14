package jLHS.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ChunkedOutputStream extends SimpleOutputStream {
    public ChunkedOutputStream(OutputStream out) {
        super(out);
    }

    private void flushBuffer() throws IOException {
        if (this.count > 0) {
            this.out.write((Integer.toString(count, 16) + "\r\n").getBytes(StandardCharsets.US_ASCII));
            this.out.write(this.buf, 0, this.count);
            this.out.write("\r\n".getBytes());
            this.count = 0;
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (this.count >= this.buf.length) {
            this.flushBuffer();
        }
        this.buf[this.count++] = (byte)b;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (len >= this.buf.length) {
            this.flushBuffer();
            while (off < len) {
                this.write(b, off, Math.min(this.buf.length - 1, len));
                off += this.buf.length - 1;
                len -= this.buf.length - 1;
            }
        } else {
            if (len > this.buf.length - this.count) {
                this.flushBuffer();
            }

            System.arraycopy(b, off, this.buf, this.count, len);
            this.count += len;
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        this.flushBuffer();
        this.out.flush();
    }

    @Override
    public void end() throws IOException {
        flushBuffer();
        this.out.write("0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        super.close();
    }
}
