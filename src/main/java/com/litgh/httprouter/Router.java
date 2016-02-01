// Copyright 2016 litgh. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be found
// in the LICENSE file.

package com.litgh.httprouter;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class Router {
	private Map<String, Node> trees;
	private Map<String, Action> cache = new HashMap<String, Action>(100);

	public void GET(String path, Handler handler) {
		handle("GET", path, handler);
	}

	public void PUT(String path, Handler handler) {
		handle("PUT", path, handler);
	}

	public void POST(String path, Handler handler) {
		handle("POST", path, handler);
	}

	public void HEAD(String path, Handler handler) {
		handle("HEAD", path, handler);
	}

	public void PATCH(String path, Handler handler) {
		handle("PATCH", path, handler);
	}

	public void DELETE(String path, Handler handler) {
		handle("DELETE", path, handler);
	}

	public void handle(String method, String path, Handler handler) {
		if (path.charAt(0) != '/') {
			throw new RuntimeException("path must begin with '/' in path '" + path + "'");
		}

		if (trees == null) {
			this.trees = new HashMap<String, Node>();
		}

		Node root = this.trees.get(method);
		if (root == null) {
			root = new Node();
			this.trees.put(method, root);
		}

		root.addRoute(path, handler);
	}

	public void serverHttp(HttpRequest req, FullHttpResponse resp) {
		try {
			Node root = this.trees.get(req.getMethod().toString());
			if (root != null) {
				String path = req.getUri();
				Action action = cache.get(path);
				if(action == null) {
				    action = root.getValue(path);
				}
				if (action.getHandler() != null) {
					action.getHandler().handle(req, resp, action.getP());
					return;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
