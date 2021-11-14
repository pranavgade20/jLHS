package jLHS;

import jLHS.exceptions.ProtocolFormatException;
import jLHS.http1_1server.Server;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        Server server = new Server(8080);

        server.on(Method.GET, "/k", ((request, response) -> {
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 10000; i++) {
                    sb.append("a");
                }
                response.write(new ByteArrayInputStream(sb.toString().getBytes()));
                response.end();
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));

        server.on(Method.GET, "/", ((request, response) -> {
            try {
                response.writeHeader("Accept-Encoding", "gzip");
                response.print("<html>\n" +
                        "    <head>\n" +
                        "        <title>Hello</title>\n" +
                        "    </head>\n" +
                        "    <body>\n" +
                        "        <h1>Form</h1>\n" +
                        "        <p>\n" +
                        "            <form action=\"/k\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                        "                <input type=\"text\" name=\"a\" value=\"b\">\n" +
                        "                <br>\n" +
                        "                <input type=\"file\" name=\"file\">\n" +
                        "                <br>\n" +
                        "                <input type=\"submit\">\n" +
                        "            </form>\n" +
                        "        </p>\n" +
                        "    </body>\n" +
                        "</html>\n");
            } catch (ProtocolFormatException | IOException e) {
                e.printStackTrace();
            }
        }));

        server.on(Method.POST, "/p", ((request, response) -> {
            try {
                request.getRequestReader().getFormData("a").orElseThrow(Exception::new).getFormData().transferTo(System.out);
                request.getRequestReader().getFormData("c").orElseThrow(Exception::new).getFormData().transferTo(System.out);
                response.gzip = true;
                response.print("aaaa\n");
                response.end();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));


        server.start(1);

        System.out.println("started server");
    }
}
