package com.taxist.googleMaps;

import java.io.IOException;

import java.io.IOException;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class Komunikacija 
{
	/**
	 * Razred Komunikacija skrbi za komuniciranje uporabnikov z razliènimi vlogami.
	 * Komunikacija poteka med taksistom in stranko.
	 * Za povezovanje do strežnika GTalk uporabljamo protokol XMPP.
	 * Ob uspešni povezavi nam razred nudi metode za pošiljanje in prejemanje sporoèila.
	 * Vsebina sporoèil za pošiljanje z strani taksista ali uporabnika jeprogramkso doloèeno
	 * v aktivnosti GoogleMaps.
	 */
	private XMPPConnection connection;
	private GoogleMaps gMaps = null;
	
	public Komunikacija(GoogleMaps gMaps)
	{
		this.gMaps = gMaps;
	}
	public Komunikacija()
	{
	}
	public void poslji(String naslovnik, String sporocilo)
	{
		//nastavitev informacije o statusu prisotnosti
		/*
		Presence presence = new Presence(Presence.Type.available);
		connection.sendPacket(presence);*/
		
		//pošiljanje sporoèila željenemu
		Message msg = new Message(naslovnik, Message.Type.normal);
		msg.setBody(sporocilo);
		if(connection != null)
			connection.sendPacket(msg);
	}
	public void prejmi()
	{		
		//prejemanje sporoèila
		PacketFilter filter = new MessageTypeFilter(Message.Type.normal);
		PacketListener pl = new PacketListener() {
			@Override
			public void processPacket(Packet p) 
			{
				Message message = (Message) p;
				String body = message.getBody();
				String from = message.getFrom();
				System.out.println("SPOROÈILO :>"+body);
				if(body != null)
				{
					String temp = body;
					if(gMaps.getMojID() > 0)
			    	{
			    		gMaps.potrditevTaxista(from, body);
			    	}
			    	else
			    	{
			    		gMaps.potrditevStranke(from, body);
			    	}
				 }
			  }
			};
		connection.addPacketListener(pl, filter);	
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Napaka pri branju");
		}
	}
	public boolean povezi(String upIme,String geslo)
	{
		System.out.println("povezava s stežnikom");
		//povzava s gtalk stežnikom
		ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
		connection = new XMPPConnection(connConfig);
		
		try 
		{
			connection.connect();
		} 
		catch (XMPPException e1) 
		{
			connection = null;
			System.out.println("Napaka pri povezavi s strežnikom");
			return false;
		}
		//prijava s uporabniškim imenom in geslom
		try 
		{
			connection.login(upIme, geslo);
			return true;
		}
		catch (Exception e1) 
		{
			System.out.println("Napaka pri prijavi: ");
			return false;
		}
	}
	public void prekini()
	{
		System.out.println("Prekinitev povezave s strežnikom GTalk!");
		connection.disconnect();
	}
}
