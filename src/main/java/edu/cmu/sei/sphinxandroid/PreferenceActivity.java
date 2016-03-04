/*
KVM-based Discoverable Cloudlet (KD-Cloudlet) 
Copyright (c) 2015 Carnegie Mellon University.
All Rights Reserved.

THIS SOFTWARE IS PROVIDED "AS IS," WITH NO WARRANTIES WHATSOEVER. CARNEGIE MELLON UNIVERSITY EXPRESSLY DISCLAIMS TO THE FULLEST EXTENT PERMITTEDBY LAW ALL EXPRESS, IMPLIED, AND STATUTORY WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF PROPRIETARY RIGHTS.

Released under a modified BSD license, please see license.txt for full terms.
DM-0002138

KD-Cloudlet includes and/or makes use of the following Third-Party Software subject to their own licenses:
MiniMongo
Copyright (c) 2010-2014, Steve Lacy 
All rights reserved. Released under BSD license.
https://github.com/MiniMongo/minimongo/blob/master/LICENSE

Bootstrap
Copyright (c) 2011-2015 Twitter, Inc.
Released under the MIT License
https://github.com/twbs/bootstrap/blob/master/LICENSE

jQuery JavaScript Library v1.11.0
http://jquery.com/
Includes Sizzle.js
http://sizzlejs.com/
Copyright 2005, 2014 jQuery Foundation, Inc. and other contributors
Released under the MIT license
http://jquery.org/license
*/

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
		.findPreference( getString(R.string.pref_domain) );

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
		.getString( getString(R.string.pref_domain), getString(R.string.default_domain));
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
