package com.taxist.googleMaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

import com.taxist.oauth2.CredentialsClient;
import com.taxist.oauth2.CredentialsSharedPreferences;

/**
 * OAuthRequestTokenTaks izvrši pridobljeno zahtevo in jo pooblasti (avtorizira).
 * Ko je zahtevek odobren s strani uporabnika, ga bo povratni klic naslova (Callback URL) 
 * tukaj prestregel.
 * Pridobimo žeton za dostop in soveževanje, katere potrebujemo pri dostopu do API-jem
 * navedenih v parametru scope.
 */
public class OAuthAccessActivity extends Activity {

	final String TAG = getClass().getName();

	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.i(TAG, "Starting task to retrieve request token.");
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// new OAuthRequestTokenTask(this).execute();
	}
	@Override
	protected void onResume() {
		super.onResume();
		WebView webview = new WebView(this);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setVisibility(View.VISIBLE);
		setContentView(webview);
		String scope = CredentialsClient.SCOPE_USERINFO+" "+CredentialsClient.SCOPE_USEREMAIL+" "
				+ CredentialsClient.SCOPE_DRIVE;
		String authorizationUrl = new GoogleAuthorizationRequestUrl(
				CredentialsClient.CLIENT_ID, CredentialsClient.REDIRECT_URI,
				scope).build();
		//Google-ova avtorizacijska spletna stran, kjer pridobimo kodo, katero potrebujemo za dostop do API
		/* WebViewClient must be set BEFORE calling loadUrl! */
		webview.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap bitmap) {
				//System.out.println("onPageStarted : " + url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {

				if(url.startsWith(CredentialsClient.REDIRECT_URI)) {
					try {
						if(url.indexOf("code=") != -1) {
							//Avtorizacijska koda
							String code = pridobiKodoIzUrl(url);

							AccessTokenResponse accessTokenResponse = new GoogleAuthorizationCodeGrant(
									new NetHttpTransport(),
									new JacksonFactory(),
									CredentialsClient.CLIENT_ID,
									CredentialsClient.CLIENT_SECRET, code,
									CredentialsClient.REDIRECT_URI).execute();

							// skupna raba nastavitev, ki jih bomo potrebovali
							// za nadalnje API dostope
							CredentialsSharedPreferences credentialStore = new CredentialsSharedPreferences(
									prefs);
							credentialStore.write(accessTokenResponse);
							System.out.println("ACCESS TOKEN: "+accessTokenResponse.accessToken);
							//preberemo naše podatke
							userInfo(accessTokenResponse.accessToken,credentialStore);

							view.setVisibility(View.INVISIBLE);
							startActivity(new Intent(OAuthAccessActivity.this,GoogleMaps.class));
						}else if (url.indexOf("error=") != -1) {
							view.setVisibility(View.INVISIBLE);
							new CredentialsSharedPreferences(prefs)
									.clearCredentials();
							startActivity(new Intent(OAuthAccessActivity.this,
									GoogleMaps.class));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			private String pridobiKodoIzUrl(String url) {
				return url.substring(
						CredentialsClient.REDIRECT_URI.length() + 7,
						url.length());
			}
		});
		webview.loadUrl(authorizationUrl);
		//preberiIzUrl(authorizationUrl);
	}
	//s pomoèjo žetona za dostop pridobimo vse naše kljuène podatke raèuna Google
	private void userInfo(String access, CredentialsSharedPreferences credential) {
		try {
			if (!access.isEmpty()) {
				JSONObject json = readJsonFromUrl("https://www.googleapis.com/oauth2/v2/userinfo?access_token="
						+ access);
				
				String picture = "";
				if(json.has("picture"))
					picture = json.get("picture").toString();
				credential.writeUser(json.get("id").toString(),
									json.get("email").toString(),
									json.get("name").toString(),
									picture);
			}

		} catch (Exception e) {
			System.out.println("Napaka ime!");
		}
	}
	//preberemo podatke formata JSON iz url-ja
	public static JSONObject readJsonFromUrl(String url) throws IOException,
			JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
}
