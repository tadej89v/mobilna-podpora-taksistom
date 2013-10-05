package com.taxist.googleMaps;

public class GoogleLatitude {
	/**
	 * Razred GoogleLatitude je namenjen klicu Latitude API, s katerim smo
	 * uporabljali funkcije in operacije API-ja.
	 * Glavne operacije so pridobitev lokacije želenega uporabnika in posodobitev
	 * lokacije uporabnika v storitev Google Latitude.
	 * Parametre za uspešen dostop do API-ja nam je omogoèil protokol OAuth 2.0.
	 */
	/*
	private SharedPreferences prefs;
	private Latitude latitude;
	private GoogleMaps gMaps = null;

	public GoogleLatitude(SharedPreferences prefs, GoogleMaps gMaps) {
		this.prefs = prefs;
		this.gMaps = gMaps;
	}

	public void clearCredentials() {
		new CredentialsSharedPreferences(prefs).clearCredentials();
	}
	public boolean ApiCall() {
		try {
			JsonFactory jsonFactory = new JacksonFactory();
			HttpTransport transport = new NetHttpTransport();

			CredentialsSharedPreferences credentialStore = new CredentialsSharedPreferences(
					prefs);

			// branje shranjenih nastavitev za API klic
			AccessTokenResponse accessTokenResponse = credentialStore.read();

			GoogleAccessProtectedResource access = new GoogleAccessProtectedResource(
					accessTokenResponse.accessToken, transport, jsonFactory,
					CredentialsClient.CLIENT_ID,
					CredentialsClient.CLIENT_SECRET,
					accessTokenResponse.refreshToken);

			latitude = new Latitude(transport, access, jsonFactory);
			latitude.apiKey = CredentialsClient.API_KEY;

			gMaps.ustvariObjektLatitude(latitude);
			

			System.out.println("!Omogoèene Google storitve!");
			return true;
		} catch (Exception ex) {
			System.out.println("NAPAKA API: " + ex.getMessage());
			return false;
		}
	}
	static LatitudeCurrentlocationResourceJson prikaziTrenutnoLokacijo(Latitude latitude) throws IOException 
	{
		LatitudeCurrentlocationResourceJson latitudeCurrentlocation = latitude.currentLocation
				.get().execute();

		return latitudeCurrentlocation;
	}

	public static List<Location> prikaziZgodovinoLokacijSt(Latitude latitude,
			String stLokacij) throws IOException {

		com.google.api.services.latitude.Latitude.Location.List list = latitude.location
				.list();
		list.maxResults = stLokacij;
		LocationFeed locationFeed = list.execute();
		List<Location> locations = locationFeed.items;

		return locations;
	}
	public static List<Location> prikaziZgodovinoLokacijCas(Latitude latitude,
			String cas) throws IOException {

		com.google.api.services.latitude.Latitude.Location.List list = latitude.location
				.list();
		list.maxTime = cas;
		LocationFeed locationFeed = list.execute();
		List<Location> locations = locationFeed.items;

		return locations;
	}
	static boolean posodobiTrenutnoLokacijo(Latitude latitude, double x,
			double y, Date d) {
		try {
			LatitudeCurrentlocationResourceJson latitudeCurrentlocationResourceJson = new LatitudeCurrentlocationResourceJson();
			latitudeCurrentlocationResourceJson.put("latitude", x);
			latitudeCurrentlocationResourceJson.put("longitude", y);
			latitudeCurrentlocationResourceJson.put("timestampMS", d);
			latitude.currentLocation.insert(latitudeCurrentlocationResourceJson).execute();

			return true;
		} catch (Exception e) {
			System.out.println("NAPAKA pri posodobitvi lokacije Latitude!");
			return false;
		}
	}*/
}
