// Copyright 2016 litgh. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be found
// in the LICENSE file.

package com.litgh.httprouter;

public class Param {
	private String key;
	private String value;

	public Param() {
	}

	public Param(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		return String.format("key:%s, value:%s", this.key, this.value);
	}
}
