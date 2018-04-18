package com.scottkrulcik.agnostic;

public interface Endpoint<I extends Endpoint.Request, O extends Endpoint.Response> {

    O handleRequest(I request);

    interface Request { }

    interface Response { }
}
