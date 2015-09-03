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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.cmu.sei.ams.cloudlet.ServiceVM;
import edu.cmu.sei.ams.cloudlet.android.CloudletCallback;
import edu.cmu.sei.ams.cloudlet.android.FindCloudletAndStartService;
import edu.cmu.sei.ams.cloudlet.rank.CpuBasedRanker;
import edu.cmu.sei.ams.cloudlet.android.ServiceConnectionInfo;
import edu.cmu.sei.ams.cloudlet.rank.CpuPerformanceRanker;
import edu.cmu.sei.ams.cloudlet.rank.MemoryPerformanceRanker;

public class SphinxAndroidClientActivity extends Activity implements OnClickListener
{
	public static final String LOG_KEY = "Speech";
	
	public static final int MENU_ID_SETTINGS = 92189;
	public static final int MENU_ID_CLEAR = 111163;
	
    private static final String SERVICE_ID = "edu.cmu.sei.ams.speech_rec_service";

	private ServiceConnectionInfo connectionInfo = new ServiceConnectionInfo();
	private String directoryString;

	private Socket socket;
	private File directory;
	private List<File> fileList;

	private DataOutputStream outToServer;
	private DataInputStream inFromServer;

	private TextView textView;
	private TextView currentDirTextView;
	private Button sendButton;
	
	private String log = "";
	
	private long requestSendTime = 0L;
	private long responseReceivedTime = 0L;
	private long rttForCurrentRequest = 0L;
	private long rttForPreviousRequest = 0L;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textView = (TextView)findViewById(R.id.text);
		textView.setText( log );
		currentDirTextView = (TextView)findViewById(R.id.current_dir_text);
		
		sendButton = (Button)findViewById(R.id.send_button);
		sendButton.setOnClickListener( this );

        loadPreferences();

