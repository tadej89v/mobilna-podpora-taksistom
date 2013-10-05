package com.taxist.googleMaps;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class GPS {
	/**
	 * Razred GPS je namenjen pridobivanju trenutne lokacije našega telefona s pomoèjo
	 * dosegljivega ponudnika. (GPS ali network)
	 * V primeru izgube signala GPS, lokacijo pridobimo iz omrežja.
	 * Razred nam ponuja pridobitev osnovnih virov naše lokacije.
	 * To so: zemljepisna širina in dolžina, hitros ter natanènost.
	 */
	private static boolean zagonGPS = false;
	private static boolean zagonNet = false;
	private GoogleMaps gMaps = null;
	private GoogleLatitude latitude = null;
	private LocationManager lokacijskiManagerNet = null;
	private LocationManager lokacijskiManagerGps = null;
	// private LocationListener lokacijskiListener = null;

	private long casPosodobitve = 0;

	// podatki po katerih vemo da še ni prišlo do zaèetnih sprememb
	private double pozicijaX = -1;
	private double pozicijaY = -1;
	private double hitrost = -1;
	private double nadmVisina = -1;
	private double natancnost = -1;
	private int statusSignalaGPS = -1;
	private SharedPreferences prefs;
	
	public GPS(LocationManager lokManagerGPS, LocationManager lokManagerNet,
			GoogleMaps gM) {
		this.lokacijskiManagerGps = lokManagerGPS;
		this.lokacijskiManagerNet = lokManagerNet;
		this.gMaps = gM;
	}

	// poveže telefon s GPS ponudnikom | vrne false èe je ponudnik onemogoèen
	// oziroma GPS ni prižgan na telefonu
	public boolean poveziGPS() {
		if (jeGPSomogocen() && zagonGPS == false) {
			LocationListener lokacijskiListener = new LokPoslusalecGPS();

			lokacijskiManagerGps.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, lokacijskiListener);

			GPS.zagonGPS = true;

			return true;
		} else
			return false;
	}

	public boolean poveziNet() {
		if (jeNetOmogocen() && zagonNet == false) {
			LocationListener lokacijskiListener = new LokPoslusalecNet();

			lokacijskiManagerNet.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, lokacijskiListener);
			GPS.zagonNet = true;

			return true;
		} else
			return false;
	}

	// preveri ali je GPS vkljuèen
	public boolean jeGPSomogocen() {
		if (lokacijskiManagerGps
				.isProviderEnabled(LocationManager.GPS_PROVIDER))
			return true;
		else
			return false;
	}

	// preveri ali je Net vkljuèen
	public boolean jeNetOmogocen() {
		if (lokacijskiManagerNet
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			return true;
		else
			return false;
	}

	public double getHitrost() {
		return this.hitrost;
	}

	public double getPozicijaX() {
		return pozicijaX;
	}

	public double getPozicijaY() {
		return this.pozicijaY;
	}

	public double getNadmVisina() {
		return this.nadmVisina;
	}

	public double getNatancnost() {
		return this.natancnost;
	}

	public int getStatusSignala() {
		return this.statusSignalaGPS;
	}

	// metoda vrne razdaljo (v metrih) med lokacijam
	public double getRazdaljaMed(double x1, double y1, double x2, double y2) {
		float tab[] = new float[2];
		Location.distanceBetween(x1, y1, x2, y2, tab);

		return tab[0];
	}

	private String splitProvider(String s) {
		String[] tab = s.split("_");

		return tab[0];
	}

	private class LokPoslusalecGPS implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			posodobiLokacijo(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			statusSignalaGPS = status;

			if (status == 2)
				gMaps.pokaziObvestilo("Povezani s GPS");
			else
				gMaps.pokaziObvestilo("Ni signala GPS");
		}

	}
	private class LokPoslusalecNet implements LocationListener {
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			// tuki bomo posodobili lokacijo na mapi
			//Location lokacija = lokacijskiManagerNet
				//	.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(statusSignalaGPS == -1)
				posodobiLokacijo(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			if (status == 2) {
				if (statusSignalaGPS == -1)
					gMaps.pokaziObvestilo("Povezani s Network");
			}
		}

	}
	private void posodobiLokacijo(Location lokacija) {

		this.prefs = PreferenceManager.getDefaultSharedPreferences(gMaps);
		pozicijaX = lokacija.getLatitude();
		pozicijaY = lokacija.getLongitude();
		hitrost = lokacija.getSpeed() * 3.6;
		nadmVisina = lokacija.getAltitude();
		// ta podatek bomo posodabljali na glavnem oknu in sicer imeli bomo
		// indikator koliko GPS signala trenutno imaš
		natancnost = lokacija.getAccuracy();
		// uro moramo dobiti vsakiè na novo èe ne vsakiè da isto instanco
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		
		gMaps.posodobiMene(pozicijaX, pozicijaY,cal.getTime(), hitrost);
		gMaps.signal(splitProvider(lokacija.getProvider()));
			
		//if(prefs.getBoolean("key_moja_lokacija", true))

		if(gMaps.getMojID() > 0)
		{
			if(gMaps.getNarocenTaxi())
			{
				gMaps.posljiPosodobitev(pozicijaX,pozicijaY);
			}
			else
			{
				if (cal.getTimeInMillis() - casPosodobitve > 30000) 
				{
					if(prefs.getBoolean("key_moja_lokacija", true))
					{
						gMaps.posodobiMene(pozicijaX, pozicijaY,cal.getTime(), hitrost);
						gMaps.pokaziObvestilo("Posodobljenja lokacija: <"+ splitProvider(lokacija.getProvider()) + ">");
					}
					if(gMaps.getDosegljiv()==1)
					{
						//posodobitev baze
						gMaps.posodobiLokacijoDb(pozicijaX,pozicijaY, true);
						//gMaps.posodobiLatitudeLokacijo(pozicijaX, pozicijaY, cal.getTime());
					}
					gMaps.posodobiVse();
				}	
			}
		}
		else
		{
			if(!gMaps.getNarocenTaxi())
				if (cal.getTimeInMillis() - casPosodobitve > 30000)
					gMaps.posodobiVse();
		}
		casPosodobitve = cal.getTimeInMillis();
	}
}
