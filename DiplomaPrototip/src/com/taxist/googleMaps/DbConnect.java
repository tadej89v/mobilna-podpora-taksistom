package com.taxist.googleMaps;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.taxist.oauth2.CredentialsClient;

public class DbConnect {
	
	/**
	 * Razred DbConnect skrbi da preko protokola HTTP preberemo PHP skripte.
	 * Te teèejo na strežniku in skrbijo za upravljanje nad podatkovno bazo.
	 */
	private Connection con;
	private Statement st;
	private ResultSet rs;
	private GoogleMaps gMaps = null;
	private StringBuilder sb = null;
	private InputStream is = null;
	
	public DbConnect(GoogleMaps gMaps)
	{
		this.gMaps = gMaps;
	}
	public DbConnect()
	{
	}
	public HashMap<String,Taxi> getTaxiMap()
	{
		HashMap<String, Taxi> taxisti = new HashMap<String, Taxi>();
		Taxi taxi;
		JSONArray jArray = null;
		String rezultat = "";
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		try{
			//htttp post
			HttpClient httpclient = new DefaultHttpClient();
	        HttpPost httppost = new HttpPost(CredentialsClient.hostDb+"apiGet.php");
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        is = entity.getContent();
			//convert response to string
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
	        sb = new StringBuilder();
	        String line = null;
	        while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	        }
	        is.close();
	        rezultat = sb.toString();
	        //parse json data
	        jArray = new JSONArray(rezultat);
	        for(int i=0;i<jArray.length();i++)
	        {
	        	JSONObject json_data = jArray.getJSONObject(i);
	        	
	        	int id = json_data.getInt("id");
	        	String ime = json_data.getString("ime");
	        	String priimek = json_data.getString("priimek");
	        	String email = json_data.getString("email");
	        	double latitude = json_data.getDouble("latitude");
	        	double longitude = json_data.getDouble("longitude");
	        	String timestamp = json_data.getString("timestamp");
	        	String slikaUrl = json_data.getString("slikaUrl");
	        	int zasedenost = json_data.getInt("dosegljiv");
	        	String naslov = "";

	        	String[] tab = gMaps.razdaljaTaxi(new LatLng(latitude,longitude));
				int razd = 0;
				if(!tab[0].equals("prazno"))
					razd=Integer.parseInt(tab[0]);
				if(!tab[4].equals("prazno"))
					naslov = tab[4];
				taxi = new Taxi(Integer.toString(id), ime, priimek, email, new LatLng(latitude,longitude), toTimestamp(timestamp).getTime(), slikaUrl, zasedenost, naslov, razd, tab[2]);
				taxisti.put(email, taxi);
	        }
	        
		}catch(Exception e){
			System.out.println("Error getTaxiMap: "+e);
		}
		return taxisti;
	}
	private Timestamp toTimestamp(String time)
	{
		Timestamp stamp = null; 
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			Date date = simple.parse(time);
			stamp = new Timestamp(date.getTime());
			
		}catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return stamp;
	}
	public boolean updateLocation(int userID, double lat, double lon, boolean dos)
	{
		boolean isUpdate = false;
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("latitude", Double.toString(lat)));
		nameValuePairs.add(new BasicNameValuePair("longitude", Double.toString(lon)));
		nameValuePairs.add(new BasicNameValuePair("id", Integer.toString(userID)));
		String dosegljiv = "1";
		if(dos)
			dosegljiv = "0";

		nameValuePairs.add(new BasicNameValuePair("dosegljiv",dosegljiv));
		
		//http post
		try{
		        HttpClient httpclient = new DefaultHttpClient();
		        HttpPost httppost = new HttpPost(CredentialsClient.hostDb+"apiUpdate.php");
		        
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		        
		        //httppost.setParams(params)
		        HttpResponse response = httpclient.execute(httppost);
		        HttpEntity entity = response.getEntity();
		        is = entity.getContent();
				//convert response to string
		        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
		        String line = reader.readLine();
		        if(line.compareTo("1") == 0)
		        	isUpdate = true;
		        is.close();
		}catch(Exception e){
		       System.out.println("Error update location: "+e);
		}
		return isUpdate;
	}
	public int checkDb(String email)
	{
		int checkID = 0;
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("email", email));
		
		//http post
		try{
		        HttpClient httpclient = new DefaultHttpClient();
		        HttpPost httppost = new HttpPost(CredentialsClient.hostDb+"apiCheck.php");
		        
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		        
		        //httppost.setParams(params)
		        HttpResponse response = httpclient.execute(httppost);
		        HttpEntity entity = response.getEntity();
		        is = entity.getContent();
				//convert response to string
		        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
		        String line = reader.readLine();
		        if(line.compareTo("0") != 0)
		        	checkID= Integer.parseInt(line);
		        is.close();
		}catch(Exception e){
		       System.out.println("Error update location: "+e);
		       checkID = 0;
		}
		
		return checkID;
	}
	public ArrayList<LatLng> getZgodovina(int userID, String stLokacij, String timestamp)
	{
		ArrayList<LatLng> zgodovina = new ArrayList<LatLng>();
		JSONArray jArray = null;
		String rezultat = "";
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("id_user", Integer.toString(userID)));
		nameValuePairs.add(new BasicNameValuePair("st_lokacij", stLokacij));
		nameValuePairs.add(new BasicNameValuePair("cas", timestamp));
		
		try{
			//htttp post
			HttpClient httpclient = new DefaultHttpClient();
	        HttpPost httppost = new HttpPost(CredentialsClient.hostDb+"apiZgodovina.php");
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        is = entity.getContent();
			//convert response to string
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
	        sb = new StringBuilder();
	        String line = null;
	        while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	        }
	        is.close();
	        rezultat = sb.toString();
	        //parse json data
	        jArray = new JSONArray(rezultat);
	        for(int i=0;i<jArray.length();i++)
	        {
	        	JSONObject json_data = jArray.getJSONObject(i);
	        	 LatLng lokacija = new LatLng(json_data.getDouble("latitude"), json_data.getDouble("longitude"));
	        	zgodovina.add(lokacija);
	        }
	        
		}catch(Exception e){
			System.out.println("Error getZgodovina: "+e);
			zgodovina = null;
		}
		
		return zgodovina;
	}
	private String timestampToString(long timestamp)
	{
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date d = new Date(timestamp);
		
		cal.setTime(d);
		
		return df.format(d);
	}
}
