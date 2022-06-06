package com.ec.g2g.utilitario;

public class RealmId {
	
	
	private String realmId;
	private String token;

	
	public RealmId(String realmId) {
		super();
		this.realmId = realmId;
	}

	public RealmId(String realmId, String token) {
		super();
		this.realmId = realmId;
		this.token = token;
	}

	public String getRealmId() {
		return realmId;
	}

	public void setRealmId(String realmId) {
		this.realmId = realmId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
