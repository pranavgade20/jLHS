package jLHS;

import jLHS.exceptions.ProtocolFormatException;
import jLHS.http1_1server.Response;
import jLHS.http1_1server.Server;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        Server server = new Server(8080);

        server.on(Method.GET, "/k", ((request, response) -> {
            try {
                ((Response)response).gzip = true;
                response.print("asdf\n");
                response.end();
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));

        server.start();
    }
}
