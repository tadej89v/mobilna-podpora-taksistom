package com.taxist.googleMaps;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
	/**
	 * Aktivnost SeettingActivity nam ponuja nastavitve aplikacije.
	 * Omogoèa nastavitve zemlejvida, nastavitve zgodovine lokacij,
	 * nastavitve samodejne posodabljanje lokacije, vkljuèlitev GPS, itd.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref_latitude);

		preberiVrednost(findPreference("key_pogled_mape"));
		Preference gumb = getPreferenceManager().findPreference("key_privzeto");
		if (gumb != null) {

			gumb.setOnPreferenceClickListener(prefToDefaultListener);
		}
		// privzeteNastavitve(findPreference("key_privzeto"));
		// preberiListPreference(findPreference("key_cas_posodobitve"));
		// preberiListPreference(findPreference("key_zgodovina"));
	}

	private static Preference.OnPreferenceChangeListener prefToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {

				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			} else {
				preference.setSummary(stringValue);
			}
			return true;
		}
	};
	private static Preference.OnPreferenceClickListener prefToDefaultListener = new Preference.OnPreferenceClickListener() {

		@Override
		public boolean onPreferenceClick(Preference preference) {

			System.out.println("jejj");

			return true;
		}
	};

	private static void preberiVrednost(Preference preference) {

		preference.setOnPreferenceChangeListener(prefToValueListener);

		prefToValueListener.onPreferenceChange(preference, PreferenceManager
				.getDefaultSharedPreferences(preference.getContext())
				.getString(preference.getKey(), ""));
	}
}
