package com.taxist.googleMaps;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ListaTaxistov extends ListActivity {
	
	/**
	 * Aktivnost ListaTaksistov nam prikazuje listo vseh taksitov, ki se nahajajo v PB.
	 * Lista takistov vsebuje vse podatke o taksistih. Na listi si lahko izberemo
	 * želenega taksista in ga s tem naroèimo.
	 * Lista taksistov se razlikuje glede na uporabnika (taksit ali stranka).
	 * Stranki so prikazani zgolj trenutno dosegljivi taksiti. 
	 * Urejeni so po oddaljenosti od trenunte lokacije stranke.
	 */
	private ProgressDialog m_ProgressDialog = null; 
    private ArrayList<Taxi> m_orders = null;
    private TaxiAdapter m_adapter;
    private Runnable viewOrders;
    private SharedPreferences prefs;
    private int vloga = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.seznam_taxistov);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			//	WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        m_orders = new ArrayList<Taxi>();
        this.m_adapter = new TaxiAdapter(this, R.layout.taxi_row, m_orders);
        setListAdapter(this.m_adapter);

        viewOrders = new Runnable(){
            @Override
            public void run() {
                getTaxi();
            }
        };
        Thread thread =  new Thread(null, viewOrders, "MagentoBackground");
        thread.start();

        m_ProgressDialog = ProgressDialog.show(ListaTaxistov.this,    
              "Prosimo poèakajte...", "Pridobivanje podatkov...", true);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//String item = (String) getListAdapter().getItem(position);
		final Taxi taxi = (Taxi) getListAdapter().getItem(position);
		
		// custom dialog
		final Dialog dialog = new Dialog(this);
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
		
		if(prefs.getInt("vloga", 0) == 2)
		{	
			btnOk.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					Intent intent = new Intent(ListaTaxistov.this,GoogleMaps.class);
					Bundle bundle = new Bundle();
					bundle.putString("emailTaxi", taxi.getEmail());
					intent.putExtras(bundle);
					startActivity(intent);
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
	private Runnable returnRes = new Runnable() 
	{
        @Override
        public void run() {
            if(m_orders != null && m_orders.size() > 0){
                m_adapter.notifyDataSetChanged();
                for(int i=0; i<m_orders.size(); i++)
                	m_adapter.add(m_orders.get(i));
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
    };
    private void getTaxi()
    {
    	try{
    		Bundle extras = getIntent().getExtras();
    		if (extras != null) {
    			ArrayList<Taxi> listTaxi = extras.getParcelableArrayList("listTaxi");
    		    
    			m_orders = listTaxi;
    		}
    		Thread.sleep(2000);
            runOnUiThread(returnRes);
        }
    	catch (Exception e) { 
            System.out.println("Napaka pri branju taxistov pri listi!");
            m_ProgressDialog.dismiss();
        }
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
    private String timesToString(Long timestampMs) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		Date d = new Date(Long.valueOf(timestampMs.toString()));

		return df.format(d);
	}
    private class TaxiAdapter extends ArrayAdapter<Taxi> {

        private ArrayList<Taxi> items;

        public TaxiAdapter(Context context, int textViewResourceId, ArrayList<Taxi> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.taxi_row, null);
                }
                Taxi taxi = items.get(position);
                if (taxi != null) {
                        TextView tt = (TextView) v.findViewById(R.id.id_ime_taxi);
                        TextView bt = (TextView) v.findViewById(R.id.id_mail_taxi);
                        ImageView icon = (ImageView) v.findViewById(R.id.id_icon_taxi);
                        
                        double razd = taxi.getRazdalja();
                        BigDecimal bd = new BigDecimal(razd/1000).setScale(1, RoundingMode.HALF_EVEN);
                        if (tt != null) {
                              tt.setText(taxi.getIme()+" "+taxi.getPriimek());                            }
                        if(bt != null){
                              bt.setText("Oddaljenost: "+bd.doubleValue()+"km Èas: "+taxi.getCas());
                        }
                        if(taxi.getDosegljiv() == 0)
                        	icon.setImageResource(R.drawable.taxi_on);
                        else if(taxi.getDosegljiv() == 1)
                        	icon.setImageResource(R.drawable.taxi_off);
                        	
                }
                return v;
        }
	}
}
