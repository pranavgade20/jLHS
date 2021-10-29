package jLHS;

import jLHS.exceptions.ProtocolFormatException;
import jLHS.http1_1server.Server;

import java.io.IOException;

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

        server.on(Method.GET, "/", ((request, response) -> {
            try {
                response.writeHeader("Accept-Encoding", "gzip");
                response.print("""
                <html>
                    <head>
                        <title>Hello</title>
                    </head>
                    <body>
                        <h1>Form</h1>
                        <p>
                            <form action="/k" method="post" enctype="multipart/form-data">
                                <input type="text" name="a" value="b">
                                <br>
                                <input type="file" name="file">
                                <br>
                                <input type="submit">
                            </form>
                        </p>
                    </body>
                </html>
                """);
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


        server.start(1);

        System.out.println("started server");
    }
}
