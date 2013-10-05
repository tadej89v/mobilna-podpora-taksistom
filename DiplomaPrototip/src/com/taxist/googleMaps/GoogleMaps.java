package com.taxist.googleMaps;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.taxist.oauth2.CredentialsSharedPreferences;

public class GoogleMaps extends FragmentActivity implements OnClickListener {

	/**
	 * Osrednja aktivnost naše aplikacije, ki skrbi za prikaz zemljevida Google Maps.
	 * Nad zemljevidom lahko izvajamo razliène operacije glede na vlogo aplikacije.
	 * TAKSIST: nastavitev zasedenosti, komunikacija s stranko, nadzor zgodovine lokacij,
	 * 			posodobitev svoje trenutne lokacije, prikaz poti do stranke in cilja stranke...
	 * STRANKA: izbira cilja, izbira taksista, prikaz poti do cilja, 
	 * 			informacije o prihodu taksista in cena storitve, komunikacija s taksistom,
	 * 			pregled urejene liste dosegljivih taksitov, itd...
	 */
	private GoogleMap mMap;
	private UiSettings mUiSettings;
	private SharedPreferences prefs;
	//private boolean isApiAccess = false;
	//private static GoogleLatitude razredLat;
	//private Latitude latitude;
	private static GPS gps;
	private double pozicijaX = 0;
	private double pozicijaY = 0;
	private Marker mojaLokacija = null;
	private int dosegljiv = 1;
	private Marker mojCilj = null;
	private static DbConnect connect;
	public static HashMap<String, Taxi> vsiTaxisti;
	private static HashMap<Marker, Taxi> prikazaniTaxisti;
	//private static int vloga;
	private static String mojeIme;
	private static Komunikacija komunikacija;
	private String IDdatoteke = "0B8UKYUuS-YFkeXRzMWM4a0hHVW8";
	private static String mojEmail;
	private static int mojID;
	private String mojeGeslo;
	private String emailStranke;
	private LatLng ciljStranke = null;
	private LatLng pozStranke = null;
	private boolean narocenTaxi = false;
	private boolean jePovezano = false;
	private Runnable returnDialog;
	private ProgressDialog progressDialog = null;
	private ToggleButton tBtn;
	private Ringtone rTone;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_google_maps);
		
		
		// preberemo nastavitve
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		//tukaj preverimo za kakšen namen aplikacije gre (taxist ali stranka)
		tBtn = (ToggleButton) findViewById(R.id.btn_prost);
		tBtn.setOnClickListener(this);
		tBtn.setChecked(true);
		
		Button btnTocka = (Button) findViewById(R.id.id_tocka);
		btnTocka.setOnClickListener(this);
		Button btnSlika = (Button) findViewById(R.id.id_seznam);
		btnSlika.setOnClickListener(this);
		
		navigacija();
		/*
		vloga = prefs.getInt("vloga", 0);
		//preverimo za kakšno vlogo aplikacije gre
		if(vloga == 1)
		{
			System.out.println("TAXIST");
			btnTocka.setVisibility(View.GONE);
			// Google Latitude
			//razredLat = new GoogleLatitude(prefs, this);
			//preverimo ali so Google storitve omogoèene
			/*if (!razredLat.ApiCall()) {
				avtorizacija();
			} 
			else{
				isApiAccess = true;
				// pridobimo lokacijo iz storitve Latitude
				//pridobiLokacijoLatitude();
			}
			
			isApiAccess = true;
			
		}
		else if(vloga == 2)
		{
			System.out.println("STRANKA");
			tBtn.setVisibility(View.GONE);
		}
		else
			startActivity(new Intent(this, VstopnaStran.class));
		*/
		//preverimo ali imamo še veljaven žeton, èe ne ga moramo pridobiti
		if(!prefs.contains("email"))
			startActivity(new Intent(this, OAuthAccessActivity.class));

		mojEmail = prefs.getString("email", "");
		mojeGeslo = prefs.getString("password", "");
		if(mojeGeslo.compareTo("") == 0)
			startActivity(new Intent(this, PrijavaGTalk.class));
		
		connect = new DbConnect(this);
		
		mojID = prefs.getInt("mojID", 0);
		System.out.println("MOJ ID ---->"+mojID);
		if(mojID > 0)
			btnTocka.setVisibility(View.GONE);
		else if(mojID == 0)
			tBtn.setVisibility(View.GONE);
		//zbirka vseh taxistov
		vsiTaxisti = new HashMap<String, Taxi>();
		prikazaniTaxisti = new HashMap<Marker, Taxi>();
		
		if(isOnline())
		{
			System.out.println("POVEZANI Z NETWORK");
		}
		nastaviOsvetlitevZaslona();
		//pobrisiMapo();
		setUpMapIfNeeded();
		
		
		//preberemo in prikažemo vse taxiste na mapi
		posodobiVse();

		Uri notification = RingtoneManager.getValidRingtoneUri(this);//DefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		rTone = RingtoneManager.getRingtone(this, notification);
		//povezava s strežnikom GTalk
		povezavaGTalk();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		//navgacija
		//navigacija();
		//preberemo in prikažemo vse taxiste na mapi
		posodobiVse();
		Bundle extras = getIntent().getExtras();
		if(extras != null && mojID == 0)
		{
			String email = extras.getString("emailTaxi");
			String cilj = extras.getString("cilj"); 

			if(mojCilj != null && email != null)
			{
				String prevoz = "PREVOZ:"+mojCilj.getPosition().latitude+":"+mojCilj.getPosition().longitude+":"+pozicijaX+":"+pozicijaY;
				komunikacija.poslji(email,prevoz);	
				
				progressDialog = ProgressDialog.show(GoogleMaps.this,    
			              "Prosimo poèakajte...", "Èakanje odgovora taxista...", true);
			}
			else
			{
				izbiraCilja();
			}
		}
	}

	private void setUpMapIfNeeded() {
		// preberemo nastavitve
		//this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//navigacija();
		// Preverimo ali že imamo zagnani zemljevid.
		if (mMap == null) {
			// Poskušamo pridobiti karto razreda SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Preverimo ali smo bili uspešni pri pridobitvi zemljevida.
			if (mMap != null) {
				setUpMap();
			}
		} else
			setUpMap();
		// preverimo ali so pridobljeni naši podatki
		if (prefs.contains("ime") && prefs.contains("slikaUrl"))
		{
			mojeIme = prefs.getString("ime", "");
			mojiPodatki(mojeIme, prefs.getString("slikaUrl", ""));
		}
	}

	private void setUpMap() {
		
		// nastavimo tip mape
		if (prefs.contains("key_pogled_mape"))
			tipMape(Integer.parseInt(prefs.getString("key_pogled_mape", "")));
		// druge nastavitve mape
		mUiSettings = mMap.getUiSettings();
		// kompas
		if (prefs.getBoolean("key_kompas", false))
			mUiSettings.setCompassEnabled(true);
		else
			mUiSettings.setCompassEnabled(false);
		// zoom kontrole
		if (prefs.getBoolean("key_zoom", true))
			mUiSettings.setZoomControlsEnabled(true);
		else
			mUiSettings.setZoomControlsEnabled(false);

		/*
		// privzete nastavitve
		if (prefs.getString("key_privzeto", "").equals("privzeto"))
			System.out
					.println("privzeto" + prefs.getString("key_privzeto", ""));
		else
			System.out.println("ni privzeto"
					+ prefs.getString("key_privzeto", ""));
		*/

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
	// Enable GPS
	private void pobrisiMapo()
	{
		mMap.clear();
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		if(pozicijaX != 0 && pozicijaY != 0)
			posodobiMene(pozicijaX,pozicijaY, cal.getTime(), 0);
	}
	private void gpsOn() {
		String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps")){
            final Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            intent.setData(Uri.parse("3"));
            sendBroadcast(intent);
        }
		/*
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", true);
		sendBroadcast(intent);*/
	}

	// Disable GPS
	private void gpsOff() {
		 String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	        if(provider.contains("gps")){
	            final Intent intent = new Intent();
	            intent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
	            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
	            intent.setData(Uri.parse("3"));
	            sendBroadcast(intent);
	        }
		/*
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", false);
		sendBroadcast(intent);*/
	}

	private void pocistiStaroLokacijo(double x, double y) {
		if (x != pozicijaX || y != pozicijaY)
			mojaLokacija.remove();
	}
	private boolean userInfo() {
		try {
			CredentialsSharedPreferences credential = new CredentialsSharedPreferences(this.prefs);
			AccessTokenResponse accessTokenResponse = credential.readAccessToken();
			String access = accessTokenResponse.accessToken;

			if (!access.isEmpty()) {
				JSONObject json = readJsonFromUrl("https://www.googleapis.com/oauth2/v1/userinfo?access_token="
						+ access);

				credential.writeUser(json.get("id").toString(),
									json.get("email").toString(),
									json.get("name").toString(),
									json.get("picture").toString());
				return true;
			}
			return false;

		} catch (Exception e) {
			System.out.println("Error branje podatkov! "+e);
			return false;
		}
	}
	public void posodobiMene(double x, double y, Date cas, double hitrost) 
	{
		if(x != 0 && y != 0)
		{
			LatLng jazLokacija = new LatLng(x, y);
			//if(vloga == 1)
				// najprej poèistimo staro lokacijo
				
			mojaLokacija = mMap.addMarker(new MarkerOptions()
					.position(jazLokacija)
					.title("To sem jaz.")
					.snippet("Posodobljeno pred: " + casPosodobitve(cas))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location)));
					//defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
			
			posodobitevCasa(cas);	
				
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(jazLokacija).zoom(10).build();
	
			mMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			
			pozicijaX = x;
			pozicijaY = y;
		}
	}
	public void posodobiVse() 
	{
		//narocenTaxi()
		if(mojCilj == null)
		{
			pobrisiMapo();
			//branje podatkov iz storitve Google Latitude
			//preberiTaxisteDrive("urlUporabnikov");
			//tukaj preberemo podatke iz baze...
			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			posodobiMene(pozicijaX, pozicijaY, cal.getTime(), 0);
			preberiTaxisteIzBaze();
			try{
				
			
			ArrayList<Taxi> list = new ArrayList<Taxi>(vsiTaxisti.values());
			for(int i=0;i<list.size();i++)
		    {
				Taxi taxi = list.get(i);
		        if(taxi.getDosegljiv() == 0 && !(taxi.getEmail().equals(mojEmail)))
		        {
					Date d = new Date(taxi.getTimestamp());
					cal.setTime(d);
					
			        //Drawable slika = naloziSliko(taxi.getSlikaUrl()); //TablicaUrl());
	
			        Marker marker = mMap.addMarker(new MarkerOptions()
											.position(taxi.getLokacija())
											.title(taxi.getIme()+" "+taxi.getPriimek())
											.snippet("Posodobljeno pred: "+ casPosodobitve(timesToDate(taxi.getTimestamp())))
											.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_taxi_show)));//BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
	
			        prikazaniTaxisti.put(marker, taxi);
		        }
		    }
			}
			catch(Exception e)
			{
				System.out.println("Napaka posodobitve!! "+e);
			}
		}
		if(prefs.getInt("mojID", 0) <= 0)
			izbiraTaxista(mMap);
	}
	// nastavimo da je osvetlitev ekrana stalna
	private void nastaviOsvetlitevZaslona() 
	{
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public void pokaziObvestilo(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
	}
	
	private void posodobitevCasa(Date d) {
		final Date cas = d;
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				if(marker.getId().equals(mojaLokacija.getId()))
					marker.setSnippet("Posodobljeno pred: " + casPosodobitve(cas));
				return false;
			}
		});
	}
	public void izbiraCilja()
	{
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.izbira_cilja);
		
		Button btnPotrdi = (Button) dialog.findViewById(R.id.id_cilj_potrdi);
		Button btnDoloci = (Button) dialog.findViewById(R.id.id_cilj_tocka);
		Button btnIzhod = (Button) dialog.findViewById(R.id.id_cilj_izhod);
		final Button btnPreklici = (Button) dialog.findViewById(R.id.id_cilj_preklici);
		final TextView tvObstaja = (TextView) dialog.findViewById(R.id.id_cilj_obstaja);
		
		if(mojCilj != null)
		{
			tvObstaja.setText("Moj cilj: "+mojCilj.getSnippet());
			tvObstaja.setVisibility(View.VISIBLE);
			btnPotrdi.setText("Spremeni cilj");
			btnDoloci.setText("Spremeni lokacijo..");
			btnPreklici.setVisibility(View.VISIBLE);
		}
		
		btnPotrdi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText etNaslov = (EditText) dialog.findViewById(R.id.id_cilj_naslov);
				TextView tvNapaka = (TextView) dialog.findViewById(R.id.id_cilj_napaka);
				if(etNaslov.getText().length() > 0)
				{
					if(najdiNaslov(etNaslov.getText().toString()))
					{
						tvNapaka.setVisibility(View.INVISIBLE);
						dialog.dismiss();
					}
					else
						tvNapaka.setVisibility(View.VISIBLE);
				}
				else
					tvNapaka.setVisibility(View.VISIBLE);
			}
		});
		btnDoloci.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMap.setOnMapClickListener(new OnMapClickListener() {
					
					@Override
					public void onMapClick(LatLng lok) {
						// TODO Auto-generated method stub
						if(mojCilj != null)
						{
							mojCilj.setPosition(lok);
							mojCilj.setSnippet(razdaljaTaxi(lok)[4]);
						}
						else
						{
						mojCilj = mMap.addMarker(new MarkerOptions()
									.position(lok)
									.title("Moj cilj..")
									.snippet(razdaljaTaxi(lok)[4])
									.icon(BitmapDescriptorFactory
											.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
						}
						potrditevCilja();
					}
				});
				dialog.dismiss();
			}
		});
		btnIzhod.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		btnPreklici.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mojCilj.remove();
				mojCilj = null;
				posodobiVse();
				tvObstaja.setVisibility(View.GONE);
				btnPreklici.setVisibility(View.GONE);
				TextView tvPrikazi = (TextView) findViewById(R.id.id_cilj_pokazi);
				LinearLayout linear = (LinearLayout) findViewById(R.id.id_linear);
				
				tvPrikazi.setVisibility(View.INVISIBLE);
				linear.setVisibility(View.INVISIBLE);
			}
		});
		
		dialog.show();
	}
	private void potrditevCilja()
	{	
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.izbira_tocke);
		
		TextView tvNaslov = (TextView) dialog.findViewById(R.id.id_tocka_naslov);
		Button btnPotrdi = (Button) dialog.findViewById(R.id.id_potrdi_tocko);
		Button btnPreklici = (Button) dialog.findViewById(R.id.id_preklici_tocko);
		
		tvNaslov.setText(mojCilj.getSnippet());
		
		btnPotrdi.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMap.setOnMapClickListener(null);
				dialog.dismiss();
				//najdiPot(new LatLng(pozicijaX,pozicijaY), mojCilj.getPosition());
			}
			
		});
		btnPreklici.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				mojCilj.remove();
			}
			
		});
		dialog.show();
	}
	public void izbiraTaxista(GoogleMap map) {
		try {
			
			map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

				@Override
				public void onInfoWindowClick(Marker m) {
					// TODO Auto-generated method stub
					if(prikazaniTaxisti.containsKey(m))
					{
						Taxi taxi = prikazaniTaxisti.get(m);
						if(!taxi.equals(null))
						{
						final Dialog dialog = new Dialog(GoogleMaps.this);
						//dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
						dialog.setContentView(R.layout.izbira_taxi);
						dialog.setTitle(taxi.getIme()+" "+taxi.getPriimek());
						
						ImageView image = (ImageView)dialog.findViewById(R.id.id_izbira_slika);
						TextView tvCas = (TextView)dialog.findViewById(R.id.id_izbira_cas);
						TextView tvEmail = (TextView)dialog.findViewById(R.id.id_izbira_email);
						TextView tvRazdalja = (TextView)dialog.findViewById(R.id.id_izbira_razdalja);
						TextView tvNaslov = (TextView)dialog.findViewById(R.id.id_izbira_naslov);
						Button btnOk  = (Button)dialog.findViewById(R.id.id_izbira_ok);
						Button btnCancel  = (Button)dialog.findViewById(R.id.id_izbira_cancel);
						
						if(naloziSliko(taxi.getSlikaUrl())!=null)
							image.setImageDrawable(naloziSliko(taxi.getSlikaUrl()));
						
						tvEmail.setText(taxi.getEmail());
						tvCas.setText("Èas prihoda: "+taxi.getCas());
						
						BigDecimal bd = new BigDecimal((double)taxi.getRazdalja()/1000).setScale(1, RoundingMode.HALF_EVEN);
						tvRazdalja.setText("Oddaljenost: "+bd.doubleValue());
						tvNaslov.setText(taxi.getNaslov());
						
						
						final String email = taxi.getEmail();
						
						if(prefs.getInt("vloga", 0) == 2)
						{
							btnOk.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									if(mojCilj != null)
									{
										if(jePovezano)
										{
											String prevoz = "PREVOZ:"+mojCilj.getPosition().latitude+":"+mojCilj.getPosition().longitude+":"+pozicijaX+":"+pozicijaY;
											komunikacija.poslji(email,prevoz);	
											
											progressDialog = ProgressDialog.show(GoogleMaps.this,    
										              "Prosimo poèakajte...", "Èakanje odgovora taxista...", true);
										}
										else
											pokaziObvestilo("Napaka v povezavi!");
									}
									else
									{
										izbiraCilja();
									}
									dialog.dismiss();
								}
							});
						}
						else if(prefs.getInt("vloga", 0) == 1)
							btnOk.setVisibility(View.INVISIBLE);
						
						btnCancel.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
						
						dialog.show();
						}
					}
				}
			});
		} 
		catch (Exception e) {
			System.out.println("Napaka pri izbiri taksista :"+e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.google_maps, menu);
		return true;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
			case R.id.id_seznam:
				//branje podatkov iz storitve Google Latitude
				//preberiTaxisteDrive("urlUporabnikov");
				//tukaj preberemo podatke iz baze...
				preberiTaxisteIzBaze();
				posodobiVse();
				
				Intent i = new Intent(this, ListaTaxistov.class);
				Bundle bundle = new Bundle();
				if(mojID > 0)
				{
					ArrayList<Taxi> listTaxi = new ArrayList<Taxi>(vsiTaxisti.values());
					bundle.putParcelableArrayList("listTaxi",listTaxi);
					i.putExtras(bundle);
				}
				else
				{
					ArrayList<Taxi> listTaxi = listaDosegljiviTaxistov();
					Collections.sort(listTaxi,new CustomComparator());
					bundle.putParcelableArrayList("listTaxi",listTaxi);
					i.putExtras(bundle);
				}
				
				startActivity(i);
				break;
			case R.id.btn_prost:
				ToggleButton tBtn2 = (ToggleButton) view;
				Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
				if(tBtn2.isChecked())
				{
					dosegljiv = 1;
					skrijPodatke();
					//posodobitev baze
					posodobiLokacijoDb(pozicijaX, pozicijaY, true);
					//GoogleLatitude.posodobiTrenutnoLokacijo(latitude, pozicijaX, pozicijaY, cal.getTime());
					posodobiVse();
					narocenTaxi = false;
				}
				else
				{
					dosegljiv = 0;	
					//posodobitev baze
					posodobiLokacijoDb(0, 0, false);
					//GoogleLatitude.posodobiTrenutnoLokacijo(latitude, -1, -1, cal.getTime());
				}
				break;
			case R.id.id_tocka:
				izbiraCilja();
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.izhod:
			izhodIzAplikacije();
			return true;
		case R.id.settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.odjava:
			odjava();
			return true;
		case R.id.zgodovina:
			if(mojID > 0)
			{
				pobrisiMapo();
				zgodovinaLokacij();
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void zgodovinaLokacij()
	{
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.zgodovina);
		
		final RadioButton rbSt = (RadioButton)dialog.findViewById(R.id.id_zgodovina_st);
		final RadioButton rbCas = (RadioButton)dialog.findViewById(R.id.id_zgodovina_cas);
		Button btnZgod = (Button)dialog.findViewById(R.id.id_zgodovina_potrdi);
		Button btnIzhod = (Button)dialog.findViewById(R.id.id_zgodovina_izhod);
		
		
		btnZgod.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//lista lokacij storitve Google Latitude
				//List<Location> zgodovina = null;
				ArrayList<LatLng> zgodovina = new ArrayList<LatLng>();
				String cas = prefs.getString("key_zgodovina_cas", "");
				String st = prefs.getString("key_zgodovina_st","");
				
				if(!(cas.equals("") || st.equals("")))
				{
					try{
						if(rbSt.isChecked())
							zgodovina = connect.getZgodovina(mojID, st, "0");
							//zgodovina = GoogleLatitude.prikaziZgodovinoLokacijSt(latitude, st);
						else if(rbCas.isChecked())
							zgodovina = connect.getZgodovina(mojID, "0",timestampToString(cas));
							//zgodovina = GoogleLatitude.prikaziZgodovinoLokacijCas(latitude, cas);
						mMap.clear();
						for(int i=0; i<zgodovina.size();i++)
						{
							//Location je spremenljivka razreda Latitude
							//Location lok = zgodovina.get(i);
							//LatLng poz = new LatLng(Double.parseDouble(lok.latitude.toString()), Double.parseDouble(lok.longitude.toString()));
							
							LatLng lok = zgodovina.get(i);
							Marker zgod = mMap.addMarker(new MarkerOptions()
											.position(lok));
							
						}
					}
					catch(Exception e)
					{
						System.out.println("Napaka pri iskanju zgodovine");
					}
				}
				dialog.dismiss();
			}
		});
		btnIzhod.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
	private String timestampToString(String min)
	{	
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		Date zdaj = cal.getTime();
		int minInt = Integer.parseInt(min);
		// delimo s 60000 da dobimo minute...drugaèe rezultat dobimo v ms
		long diff = Math.abs(zdaj.getTime()) / 60000 - minInt;
		
		Date nov = new Date(diff*60000);
		
		return df.format(nov);
	}
	public int getDosegljiv()
	{
		return dosegljiv;
	}
	private ArrayList<Taxi> listaDosegljiviTaxistov()
	{
		ArrayList<Taxi> list = new ArrayList<Taxi>();

		Iterator i = vsiTaxisti.keySet().iterator();
	    
		while(i.hasNext()) 
		{
			String key = (String)i.next();
			Taxi taxi = vsiTaxisti.get(key);
			if(taxi.getDosegljiv() == 0 && !(taxi.getEmail().equals(mojEmail)))
				list.add(taxi);
		}
		return list;
	}
	// poèistimo use nastavitve za avtorizacijo s Google Apis
	public void clearCredentials() {
		CredentialsSharedPreferences cred = new CredentialsSharedPreferences(prefs);
		cred.clearCredentials();
		//isApiAccess = false;
	}
	public Komunikacija getObjektKomunikacija()
	{
		return komunikacija;
	}
	/*
	public void ustvariObjektLatitude(Latitude lat) {
		latitude = lat;
	}
	private boolean pridobiLokacijoLatitude() {
		// pridobitev lokacije
		try {
			LatitudeCurrentlocationResourceJson currentLocation = latitude.currentLocation
					.get().execute();
			// prikaz na mapi
			
			LatitudeLocation(currentLocation);
			return true;
		} catch (Exception e) {
			return false;
		}
	}*/
	private Taxi pridobiLokacijoTaxistaLatitude(String userId)
	{
		Taxi taxist = null;
		int zasedenost;
		try{
			JSONObject json = readJsonFromUrl("http://latitude.google.com/latitude/apps/badge/api?user="+userId+"&type=json");
			
			if(json.getJSONArray("features").isNull(0))
			{
				zasedenost = -1;
				taxist = new Taxi(userId,zasedenost);
			}
			else
			{
				
				JSONObject properties = json.getJSONArray("features").getJSONObject(0).getJSONObject("properties");
				long timeStamp = properties.getLong("timeStamp");
				String naslov = properties.getString("reverseGeocode");
				String slikaUrl = properties.getString("photoUrl");
				String tablicaUrl = properties.getString("placardUrl");
				JSONObject geometry = json.getJSONArray("features").getJSONObject(0).getJSONObject("geometry");;
				JSONArray koordinate = geometry.getJSONArray("coordinates");
				LatLng lok = new LatLng(koordinate.getDouble(1), koordinate.getDouble(0));
				if(lok.latitude == -1 && lok.longitude == -1)
				{
					zasedenost = -1;
					taxist = new Taxi(userId,zasedenost);
				}
				else
				{
					zasedenost = 0;
				
					String[] tab = razdaljaTaxi(lok);
					int razd=0;
					if(!tab[0].equals("prazno"))
						razd=Integer.parseInt(tab[0]);
					if(!tab[4].equals("prazno"))
						naslov = tab[4];
					taxist = new Taxi(userId, naslov, timeStamp, lok, slikaUrl, tablicaUrl, zasedenost, razd, tab[2]);
				}
			}
			return taxist;
		}
		catch(Exception e)
		{
			System.out.println("Napaka pri branju JSONObject: "+e);
			return null;
		}
	}
	private void preberiTaxisteIzBaze()
	{
		vsiTaxisti = connect.getTaxiMap();
	}
	private void preberiTaxisteDrive(String url)
	{
		try {
			vsiTaxisti.clear();
			JSONObject json = readJsonFromUrl("https://googledrive.com/host/0B8UKYUuS-YFkeUNnU2x6MnN3R0k/taxisti.json");

			JSONArray prebraniTaxisti = json.getJSONArray("taxisti");
			for(int i=0;i<prebraniTaxisti.length();i++)
			{
				String id = prebraniTaxisti.getJSONObject(i).getString("userId");
				String ime = prebraniTaxisti.getJSONObject(i).getString("ime");
				String email = prebraniTaxisti.getJSONObject(i).getString("email");
				
				if(!email.equals(mojEmail))
				{
					Taxi taxi = pridobiLokacijoTaxistaLatitude(id);
					taxi.setDodatno(ime, email);
					vsiTaxisti.put(email, taxi);
				}
			}
			
		}
		catch (Exception e) 
		{
			System.out.println("Napaka pri branju taxistov!! "+e);
			e.printStackTrace();
		}
	}
	public String[] razdaljaTaxi(LatLng lokTaxi)
	{
		String[] tab = new String[6];
		try {
			String url = "http://maps.googleapis.com/maps/api/directions/json?origin="
						+pozicijaX+","+pozicijaY+"&destination="
						+lokTaxi.latitude+","+lokTaxi.longitude+"&sensor=false&mode=driving";

			JSONObject json = readJsonFromUrl(url);
			
			if(json.getString("status").equals("OK"))
			{
				JSONArray pot = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
				JSONObject razdalja = pot.getJSONObject(0).getJSONObject("distance");
				JSONObject trajanje = pot.getJSONObject(0).getJSONObject("duration");
				
				tab[0] = Integer.toString(razdalja.getInt("value"));
				tab[1] = razdalja.getString("text");
				tab[2] = trajanje.getString("text");
				tab[3] = pot.getJSONObject(0).getString("start_address");
				tab[4] = pot.getJSONObject(0).getString("end_address");
				tab[5] = Integer.toString(trajanje.getInt("value"));
			}
			else{
				System.out.println("pot ne obstaja>"+json.getString("status"));
				tab[0] = "prazno";
				tab[1] = "prazno";
				tab[2] = "prazno";
				tab[3] = "prazno";
				tab[4] = "prazno";
			}
		} 
		catch (Exception e) 
		{
			System.out.println("Napaka pri iskanju poti: "+e);
		}
		return tab;
	}
	private boolean najdiNaslov(String naslov)
	{
		String naslov2 = "";
		String[] tab = naslov.split(" ");
		for(int i=0; i<tab.length; i++)
		{
			naslov2 += tab[i];
			if(i<tab.length-1)
				naslov2 += "%20";
		}
		boolean jeNajden = false;
		try {
			String url = "http://maps.googleapis.com/maps/api/directions/json?origin="
							+pozicijaX+","+pozicijaY+"&destination="
							+naslov2+"&sensor=false&mode=driving";

			JSONObject json = readJsonFromUrl(url);
			
			if(json.getString("status").equals("OK"))
			{
				JSONArray pot = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
				JSONObject razdalja = pot.getJSONObject(0).getJSONObject("distance");
				JSONObject trajanje = pot.getJSONObject(0).getJSONObject("duration");
				JSONObject lokacijaCilja = pot.getJSONObject(0).getJSONObject("end_location");
				
				LatLng lokCilj = new LatLng(lokacijaCilja.getDouble("lat"), lokacijaCilja.getDouble("lng"));
				if(mojCilj != null)
				{
					mojCilj.setPosition(lokCilj);
					mojCilj.setSnippet(pot.getJSONObject(0).getString("end_address"));
				}
				else
				{
					mojCilj = mMap.addMarker(new MarkerOptions()
								.position(lokCilj)
								.title("Moj cilj..")
								.snippet(pot.getJSONObject(0).getString("end_address"))
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
				}
				CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(mojCilj.getPosition()).zoom(10).build();
				mMap.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));
				
				jeNajden = true;
			}
			else
				jeNajden = false;
		} 
		catch (Exception e) 
		{
			System.out.println("Napaka pri iskanju naslova cilja: "+e);
		}
		return jeNajden;
	}
	private void najdiPot(LatLng start, LatLng end, String zac, String kon, int color)
	{	
		Pot pot = new Pot(start, end);
		
		ArrayList<LatLng> potDoCilja = pot.najdiPot();
		PolylineOptions rectLine = new PolylineOptions().width(3).color(color);
		
		addMarker(start.latitude, start.longitude, zac , pot.getZacetek());
		addMarker(end.latitude, end.longitude, kon, pot.getKonec());
		
		for (int i = 0; i < potDoCilja.size(); i++) {
			rectLine.add(potDoCilja.get(i));
		}
		mMap.addPolyline(rectLine);
	}
	private void prikazPodatkov(String email, String naslov, String km, String sek, String cena)
	{
		TextView tvPrikazi = (TextView) findViewById(R.id.id_cilj_pokazi);
		TextView tvKm = (TextView) findViewById(R.id.id_cilj_km);
		TextView tvMin = (TextView) findViewById(R.id.id_cilj_min);
		TextView tvCena = (TextView) findViewById(R.id.id_cilj_cena);
		LinearLayout linear = (LinearLayout) findViewById(R.id.id_linear);
		
		tvPrikazi.setVisibility(View.VISIBLE);
		linear.setVisibility(View.VISIBLE);
		
		String[] tabKm = km.split(" ");
		String[] tabEmail = email.split("/");
		int min = Integer.parseInt(sek)/60;
		String cas = "";
		if(min >= 60)
		{
			if(min%60 == 0)
				cas = (min/60)+"h";
			else
				cas = (min/60)+"h "+(min%60)+"min";
		}
		else
			cas = min+"min";
		if(mojID > 0)
			tvPrikazi.setText("Stranka: "+tabEmail[0]+"'\nCilj: "+naslov);
		else
			tvPrikazi.setText("Taxi: "+tabEmail[0]+"'\nCilj: "+naslov);
		tvKm.setText(tabKm[0]+tabKm[1]);
		tvMin.setText(cas);
		if(cena != null)
			tvCena.setText(cena+"€");
	}
	private void skrijPodatke()
	{
		TextView tvPrikazi = (TextView) findViewById(R.id.id_cilj_pokazi);
		LinearLayout linear = (LinearLayout) findViewById(R.id.id_linear);
		
		tvPrikazi.setVisibility(View.INVISIBLE);
		linear.setVisibility(View.INVISIBLE);
	}
	private void posodobiOddInCas(String km, String sek)
	{
		TextView tvKm = (TextView) findViewById(R.id.id_cilj_km);
		TextView tvMin = (TextView) findViewById(R.id.id_cilj_min);
		
		String[] tabKm = km.split(" ");
		int min = Integer.parseInt(sek)/60;
		String cas = "";
		if(min >= 60)
		{
			if(min%60 == 0)
				cas = (min/60)+"h";
			else
				cas = (min/60)+"h "+(min%60)+"min";
		}
		else
			cas = min+"min";
		
		tvKm.setText(tabKm[0]+tabKm[1]);
		tvMin.setText(cas);
	}
	private String[] izracunPrevoza(LatLng start, LatLng finish)
	{
		String[] tab = new String[3];
		try {
			String url = "http://maps.googleapis.com/maps/api/directions/json?origin="
						+start.latitude+","+start.longitude+"&destination="
						+finish.latitude+","+finish.longitude+"&sensor=false&mode=driving";

			JSONObject json = readJsonFromUrl(url);
			
			if(json.getString("status").equals("OK"))
			{
				JSONArray pot = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
				JSONObject razdalja = pot.getJSONObject(0).getJSONObject("distance");
				JSONObject trajanje = pot.getJSONObject(0).getJSONObject("duration");
				
				tab[0] = razdalja.getString("text");
				tab[1] = Integer.toString(trajanje.getInt("value"));
				tab[2] = pot.getJSONObject(0).getString("end_address");
			}
		} 
		catch (Exception e) 
		{
			System.out.println("Napaka pri izraèunu cene prevoza: "+e);
		}
		return tab;
	}
	public HashMap<String,Taxi> getSeznamTaxistov()
	{
		return GoogleMaps.vsiTaxisti;
	}
	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException 
	{
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
	private static String readAll(Reader rd) throws IOException 
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
	private Date timesToDate(long timestampMs) 
	{	
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date d = new Date(timestampMs);
		
		return d;
	}
	public void posodobiLokacijoDb(double lat, double lon, boolean jeProst)
	{
		boolean pos = connect.updateLocation(mojID, lat, lon, jeProst); 
		if(pos)
			pokaziObvestilo("Posodobljena lokacija!");
	}
	/*
	public void posodobiLatitudeLokacijo(double x, double y, Date d) {
			GoogleLatitude.posodobiTrenutnoLokacijo(latitude, x, y, d);
	}

	private void LatitudeLocation(
			LatitudeCurrentlocationResourceJson currentLocation) {
		String timestampMs = (String) currentLocation.get("timestampMs");
		Date d = new Date(Long.valueOf(timestampMs));
		
		posodobiMene(
				"Latitude",
				Double.parseDouble(currentLocation.get("latitude").toString()),
				Double.parseDouble(currentLocation.get("longitude").toString()),
				d, 0);
	}*/
	private void addMarker(double lat, double lon,String string, String string2) {
		mMap.addMarker(new MarkerOptions()
				.position(new LatLng(lat, lon))
				.title(string).snippet(string2));
	}
	private String casPosodobitve(Date d) {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		Date zdaj = cal.getTime();
		// delimo s 60000 da dobimo minute...drugaèe rezultat dobimo v ms
		long diff = Math.abs(zdaj.getTime() - d.getTime()) / 60000;

		if (diff < 60)
			return diff + "min";
		else if (diff < (60 * 24))
			return diff / 60 + "h " + diff % 60 + "min";
		else
			return diff / (24 * 60) + "dni " + diff % (24 * 60) / 60 + "h "
					+ (diff % (24 * 60)) % 60 + "min";
	}

	private void tipMape(int izbira) {
		if (izbira == 0)
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		else if (izbira == 1)
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		else if (izbira == 2)
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		else if (izbira == 3)
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
	}

	private void PrivzeteNastavitve() {
		PreferenceManager.setDefaultValues(this, R.xml.pref_latitude, true);
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
				gpsOff();
				komunikacija.prekini();
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
	private void avtorizacija()
	{
		startActivity(new Intent(this, OAuthAccessActivity.class));
	}
	private void odjava() 
	{
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.odjava);
		
		Button btnPotrdi = (Button)dialog.findViewById(R.id.id_odjava_potrdi);
		Button btnIzhod = (Button)dialog.findViewById(R.id.id_izhod_odjava);
		TextView tvRacun = (TextView)dialog.findViewById(R.id.id_racun_odjava);
		
		tvRacun.setText("raèuna: \n"+mojEmail);
		btnPotrdi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				komunikacija.prekini();
				clearCredentials();
				startActivity(new Intent(GoogleMaps.this, OAuthAccessActivity.class));
				finish();
			}
		});
		btnIzhod.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		dialog.show();
		/*
		if (!isApiAccess)
		{
			AlertDialog.Builder latOdjava = new AlertDialog.Builder(this);
			latOdjava
					.setTitle("Prijava Latitude")
					.setPositiveButton("Prijava",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									avtorizacija();
								}
							})
					.setNegativeButton("Preklièi", null).show();				
		}
		else 
		{
			AlertDialog.Builder latOdjava = new AlertDialog.Builder(this);
			latOdjava
					.setTitle("Se želite odjaviti?")
					.setMessage("raèun: "+mojEmail)
					.setCancelable(false)
					.setPositiveButton("Odjava",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									clearCredentials();
								}
							})
					.setNegativeButton("Preklièi", null).show();
			isApiAccess = false;
		}*/
	}

	private Drawable naloziSliko(String address) {
		try {
			URL url = new URL(address);
			InputStream is = (InputStream) url.getContent();

			Drawable d = Drawable.createFromStream(is, "src");
			return d;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	private void mojiPodatki(String ime, String slikaUrl) {
		TextView tIme = (TextView) findViewById(R.id.textView_ime);
		ImageView image = (ImageView) findViewById(R.id.image);

		tIme.setText(ime);
		if(!slikaUrl.equals(""))
		{
			Drawable slika = naloziSliko(slikaUrl);
			if(slika != null)
				image.setImageDrawable(slika);
		}
	}

	private void navigacija() {
		LocationManager lokacijskiManagerGps = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationManager lokacijskiManagerNet = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		gps = new GPS(lokacijskiManagerGps, lokacijskiManagerNet, GoogleMaps.this);

		// nastavitve GPS
		if (gps.jeGPSomogocen())
			gps.poveziGPS();
		else {
			gpsOn();
			gps.poveziGPS();
		}
		
		if (!gps.jeNetOmogocen()) {
			startActivityForResult(
					new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),0);
			gps.poveziNet();
		} 
		else
			gps.poveziNet();
	}
	public void signal(String provider) {
		TextView signal = (TextView) findViewById(R.id.textView_singal);
		signal.setText("Signal: " + provider);
	}
	private void povezavaGTalk()
	{
		try
		{
			komunikacija =  new Komunikacija(this);
			komunikacija.povezi(mojEmail, mojeGeslo);
			komunikacija.prejmi();
			jePovezano = true;
		}
		catch(Exception e)
		{
			System.out.println("Napaka pri povezavi na GTalk strežnik!");
		}
	}
	public void potrditevTaxista(String email, String sporocilo)
	{
		final String naslovnik = email;
		String[] tab = razcleniSporocilo(sporocilo);
		if(tab[0].equals("PREVOZ"))
		{

			ciljStranke = new LatLng(Double.parseDouble(tab[1]), Double.parseDouble(tab[2]));
			pozStranke = new LatLng(Double.parseDouble(tab[3]), Double.parseDouble(tab[4]));
			returnDialog = new Runnable() 
			{
		        @Override
		        public void run() {
					rTone.play();
					
					final Dialog dialog = new Dialog(GoogleMaps.this);
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.potrdi_stranko);
					TextView tvEmail = (TextView) dialog.findViewById(R.id.id_stranka_email);
					TextView tvNaslov = (TextView) dialog.findViewById(R.id.id_stranka_naslov);
					Button btnPotrdi = (Button) dialog.findViewById(R.id.id_potrdi_stranko);
					Button btnPreklici = (Button) dialog.findViewById(R.id.id_preklici_stranko);
					final Spinner spinner = (Spinner) dialog.findViewById(R.id.id_spinner);
					
					final String[] tabRaz = izracunPrevoza(new LatLng(pozicijaX, pozicijaY), ciljStranke);
					
					tvEmail.setText(naslovnik.split("/")[0]);
					tvNaslov.setText(tabRaz[2]);
					btnPotrdi.setOnClickListener(new OnClickListener()
					{
			
						@Override
						public void onClick(View v) {
							rTone.stop();
							// TODO Auto-generated method stub
							String tarifa = spinner.getSelectedItem().toString();
							double stevec = Double.parseDouble(tabRaz[0].split(" ")[0]) * Double.parseDouble(tarifa.split(" ")[0]);
							
							komunikacija.poslji(naslovnik,"TAXI:DA:"+pozicijaX+":"+pozicijaY+":"+stevec);
							dialog.dismiss();
							progressDialog = ProgressDialog.show(GoogleMaps.this,    
						              "Prosimo poèakajte...", "Èakanje potrditve STRANKE...", true);
						}
						
					});
					btnPreklici.setOnClickListener(new OnClickListener()
					{
			
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							komunikacija.poslji(naslovnik,"TAXI:NE");
							dialog.dismiss();
						}
						
					});
					dialog.show();
				}
			};
			runOnUiThread(returnDialog);
		}
		else if(tab[0].equals("STRANKA") && tab[1].equals("OK"))
		{
			
			dosegljiv = 0;
			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			//GoogleLatitude.posodobiTrenutnoLokacijo(latitude, -1, -1, cal.getTime());
			//posodobitev baze
			posodobiLokacijoDb(0, 0, false);
			
			narocenTaxi = true;
			emailStranke = naslovnik;
			returnDialog = new Runnable() 
			{
				@Override
		        public void run() {
					if(ciljStranke != null)
					{
						tBtn.setChecked(false);
						String[] tabRaz = izracunPrevoza(new LatLng(pozicijaX,pozicijaY), ciljStranke);
						prikazPodatkov(naslovnik, tabRaz[2], tabRaz[0], tabRaz[1], null);
						pobrisiMapo();
						najdiPot(new LatLng(pozicijaX, pozicijaY), pozStranke,"Start","Stranka",Color.BLUE);
						najdiPot(pozStranke,ciljStranke,"Stranka","Cilj stranke",Color.RED);
						progressDialog.dismiss();
					}
					else
						pokaziObvestilo("Prišlo je do napake v komunikaciji!");
				}
			};
			runOnUiThread(returnDialog);
		}
		else if(tab[0].equals("STRANKA") && tab[1].equals("NE"))
		{
			progressDialog.dismiss();
			tBtn.setChecked(true);
			dosegljiv = 1;
			narocenTaxi = false;
		}
	}
	public void potrditevStranke(String email, String sporocilo)
	{
		
		final String naslovnik = email;
		
		final String[] tab = razcleniSporocilo(sporocilo);
		if(tab[0].equals("TAXI") && tab[1].equals("DA"))
		{
			rTone.play();
			
			progressDialog.dismiss();
			try{
				final LatLng pozTaxi = new LatLng(Double.parseDouble(tab[2]),Double.parseDouble(tab[3]));
				returnDialog = new Runnable() 
				{
			        @Override
			        public void run() {
			        	final Dialog dialog = new Dialog(GoogleMaps.this);
			        	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
						dialog.setContentView(R.layout.potrdi_taxista);
						TextView tvIme = (TextView) dialog.findViewById(R.id.id_taxist_ime);
						TextView tvEmail = (TextView) dialog.findViewById(R.id.id_taxist_email);
						Button btnPotrdi = (Button) dialog.findViewById(R.id.id_potrdi_taxi);
						Button btnPreklici = (Button) dialog.findViewById(R.id.id_preklici_taxi);
						
						String[] tabEmail = naslovnik.split("/");
						final String ime = vsiTaxisti.get(tabEmail[0]).getIme()+" "+vsiTaxisti.get(tabEmail[0]).getPriimek();
						tvIme.setText(ime);
						tvEmail.setText(tabEmail[0]+"\nCENA = "+tab[4]+" EUR");
						
						btnPotrdi.setOnClickListener(new OnClickListener()
						{
							@Override
							public void onClick(View v) {
								rTone.stop();
								// TODO Auto-generated method stub
								komunikacija.poslji(naslovnik,"STRANKA:OK");
								String[] tab2 = izracunPrevoza(pozTaxi, mojCilj.getPosition());
														
								prikazPodatkov(naslovnik, tab2[2], tab2[0], tab2[1], tab[4]);
								pobrisiMapo();
								najdiPot(new LatLng(pozicijaX,pozicijaY), mojCilj.getPosition(),"Start","Moj cilj",Color.RED);
								dialog.dismiss();
							}
							
						});
						btnPreklici.setOnClickListener(new OnClickListener()
						{
				
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								komunikacija.poslji(naslovnik,"STRANKA:NE");
								dialog.dismiss();
							}
							
						});
						dialog.show();
			        }
				};
				runOnUiThread(returnDialog);
			}
			catch(Exception e)
			{
				System.out.println("NAPAKA PRI KOMUNIKACIJI! "+e);
				pokaziObvestilo("Napaka pri komunikaciji!");
			}
		}
		else if(tab[0].equals("TAXI") && tab[1].equals("POS"))
		{
			LatLng pozTaxi = new LatLng(Double.parseDouble(tab[2]),Double.parseDouble(tab[3]));
			
			final String[] tabPos = izracunPrevoza(pozTaxi, mojCilj.getPosition());
			returnDialog = new Runnable() 
			{
		        @Override
		        public void run() {
		        	int dolzina = Integer.parseInt(tabPos[0].split("/")[0]);
		        	if(dolzina <= 1)
		        	{
		        		pokaziObvestilo("Prispeli smo na cilj..\nŽelimo Vam lep dan!");
		        		skrijPodatke();
		        		pobrisiMapo();
		        		posodobiVse();
		        	}
		        	else
		        		posodobiOddInCas(tabPos[0], tabPos[1]);
		        }
			};
			runOnUiThread(returnDialog);
		}
		else if(tab[0].equals("TAXI") && tab[1].equals("NE"))
		{
			pokaziObvestilo("Taxist: \n"+email+" je zaseden!");
		}
	}
	public int getMojID()
	{
		return mojID;
	}

	private String[] razcleniSporocilo(String s)
	{
		String[] tab = s.split(":");
		
		return tab;
	}
	public boolean getNarocenTaxi()
	{
		return narocenTaxi;
	}
	public void posljiPosodobitev(double x, double y)
	{
		komunikacija.poslji(emailStranke, "TAXI:POS:"+x+":"+y);
		String[] tab = izracunPrevoza(new LatLng(x,y), ciljStranke);
		posodobiOddInCas(tab[0], tab[1]);
	}
}
class CustomComparator implements Comparator<Taxi> {
    @Override
    public int compare(Taxi t1, Taxi t2) {
        return t1.getRazdalja()-t2.getRazdalja();
    }
}
