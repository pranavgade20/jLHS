package jLHS.readers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FixedLengthInputStream extends SimpleInputStream {
    byte[] buf;
    int pos = 0;
    int count = 0;
    byte[] boundary;

    public FixedLengthInputStream(byte[] boundary, InputStream inputStream) {
        super(inputStream);
        this.boundary = boundary;
        buf = new byte[8*1024 + boundary.length];
    }

    boolean filledCompletely = false;
    public long fillCompletely() throws IOException {
        if (filledCompletely) return 0;
        synchronized (buf) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(); // maybe doing this manually will be faster
            this.transferTo(bos);
            bos.write(boundary);
            this.buf = bos.toByteArray();
            this.pos = 0;
            this.count = this.buf.length;
        }
        filledCompletely = true;

        return read_content_count;
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
            int len = Math.min(max_bytes, buf.length - count);
            int read = in.read(buf, count, len);
            if (read == -1) throw new IOException("Can't read more bytes.");
            read_content_count += read;
            count += read;

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
                    in.read(); // get rid of trailing \r\n or --
                    in.read();
                    read_content_count += 2;
                }
                return -1;
            }
            this.pos++;
            return ret & 255;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // from https://developer.classpath.org/doc/java/io/InputStream-source.html
        if (off < 0 || len < 0 || b.length - off < len)
            throw new IndexOutOfBoundsException();

        int i, ch;
        for (i = 0; i < len; ++i)
            try {
                if ((ch = read()) < 0) return i == 0 ? -1 : i;
                b[off + i] = (byte) ch;
            } catch (IOException ex) {
                // Only reading the first byte should cause an IOException.
                if (i == 0) throw ex;
                return i;
            }

        return i;
    }
}
