package com.taxist.oauth2;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;

public class CredentialsSharedPreferences {
	/**
	 * Z razredom CredentialsSharedPreferences shranimo nastavitve in ostale podakte 
	 * v medpomnilnik, ki se tam hranijo vse dokler se iz aplikacije na odjavimo.
	 * Pri vnoviènem zagonu (brez odjave) aplikacija prebere 
	 * shranjene podakte, ki jih potrebujemo za delovanje aplikacije.
	 */

	private static final String ACCESS_TOKEN = "access_token";
	private static final String EXPIRES_IN = "expires_in";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String SCOPE = "scope";
	private static final String GOOGLEID = "googleId";
	private static final String IME = "ime";
	private static final String SLIKA_URL = "slikaUrl";
	private static final String MOJID = "mojID";
	private static final String EMAIL = "email";
	private static final String PASSWORD = "password";
	// skupne nastavitve
	private SharedPreferences prefs;

	public CredentialsSharedPreferences(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public AccessTokenResponse readAccessToken() {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.accessToken = prefs.getString(ACCESS_TOKEN, "");
		accessTokenResponse.expiresIn = prefs.getLong(EXPIRES_IN, 0);
		accessTokenResponse.refreshToken = prefs.getString(REFRESH_TOKEN, "");
		accessTokenResponse.scope = prefs.getString(SCOPE, "");
		return accessTokenResponse;
	}

	public void write(AccessTokenResponse accessTokenResponse) {
		Editor editor = prefs.edit();
		editor.putString(ACCESS_TOKEN, accessTokenResponse.accessToken);
		editor.putLong(EXPIRES_IN, accessTokenResponse.expiresIn);
		editor.putString(REFRESH_TOKEN, accessTokenResponse.refreshToken);
		editor.putString(SCOPE, accessTokenResponse.scope);

		editor.commit();
	}

	public void writeUser(String googleId, String email, String ime, String slikaUrl) {
		Editor editor = prefs.edit();
		
		editor.putString(GOOGLEID, googleId);
		editor.putString(EMAIL, email);
		editor.putString(IME, ime);
		editor.putString(SLIKA_URL, slikaUrl);
		editor.commit();
	}
	public void writeMojID(int izbira)
	{
		Editor editor = prefs.edit();
		editor.putInt(MOJID, izbira);
		editor.commit();
	}
	public void writeGeslo(String sPassword)
	{
		Editor editor = prefs.edit();
		editor.putString(PASSWORD, sPassword);
		editor.commit();
	}
	public void clearMojID()
	{
		Editor editor = prefs.edit();
		editor.remove(MOJID);
		editor.commit();
	}
	public void clearCredentials() 
	{
		Editor editor = prefs.edit();
		editor.remove(ACCESS_TOKEN);
		editor.remove(EXPIRES_IN);
		editor.remove(REFRESH_TOKEN);
		editor.remove(SCOPE);
		editor.remove(IME);
		editor.remove(GOOGLEID);
		editor.remove(SLIKA_URL);
		editor.remove(MOJID);
		editor.remove(EMAIL);
		editor.remove(PASSWORD);
		editor.commit();
	}
}
