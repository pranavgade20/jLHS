package jLHS.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GzipOutputStream extends SimpleOutputStream {
    GZIPOutputStream outputStream;
    OutputStream out;
    public GzipOutputStream(OutputStream out) throws IOException {
        super(new GZIPOutputStream(out));
        this.out = out;
        outputStream = (GZIPOutputStream)super.out;
    }

    @Override
    public void end() throws IOException {
        outputStream.finish();
        if (out instanceof SimpleOutputStream) ((SimpleOutputStream) out).end();
        super.close();
    }
}
