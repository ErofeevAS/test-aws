package com.example.demo.registration;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


public class Registration {

	@NotBlank
	private String username;

	@Email
	private String email;

	@ValidInvitationCode
	private String invitationCode;

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getInvitationCode() {
		return invitationCode;
	}

	public void setInvitationCode(final String invitationCode) {
		this.invitationCode = invitationCode;
	}
}