		loadCurrentFileList();
	}

    protected void findService()
    {
        // Code to get cloudlet
        new FindCloudletAndStartService(this, this.SERVICE_ID, new CpuBasedRanker(), new CloudletCallback<ServiceVM>() {
            @Override
            public void handle(ServiceVM result) {
                if (result == null) {
                    Toast.makeText(SphinxAndroidClientActivity.this, "Failed to locate a cloudlet, or service not available", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.v("FACE", "GOT SERVICE RESULT: " + result.getInstanceId());

                Toast.makeText(SphinxAndroidClientActivity.this, "Located a cloudlet to use!", Toast.LENGTH_LONG).show();

                connectionInfo.setIpAddress(result.getAddress().getHostAddress());
                connectionInfo.setPortNumber(result.getPort());
                connectionInfo.storeIntoPreferences(SphinxAndroidClientActivity.this,
                        SphinxAndroidClientActivity.this.getString(R.string.pref_ipaddress),
                        SphinxAndroidClientActivity.this.getString(R.string.pref_portnumber));
            }
        }).execute();
    }

	private void loadCurrentFileList()
	{
		directory = new File( directoryString );
		if( !directory.exists() )
		{
			Toast.makeText( this, "Directory does not exist", Toast.LENGTH_SHORT).show();
		}
		else
		{
			File[] files = directory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.toString().endsWith(".wav")
							|| pathname.toString().endsWith(".WAV")) {
						return true;
					}
					return false;
				}
			});

            if(files != null) {
                fileList = Arrays.asList(files);
            }
            else
            {
                Toast.makeText( this, "No files found in selected folder.", Toast.LENGTH_SHORT).show();
            }
		}
	}

	@Override
	protected void onResume() 
	{
        loadPreferences();
        findService();

		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		menu.add(0, MENU_ID_SETTINGS, 0, getString( R.string.menu_settings) );
		menu.add(0, MENU_ID_CLEAR, 1, getString(R.string.menu_clear));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{
		switch( item.getItemId() )
		{
		case MENU_ID_SETTINGS:
			startActivity( new Intent( SphinxAndroidClientActivity.this, PreferenceActivity.class) );
			break;
		case MENU_ID_CLEAR:
			log = "";
			textView.setText( log );
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void loadPreferences()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
		this.connectionInfo.setIpAddress(prefs.getString(getString(R.string.pref_ipaddress), getString(R.string.default_ipaddress)));
		this.connectionInfo.setPortNumber(Integer.parseInt(prefs.getString(getString(R.string.pref_portnumber), getString(R.string.default_portnumber))));
		this.directoryString = prefs.getString( getString( R.string.pref_directory), getString(R.string.default_directory));
		currentDirTextView.setText( directoryString );
	}

	/**
	 * @author gmcahill
	 * A sync task for sending audio
	 */
	class SendAudio extends AsyncTask<Void,String,String>
	{
		ProgressDialog progreeDialog;

		@Override
		protected void onPreExecute() 
		{
			progreeDialog = new ProgressDialog( SphinxAndroidClientActivity.this );
			progreeDialog.setCancelable( false );
			
			// Update the current list of files, in case the folder has changed.
			loadPreferences();
			loadCurrentFileList();
			
			updateLog( "----------");
			progreeDialog.setMessage("Connecting to server...");
			updateLog( "Connecting to server..." );
			progreeDialog.show();
			super.onPreExecute();
			
		}

		@Override
		protected void onPostExecute(String result) 
		{
			progreeDialog.dismiss();
			if( result == null )
			{
				if(fileList.size() != 0)
				{
					updateLog( "No response or error from server...");
				}
			}
			super.onPostExecute(result);
		}


		@Override
		protected void onProgressUpdate(String... values) 
		{
			progreeDialog.setMessage( values[0] );
			updateLog( values[0] );
			super.onProgressUpdate(values);
		}

		@Override
		protected String doInBackground(Void... params) 
		{
			String response = null;
			try 
			{
				int fileCount = fileList.size();				
				publishProgress("Files to send: " + fileCount);
				if(fileCount == 0)
				{
					publishProgress( "No valid audio files were found in the current folder. No connection will be made.");	
					return null;
				}

				socket = new Socket();
				socket.connect(new InetSocketAddress(connectionInfo.getIpAddress(), connectionInfo.getPortNumber() ), 5000 );
				publishProgress("Connected to server " + socket.getInetAddress() +" on port " + socket.getPort() );

				outToServer = new DataOutputStream( socket.getOutputStream() );
				inFromServer = new DataInputStream( socket.getInputStream() );

                Log.d("FACE", "START SENDING FILES");
				int filesProccessed = 1;				
				for( final File file: fileList )
				{
                    Log.d("FACE", "SENDING FILE: " +file.getName() );
					publishProgress("Sending " +filesProccessed +" / " + fileCount +" file(s) \n" 
							+file.getName() +"\n" +"File size is " + file.length() +" bytes" );
					requestSendTime = System.currentTimeMillis();
					sendSpeechRequest( file );
					publishProgress("Finished sending " +file.getName() );
					
					publishProgress("Getting response from server..." );
					int responseSize = inFromServer.readInt();
					publishProgress("Response size is " + responseSize +" bytes" );
                    Log.d("FACE", "Received response for : " +file.getName() );
					
					if(responseSize > 0 )
					{
						byte[] byteBuffer = new byte[responseSize];
						inFromServer.read(byteBuffer);
						responseReceivedTime = System.currentTimeMillis();
						rttForCurrentRequest = responseReceivedTime - requestSendTime;
						response = new String(byteBuffer);
						publishProgress( "----------");
						publishProgress( response );
						publishProgress("Request Send Time: " + requestSendTime );
						publishProgress("Response Recieved Time: " + responseReceivedTime );
                        Log.d("FACE", "Response time: " + rttForCurrentRequest);
						publishProgress("RTT Current Request: " + rttForCurrentRequest );
						publishProgress("RTT Previous Request: " + rttForPreviousRequest );
						publishProgress( "----------");
						rttForPreviousRequest = rttForCurrentRequest;
					}
					filesProccessed++;
				}

			} 
			catch (UnknownHostException e) 
			{
				publishProgress("An UnknownHostException has occured." + e.toString());
				e.printStackTrace();
				return null;
			} 
			catch (IOException e) 
			{
				publishProgress("An IOException has occured: " + e.toString());
				e.printStackTrace();
				return null;
			}
			return response;
		}
	}
	
	public void sendSpeechRequest( File file ) 
	{
		try 
		{
			int fileLength = (int)(file.length());
			outToServer.writeLong( fileLength );
			FileInputStream fis = new FileInputStream( file );
			byte[] buffer = new byte[ fileLength ];
			fis.read( buffer );
			
			outToServer.write( buffer );
		} 
		catch (IOException io) 
		{
			io.printStackTrace();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{
		outState.putString( LOG_KEY, log );
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		log = savedInstanceState.getString( LOG_KEY );
		textView.setText( log );
		super.onRestoreInstanceState(savedInstanceState);
	}

	
	@Override
	public void onClick(View v) 
	{
		if( v.equals( sendButton ) )
		{
			new SendAudio().execute();
		}
	}
	
	public void updateLog( String text )
	{
		log = log +"\n" + text;
		textView.setText( log );
	}
}