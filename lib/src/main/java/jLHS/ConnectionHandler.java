package jLHS;

public interface ConnectionHandler<Req extends Request, Res extends Response> {
    void handler(Req request, Res response);
}
