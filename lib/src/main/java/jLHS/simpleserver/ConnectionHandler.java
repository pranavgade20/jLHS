package jLHS.simpleserver;


public interface ConnectionHandler extends jLHS.ConnectionHandler<Request,Response> {
    @Override
    void handler(Request request, Response response);
}
