// Copyright 2016 litgh. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be found
// in the LICENSE file.

package com.litgh;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.litgh.httprouter.Handler;
import com.litgh.httprouter.Param;
import com.litgh.httprouter.Router;

public class RouterTest extends TestCase {

	public void testRouter() {
		Router router = new Router();
		final Map<String, Boolean> test = new HashMap<String, Boolean>();
		router.GET("/user/:name", new Handler() {
			public void handle(HttpRequest req, FullHttpResponse resp, List<Param> params) {
				test.put("routed", true);
				Param want = new Param("name", "router");
				if (params.size() == 1 && want.getKey().equals(params.get(0).getKey())
						&& want.getValue().equals(params.get(0).getValue())) {
					test.put("paramed1", true);
				} else {
					test.put("paramed1", false);
				}

			}
		});

		HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/user/router");
		FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		router.serverHttp(req, resp);

		assertEquals("routed", Boolean.TRUE, test.get("routed"));
		assertEquals("paramed1", Boolean.TRUE, test.get("paramed1"));
	}
	
	public void testRouter1() {
		Router router = new Router();
		final Map<String, Boolean> test = new HashMap<String, Boolean>();
		router.GET("/user/:firstname/:lastname", new Handler() {
			public void handle(HttpRequest req, FullHttpResponse resp, List<Param> params) {
				test.put("routed", true);
				Param want1 = new Param("firstname", "fname");
				Param want2 = new Param("lastname", "lname");
				
				if (params.size() == 2 && want1.getKey().equals(params.get(0).getKey())
						&& want1.getValue().equals(params.get(0).getValue())
						&& want2.getKey().equals(params.get(1).getKey())
						&& want2.getValue().equals(params.get(1).getValue())) {
					test.put("paramed2", true);
				} else {
					test.put("paramed2", false);
				}

			}
		});

		HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/user/fname/lname");
		FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		router.serverHttp(req, resp);

		assertEquals("routed", Boolean.TRUE, test.get("routed"));
		assertEquals("paramed2", Boolean.TRUE, test.get("paramed"));
	}

	public void testRouterAPI() {
		Router router = new Router();
		final Map<String, Boolean> test = new HashMap<String, Boolean>();

		router.GET("/GET", new Handler() {
			public void handle(HttpRequest req, FullHttpResponse resp, List<Param> params) {
				test.put("GET", true);
			}
		});

		router.POST("/POST", new Handler() {
			public void handle(HttpRequest req, FullHttpResponse resp, List<Param> params) {
				test.put("POST", true);
			}
		});

		router.PUT("/PUT", new Handler() {
			public void handle(HttpRequest req, FullHttpResponse resp, List<Param> params) {
				test.put("PUT", true);
			}
		});

		router.DELETE("/DELETE", new Handler() {
			public void handle(HttpRequest req, FullHttpResponse resp, List<Param> params) {
				test.put("DELETE", true);
			}
		});

		router.HEAD("/HEAD", new Handler() {
			public void handle(HttpRequest req, FullHttpResponse resp, List<Param> params) {
				test.put("HEAD", true);
			}
		});

		router.PATCH("/PATCH", new Handler() {
			public void handle(HttpRequest req, FullHttpResponse resp, List<Param> params) {
				test.put("PATCH", true);
			}
		});

		HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/GET");
		FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		router.serverHttp(req, resp);

		req.setMethod(HttpMethod.POST);
		req.setUri("/POST");
		router.serverHttp(req, resp);

		req.setMethod(HttpMethod.PUT);
		req.setUri("/PUT");
		router.serverHttp(req, resp);

		req.setMethod(HttpMethod.DELETE);
		req.setUri("/DELETE");
		router.serverHttp(req, resp);

		req.setMethod(HttpMethod.HEAD);
		req.setUri("/HEAD");
		router.serverHttp(req, resp);

		req.setMethod(HttpMethod.PATCH);
		req.setUri("/PATCH");
		router.serverHttp(req, resp);

		assertEquals("routing GET failed", Boolean.TRUE, test.get("GET"));
		assertEquals("routing POST failed", Boolean.TRUE, test.get("POST"));
		assertEquals("routing PUT failed", Boolean.TRUE, test.get("PUT"));
		assertEquals("routing DELETE failed", Boolean.TRUE, test.get("DELETE"));
		assertEquals("routing HEAD failed", Boolean.TRUE, test.get("HEAD"));
		assertEquals("routing PATCH failed", Boolean.TRUE, test.get("PATCH"));
	}
}
