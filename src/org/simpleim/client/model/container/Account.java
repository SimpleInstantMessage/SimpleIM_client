package org.simpleim.client.model.container;

public class Account {
	private String id;
	private String password;
	private String nickname;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public Account setId(String id) {
		this.id = id;
		return this;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public Account setPassword(String password) {
		this.password = password;
		return this;
	}
	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return nickname;
	}
	/**
	 * @param nickname the nickname to set
	 */
	public Account setNickname(String nickname) {
		this.nickname = nickname;
		return this;
	}

	public boolean isValid() {
		return id != null && !id.isEmpty() && password != null;
	}
}
