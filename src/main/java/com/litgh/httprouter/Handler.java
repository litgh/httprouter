package com.litgh.httprouter;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import java.util.List;


public interface Handler {

	public void handle(HttpRequest req, FullHttpResponse resp, List<Param> params);
}
