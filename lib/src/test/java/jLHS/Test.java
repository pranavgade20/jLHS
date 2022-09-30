package jLHS;

import jLHS.exceptions.ProtocolFormatException;
import jLHS.http1_1server.Server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Test {
    public static void main(String[] args) throws IOException {
        Server server = new Server(8080);

        server.on(Method.GET, "/k", ((request, response) -> {
            try {
                response.gzip = true;
                response.print("aaaa\n");
                response.end();
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));

        server.on(Method.POST, "/p", ((request, response) -> {
            try {
                request.getRequestReader().getFormData("a").orElseThrow().getFormData().transferTo(System.out);
                request.getRequestReader().getFormData("c").orElseThrow().getFormData().transferTo(System.out);
                response.gzip = true;
                response.print("aaaa\n");
                response.end();
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));

        server.start();

        System.out.println("started server");

        Socket client = new Socket("localhost", 8080);
        var out = new PrintWriter(new BufferedOutputStream(client.getOutputStream()));
        out.print("POST /p\r\n");
        out.flush();

    }


}
