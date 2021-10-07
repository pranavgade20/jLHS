package jLHS;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Server {
    private ServerSocket serverSocket;
    private Thread serverThread;
    private ArrayList<Route> routes;
    private HashSet<String> defaultHeaders = new HashSet<>();

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        routes = new ArrayList<>();
    }

    public Server(int port, String keystorePath, String password) throws Exception{
        SSLContext ctx;
        KeyManagerFactory kmf;
        KeyStore ks;

        ctx = SSLContext.getInstance("TLS");
        kmf = KeyManagerFactory.getInstance("SunX509");
        ks = KeyStore.getInstance("JKS");

        ks.load(new FileInputStream(keystorePath), password.toCharArray());
        kmf.init(ks, password.toCharArray());
        ctx.init(kmf.getKeyManagers(), null, null);

        serverSocket = ctx.getServerSocketFactory().createServerSocket(port);

        ((SSLServerSocket)serverSocket).setNeedClientAuth(false);
    }

    public void addDefaultHeaders(Collection<String> defaultHeaders) {
        this.defaultHeaders.addAll(defaultHeaders);
    }

    public HashSet<String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public void start() {
        serverThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Socket client = serverSocket.accept();
                    Request request = new Request(client);
                    Response response = new Response(client, defaultHeaders);
                    boolean handled = false;

                    for (Route route :
                            routes) {
                        if (route.method == request.getMethod()) {
                            String requestPath = request.getPath().split("\\?")[0];
                            if (Pattern.matches(route.path, requestPath)) {
                                route.handler.handler(request, response);
                                if (response.getStatus() != Response.Status.ENDED_RESPONSE)
                                    response.end();
                                handled = true;
                                break;
                            }
                        }
                    }
                    if (!handled) {
                        response.setCode(404, "Not Found");
                        response.writeHeader("content-type", "text/html");
                        response.print("Error 404 : The requested url was not found on the server.");
                        response.end();
                    }
                } catch (ThreadDeath e) {
                    // stopped the thread
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        serverThread.start();
    }

    @Deprecated
    public void stop() throws IOException {
        serverThread.interrupt();
        serverThread.stop();
        serverSocket.close();
    }

    public void on(Method method, String path, ConnectionHandler handler) {
        routes.add(new Route(method, path.split("\\?")[0], handler));
    }
}
