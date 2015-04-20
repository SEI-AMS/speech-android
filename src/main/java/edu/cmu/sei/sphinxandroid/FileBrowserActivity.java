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

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FileBrowserActivity extends ListActivity implements OnItemClickListener, OnClickListener
{
	private File currentDirectory;
	private ListView listView;
	
	private List<File> fileList;
	private FileAdapter adapter;
	
	private TextView currentDirTextView;
	
	private Button useThisDirButton;
	private Button cancelButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filebrowser);
		
		String dir = getIntent().getStringExtra( getString( R.string.intent_key_directory ));
		
		listView = getListView();
		currentDirTextView = (TextView)findViewById(R.id.current_dir_text);
		useThisDirButton = (Button)findViewById(R.id.use_this_dir_button);
		useThisDirButton.setOnClickListener( this );
		cancelButton = (Button)findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener( this );
		
		openDirectory( new File( dir ) );
		
		adapter = new FileAdapter( this, R.layout.file_row, fileList );
		listView.setAdapter( adapter );
		listView.setOnItemClickListener( this );
		
	}
	
	class FileAdapter extends ArrayAdapter<File>
	{
		private int resource;
		
		public FileAdapter( Context context, int textViewResourceId, List<File> list ) 
		{
			super( context, textViewResourceId, list );
			resource = textViewResourceId;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;
			if( v == null )
			{
				LayoutInflater inflator = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
				v = inflator.inflate( resource, null );
			}
			
			File file = fileList.get( position );
			
			ImageView img = (ImageView)v.findViewById(R.id.image1);
			if( file.isDirectory() )
			{
				img.setVisibility(View.VISIBLE);
			}
			else
			{
				img.setVisibility(View.INVISIBLE);
			}
			TextView text = (TextView)v.findViewById(R.id.text1);
			text.setText( file.getName() );
			
			return v;
		}
	}
	
	public void updateAdapter()
	{
		//adapter.notifyDataSetChanged();
		adapter = new FileAdapter(this, R.layout.file_row, fileList);
		listView.setAdapter(adapter);
	}
	
	public void openDirectory( File file )
	{
		currentDirectory = file;
		currentDirTextView.setText( currentDirectory.toString() );
		fileList = getDirectoryList( currentDirectory );
	}
	
	public List<File> getDirectoryList( File file )
	{
/*		File[] files = file.listFiles( new FileFilter() {
			@Override
			public boolean accept(File pathname) 
			{
				return pathname.isDirectory();
			}
		});
		return Arrays.asList( files );*/
		
		File[] files = file.listFiles();
		return Arrays.asList( files );
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
	{
/*		File file = fileList.get( arg2 );
		openDirectory( file );
		updateAdapter();*/
		
		File file = fileList.get( arg2 );
		if( file.isDirectory() )
		{
			openDirectory( file );
			updateAdapter();
		}
	}
	
	@Override
	public void onBackPressed() 
	{
		File parent = currentDirectory.getParentFile();
		
		if( parent != null )
		{
			openDirectory( parent );
			updateAdapter();
		}
		else
		{
			super.onBackPressed();
		}
	}

	@Override
	public void onClick(View v) 
	{
		if( v.equals( useThisDirButton ) )
		{
			Intent data = new Intent();
			data.putExtra( getString( R.string.intent_key_directory ), currentDirectory.toString() );
			setResult( RESULT_OK, data );
			finish();
		}
		if( v.equals( cancelButton ) )
		{
			setResult( RESULT_CANCELED );
			finish();
		}
	}
}
