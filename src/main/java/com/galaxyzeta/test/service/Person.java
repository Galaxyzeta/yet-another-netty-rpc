package com.galaxyzeta.test.service;

import java.io.Serializable;

public class Person implements Serializable {

	private static final long serialVersionUID = 624343169220911413L;

	private String username;

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
}
