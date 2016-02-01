// Copyright 2016 litgh. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be found
// in the LICENSE file.

package com.litgh.httprouter;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import java.util.List;


public interface Handler {

	public void handle(HttpRequest req, FullHttpResponse resp, List<Param> params);
}
