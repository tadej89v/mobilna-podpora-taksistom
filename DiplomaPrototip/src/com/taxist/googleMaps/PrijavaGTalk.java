package com.taxist.googleMaps;

import com.taxist.oauth2.CredentialsSharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class PrijavaGTalk extends Activity {

	/**
	 * Aktivnost PrijavaGTalk nam po uspešni avtentikaciji v raèun Google zahteva
	 * naše ponovno geslo. To je zaradi varnosti in uporabe direktnega strežnika Google
	 * za komuniciranje med uporabniki. Za povezavo s strežnikom uporabljamo protokol XMPP.
	 * Ob prijavi se naši podatki shranijo za nadalnjo uporabo.
	 * V aktivnosti preverjamo prisotnost našega raèuna v naši podatkovni bazi MySQL.
	 * S pomoèjo tega definiramo za katero vrsto uporabnika gre (taksist ali stranka).
	 */
	private String mName;
	private String mEmail;
	private String mPassword;
	private TextView mEmailView;
	private EditText mPasswordView;
	private SharedPreferences prefs;
	private DbConnect connect = null;
	private CredentialsSharedPreferences credential = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prijava_gtalk);
		
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//vstavimo zapomjen email
		mName = prefs.getString("ime", "");
		mEmail = prefs.getString("email","");
		
		TextView mNameView = (TextView) findViewById(R.id.tvName);
		mEmailView = (TextView) findViewById(R.id.tvEmail);
		mPasswordView = (EditText) findViewById(R.id.passGTalk);
		
		if(mEmail.compareTo("")!=0 && mName.compareTo("")!=0){
			mNameView.setText(mName);
			mEmailView.setText(mEmail);
		}
		else
			startActivity(new Intent(this, OAuthAccessActivity.class));
		connect = new DbConnect();
		credential = new CredentialsSharedPreferences(prefs);
		if(!isOnline())
			startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),0);
		
		
		credential.writeMojID(connect.checkDb(mEmail));
		
		
		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						prijavaGTalk();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.prijava_gtalk, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.izhodTalk:
			izhodIzAplikacije();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	public void prijavaGTalk()
	{
		TextView tvNapaka = (TextView)findViewById(R.id.tvNapaka);
		mPassword = mPasswordView.getText().toString();
		
		Komunikacija kom = new Komunikacija();
		
		if(kom.povezi(mEmail, mPassword))
		{
			tvNapaka.setVisibility(View.INVISIBLE);
			CredentialsSharedPreferences credentialStore = new CredentialsSharedPreferences(prefs);
			
			credentialStore.writeGeslo(mPassword);
			kom.prekini();
			startActivity(new Intent(this, GoogleMaps.class));
		}
		else
			tvNapaka.setVisibility(View.VISIBLE);
	}
	private void izhodIzAplikacije() 
	{
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.izhod);
		
		Button btnPotrdi = (Button)dialog.findViewById(R.id.id_izhod_potrdi);
		Button btnIzhod = (Button)dialog.findViewById(R.id.id_izhod_preklici);
		
		btnPotrdi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.exit(0);
			}
		});
		btnIzhod.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	//Preverimo èe smo povezani s omrežjem 
	public boolean isOnline() {
	 	ConnectivityManager cm = (ConnectivityManager)
	 	getSystemService(Context.CONNECTIVITY_SERVICE); 
	 	NetworkInfo netInfo = cm.getActiveNetworkInfo(); 
	 	if (netInfo != null && netInfo.isConnected())
	 		return true; 
	 	else 
	 		return false; 
	}
}
