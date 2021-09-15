package com.cliffordlab.amoss.network.json;

/**
 * Created by michael on 2/4/16.
 */
public class LoginRequest {
	private final long participantID;
	private final String email;
	private final String password;

	public LoginRequest(long id, String email, String password) {
		this.participantID = id;
		this.email = email;
		this.password = password;
	}
}
