package jLHS.readers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

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
    public void fillCompletely() throws IOException {
        if (filledCompletely) return;
        synchronized (buf) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(); // maybe doing this manually will be faster
            this.transferTo(bos);
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
    public long transferTo(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0L;

        for(int read; (read = this.read()) >= 0; transferred ++) {
            out.write(read);
        }

        return transferred;
    }
}
