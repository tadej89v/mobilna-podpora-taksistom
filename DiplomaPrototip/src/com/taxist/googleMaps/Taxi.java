package com.taxist.googleMaps;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Taxi implements Parcelable
{
	/**
	 * Razred Taxi vsebuje vse spremenljivke in metode, ki nam sluûijo za shrambo vseh podatkov
	 * taksita. Tega nato kot razred Taxi vstavimo v zbirko vseh taksistov (HahsMap), s katerimi
	 * upravljamo skozi celotno aplikacijo.
	 */
	private String id;
	private String ime;
	private String priimek;
	private String email;
	private String naslov;
	private long timeStamp;
	private LatLng lokacija;
	private String slikaUrl;
	private String tablicaUrl;
	private int dosegljiv;
	private int razdalja;
	private String cas;
	
	public Taxi(String id, String naslov, long timeStamp, LatLng lokacija, String slikaUrl, String tablicaUrl, int dosegljiv, int razdalja, String cas)
	{
		this.id = id;
		this.naslov = naslov;
		this.timeStamp = timeStamp;
		this.lokacija = lokacija;
		this.slikaUrl = slikaUrl;
		this.tablicaUrl = tablicaUrl;
		this.dosegljiv = dosegljiv;
		this.razdalja = razdalja;
		this.cas = cas;
	}
	public Taxi(String id, int dosegljiv)
	{
		this.dosegljiv = dosegljiv;
		this.dosegljiv = dosegljiv;
	}
	public Taxi(String id, String ime, String priimek, String email, LatLng lokacija, long timeStamp, String slikaUrl, int dosegljiv, String naslov, int razdalja, String cas)
	{
		this.id = id;
		this.ime = ime;
		this.priimek = priimek;
		this.email = email;
		this.timeStamp = timeStamp;
		this.lokacija = lokacija;
		this.slikaUrl = slikaUrl;
		this.dosegljiv = dosegljiv;
		this.naslov = naslov;
		this.razdalja = razdalja;
		this.cas = cas;
	}
	public String getId()
	{
		return id;
	}
	public String getIme()
	{
		return ime;
	}
	public String getPriimek()
	{
		return priimek;
	}
	public String getEmail()
	{
		return email;
	}
	public String getNaslov()
	{
		return naslov;
	}
	public String getSlikaUrl()
	{
		return slikaUrl;
	}
	public String getTablicaUrl()
	{
		return tablicaUrl;
	}
	public int getDosegljiv()
	{
		return dosegljiv;
	}
	public long getTimestamp()
	{
		return timeStamp;
	}
	public LatLng getLokacija()
	{
		return lokacija;
	}
	public int getRazdalja()
	{
		return razdalja;
	}
	public String getCas()
	{
		return cas;
	}
	public void setIme(String ime)
	{
		this.ime = ime;
	}
	public void setDodatno(String ime,String email)
	{
		this.ime = ime;
		this.email = email;
	}
	@Override
	public String toString()
	{
		return "\nIme:\t"+ime+
				"\nNaslov:\t"+naslov+
				"\nEmail:\t"+email+
				"\n»as:\t"+timeStamp+
				"\nLatitude:\t"+lokacija.latitude+
				"\nLongitude:\t"+lokacija.longitude+
				"\nZasedenost:\t"+dosegljiv+
				"\nRazdalja: "+razdalja+
				"\nCas: "+cas;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(ime);
		dest.writeString(priimek);
		dest.writeString(email);
		dest.writeString(slikaUrl);
		dest.writeLong(timeStamp);
		dest.writeInt(razdalja);
		dest.writeInt(dosegljiv);
		dest.writeString(cas);
		dest.writeString(naslov);
	}
	public static final Parcelable.Creator<Taxi> CREATOR = new Parcelable.Creator<Taxi>() 
	{
		@Override
		public Taxi createFromParcel(Parcel in) 
		{
			return new Taxi(in);
		}

		@Override
		public Taxi[] newArray(int size) 
		{
			return new Taxi[size];
		}
	};

	private Taxi(Parcel in) {
		ime = in.readString();
		priimek = in.readString();
		email = in.readString();
		slikaUrl = in.readString();
		timeStamp = in.readLong();
		razdalja = in.readInt();
		dosegljiv = in.readInt();
		cas = in.readString();
		naslov = in.readString();
	}
}
