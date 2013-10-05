package com.taxist.googleMaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Pot {
	/**
	 * Razred Pot nam služi za izrisovanje poti taksistu ali stranki na zemlejvidu.
	 * željene podatke o željeni destinaciji pridobimo preko funkcionalnosti 
	 * zemljevida Google Maps.
	 * Preko URL v JSON formatu preberemo podatke o zaèetni in konèni toèki.
	 * S pomoèjo algotirma dekodirajPoligon() dekodiramo vse toèke, katere izrišemo
	 * na zemljevidu.
	 */
	private LatLng start;
	private LatLng end;
	private List<Location> tocke;
	private String konec;
	private String zacetek;
	private String km;
	private String min;
	private LatLng cilj;
	
	public Pot(LatLng start, LatLng end)
	{
		this.start = start;
		this.end = end;
	}
	public Pot(LatLng cilj)
	{
		this.cilj = cilj;
	}
	public ArrayList<LatLng> najdiPot()
	{
		ArrayList<LatLng> potDoCilja = new ArrayList<LatLng>();
		try {
			String url;
			url = "http://maps.googleapis.com/maps/api/directions/json?origin="
						+start.latitude+","+start.longitude+"&destination="
						+end.latitude+","+end.longitude+"&sensor=false&mode=driving";

			JSONObject json = readJsonFromUrl(url);
			
			if(json.getString("status").equals("OK"))
			{
				JSONArray pot = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
				JSONObject razdalja = pot.getJSONObject(0).getJSONObject("distance");
				JSONObject trajanje = pot.getJSONObject(0).getJSONObject("duration");
		
				zacetek = pot.getJSONObject(0).getString("start_address");
				konec = pot.getJSONObject(0).getString("end_address");
				km = razdalja.getString("text");
				min = trajanje.getString("text");
				JSONArray koraki = pot.getJSONObject(0).getJSONArray("steps");
				
				//LatLng lokCilj = new LatLng(lokacijaCilja.getDouble("lat"), lokacijaCilja.getDouble("lng"));

				for(int j=0;j<koraki.length();j++)
				{
					JSONObject start = koraki.getJSONObject(j).getJSONObject("start_location");
					JSONObject end = koraki.getJSONObject(j).getJSONObject("end_location");
			
					potDoCilja.add(new LatLng(start.getDouble("lat"), start.getDouble("lng")));
					JSONObject poligon = koraki.getJSONObject(j).getJSONObject("polyline");
					ArrayList<LatLng> tocke = dekodirajPoligon(poligon.getString("points"));
					for(int i=0;i<tocke.size();i++)
					{
						potDoCilja.add(tocke.get(i));
					}
					potDoCilja.add(new LatLng(end.getDouble("lat"), end.getDouble("lng")));
				}
				return potDoCilja;
			}
			else
				return null;
		}
		catch(Exception e)
		{
			System.out.println("Napaka pri iskanju poti!");
			return null;
		}
		
	}
	private ArrayList<LatLng> dekodirajPoligon(String encoded) 
	{
		ArrayList<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;
		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng position = new LatLng(lat / 1E5, lng / 1E5);
			poly.add(position);
		}
		return poly;
	}
	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException 
	{
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
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
	public String getZacetek()
	{
		return zacetek;
	}
	public String getKonec()
	{
		return konec;
	}
	public String getKm()
	{
		return km;
	}
	public String getMin()
	{
		return min;
	}
	public LatLng getCilj()
	{
		return cilj;
	}
}
