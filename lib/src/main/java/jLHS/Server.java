package jLHS;

import java.io.IOException;

public interface Server<Handler extends ConnectionHandler<?, ?>> {
    void start();

    void stop() throws IOException;

    void on(Method method, String pathRegex, Handler handler);
}
