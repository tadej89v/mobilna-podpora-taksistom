package com.taxist.oauth2;

/**
 * Google apis console: "https://code.google.com/apis/console".
 */
public class CredentialsClient {

	/** Odjemalèev ID za namešèene aplikacije. */
	public static final String CLIENT_ID = "162447967717.apps.googleusercontent.com";

	/** "Client secret" za namešèene aplikacije. */
	public static final String CLIENT_SECRET = "";

	/** Podroèje uporabe OAuth 2. */
	public static final String SCOPE_LATITUDE = "https://www.googleapis.com/auth/latitude.all.best";
	public static final String SCOPE_PLUS = "https://www.googleapis.com/auth/plus.me";
	public static final String SCOPE_USERINFO = "https://www.googleapis.com/auth/userinfo.profile";
	public static final String SCOPE_USEREMAIL = "https://www.googleapis.com/auth/userinfo.email";
	public static final String SCOPE_DRIVE = "https://www.googleapis.com/auth/drive";
	/** Preusmerjen naslov OAuth 2. */
	public static final String REDIRECT_URI = "http://localhost";
	public static final String REDIRECT_URI_2 = "urn:ietf:wg:oauth:2.0:oob";

	/**
	 * Aplikacijski kljuè storitve Latitude ali druge Google storitve oz.
	 * aplikacije.
	 */
	public static final String API_KEY = "AIzaSyDWQ0eg4uGMzdu64jrxSFsjQERLqo_lWWs";
	/**
	 *  Povezava do lokacije skript, s katero upravljamo PB.
	 *  Skripte teèejo na strežniku MySQL, kjer se nahaja PB taksistov.
	 */
	public static final String hostDb = "http://www.lovse.net/tadejvrtacic/";
	// testiranje je potekalo na lokalni PB "http://192.168.1.3";
}
