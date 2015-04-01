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
