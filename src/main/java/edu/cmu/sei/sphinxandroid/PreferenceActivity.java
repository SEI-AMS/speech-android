/**
---------------------
Copyright 2012 Carnegie Mellon University

This material is based upon work funded and supported by the Department of Defense under Contract No. 
FA8721-05-C-0003 with Carnegie Mellon University for the operation of the Software Engineering Institute, 
a federally funded research and development center.

Any opinions, findings and conclusions or recommendations expressed in this material are those of the 
author(s) and do not necessarily reflect the views of the United States Department of Defense.

NO WARRANTY
THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS"
BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY 
MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, 
OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF 
ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.

This material contains SEI Proprietary Information and may not be disclosed outside of the SEI without 
the written consent of the Director's Office and completion of the Disclosure of Information process.
------------
**/

package edu.cmu.sei.sphinxandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.text.InputType;

public class PreferenceActivity extends android.preference.PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener
{

	public static final int FILE_BROWSER_REQUEST_CODE = 21212;

	private EditTextPreference ipaddressPref;
	private EditTextPreference portnumberPref;
	private Preference directoryPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		ipaddressPref = (EditTextPreference)getPreferenceScreen()
		.findPreference( getString(R.string.pref_ipaddress) );

		portnumberPref = (EditTextPreference)getPreferenceScreen()
		.findPreference( getString( R.string.pref_portnumber));
		portnumberPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

		directoryPref = (Preference)getPreferenceScreen()
		.findPreference(getString(R.string.pref_directory));

		directoryPref.setOnPreferenceClickListener( this );

		updatePreferneces();

		getPreferenceScreen()
		.getSharedPreferences()
		.registerOnSharedPreferenceChangeListener( this );

	}

	public void updatePreferneces()
	{
		String ipAddress = getPreferenceScreen()
		.getSharedPreferences()
		.getString( getString(R.string.pref_ipaddress), getString(R.string.default_ipaddress));
		ipaddressPref.setSummary(ipAddress);

		String portNumber = getPreferenceScreen()
		.getSharedPreferences()
		.getString(getString(R.string.pref_portnumber), getString(R.string.default_portnumber));
		portnumberPref.setSummary(portNumber);

		String directory = getPreferenceScreen()
		.getSharedPreferences()
		.getString(getString(R.string.pref_directory), getString(R.string.default_directory));
		directoryPref.setSummary(directory);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
	{
		updatePreferneces();	
	}


	@Override
	public boolean onPreferenceClick(Preference preference) 
	{
		if( preference.equals( directoryPref ) )
		{
			Intent intent = new Intent( PreferenceActivity.this, FileBrowserActivity.class );
			intent.putExtra( getString( R.string.intent_key_directory), directoryPref.getSummary().toString() );
			startActivityForResult( intent, FILE_BROWSER_REQUEST_CODE );
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch( requestCode )
		{
		case FILE_BROWSER_REQUEST_CODE:
			switch( resultCode )
			{
			case RESULT_OK:
				String dir = data.getStringExtra( getString(R.string.intent_key_directory ) );
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences( PreferenceActivity.this ).edit();
				editor.putString( getString( R.string.pref_directory), dir );
				editor.apply();
				updatePreferneces();
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
