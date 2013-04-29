package com.bvcode.ncopter;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_heartbeat;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.MAVLink.Messages.common.msg_statustext;
import com.bvcode.ncopter.comms.BluetoothModule;
import com.bvcode.ncopter.comms.CommunicationClient;
import com.bvcode.ncopter.gps.GPSActivity;
import com.bvcode.ncopter.mission.MissionActivity;
import com.bvcode.ncopter.setup.SetupActivity;
import com.bvcode.ncopter.widgets.ttsOptionsWidget;

public class MainActivity extends Activity implements OnClickListener {
	
	private Button setupButton, GPSButton, readouts, status, params, hud, mission, mode, cli;
	private Boolean first = true;
	private TextView connectLabel;
	private TextView extraInfo1;
	private TextView extraInfo2;
	private TextView extraInfo3;
	private TextView extraInfo4;
	private TextView extraInfo5;
	
	public MainActivity(){
		
	}
	public void startup(){
		if( CommonSettings.isProtocolMAVLink() && CommonSettings.kmlLog ){
			msg_request_data_stream req1 = new msg_request_data_stream();
			req1.req_message_rate = 1;
			req1.req_stream_id = MAVLink.MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS;
			req1.start_stop = 1;
			req1.target_system = MAVLink.CURRENT_SYSID;
			req1.target_component = 0;
			ba.sendBytesToComm( MAVLink.createMessage(req1));
		
			//char[] name = MAVLink.StringNameToInt("SIMPLE");
			//msg_param_request_read req2 = new msg_param_request_read();
			//req2.param_index = -1;
			//req2.target_system = MAVLink.CURRENT_SYSID;
			//req2.target_component = 0;
			//req2.param_id = name;
			//ba.sendBytesToComm( MAVLink.createMessage(req2));
		}
	}
	public void vibe(int len){
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
	}
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources resources=getResources(); 
		String urlGet = resources.getString(R.string.checkForUpdates);
		SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);//set audio stream controls
		if( ! settings.contains(getString(R.string.kmlLog))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean( getString(R.string.kmlLog), false );
			editor.commit();
		}else{
			CommonSettings.kmlLog = settings.getBoolean(getString(R.string.kmlLog), false);
		}
		if( ! settings.contains(getString(R.string.tlog))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean( getString(R.string.tlog), false );
			editor.commit();
		}else{
			CommonSettings.tlog = settings.getBoolean(getString(R.string.tlog), false);
		}
		if( ! settings.contains(getString(R.string.linkType))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(CommunicationClient.LINK_TYPE, CommonSettings.LINK_BLUE);
			CommonSettings.currentLink = CommonSettings.LINK_BLUETOOTH;
			editor.commit();
		}
		if( ! settings.contains(getString(R.string.telemetryProtocol))){
			SharedPreferences.Editor editor = settings.edit();	
			editor.putString(CommunicationClient.ACTIVE_PROTOCOL, CommonSettings.MAVLink);
			CommonSettings.currentProtocol = CommonSettings.MAVLINK_PROTOCOL;
			editor.commit();
		}
		if( ! settings.contains(getString(R.string.speakStatusMessage))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean( getString(R.string.speakStatusMessage), true );
			editor.commit();
		}
		if( ! settings.contains(getString(R.string.keepScreenOn))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean( getString(R.string.keepScreenOn), true );
			editor.commit();
		}
		if( ! settings.contains(getString(R.string.speakModeChange))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean( getString(R.string.speakModeChange), true );
			editor.commit();
		}
		if( ! settings.contains(getString(R.string.speakWaypointReached))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean( getString(R.string.speakWaypointReached) , true );
			editor.commit();
		}
		if( ! settings.contains(getString(R.string.speakConnectionStatus))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean( getString(R.string.speakConnectionStatus), true );
			editor.commit();
		}
		if( ! settings.contains(getString(R.string.speakLowBattery))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean( getString(R.string.speakLowBattery), true );
			editor.commit();
		}
		if( ! settings.contains(getString(R.string.speakWaypointNext))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean( getString(R.string.speakWaypointNext), true );
			editor.commit();
		}
		if( ! settings.contains(getString(R.string.gpsOnMission))){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean( getString(R.string.gpsOnMission), true );
			editor.commit();
		}
		if(settings.contains(CommunicationClient.DEFAULT_ORIENTATION)){
			String address = settings.getString(CommunicationClient.DEFAULT_ORIENTATION, "");
			
			int orient = CommonSettings.ORIENTATION_DEFAULT;
			
			if(	address.equals(CommonSettings.LANDSCAPE))
				orient = CommonSettings.ORIENTATION_LANDSCAPE;
			else if(	address.equals(CommonSettings.PORTRAIT))
				orient = CommonSettings.ORIENTATION_PORTRAIT;

			if( orient != CommonSettings.ORIENTATION_DEFAULT){
				if( CommonSettings.setOrientation(this, orient))
					return;
			}
				
		}

		setContentView(R.layout.main_activity); 
    
		setupButton = (Button) findViewById(R.id.buttonSetup);
		setupButton.setOnClickListener(this);

		GPSButton = (Button) findViewById(R.id.gpsButton);
		GPSButton.setOnClickListener(this);

		readouts = (Button) findViewById(R.id.readoutsButton);
		readouts.setOnClickListener(this);
  
		status = (Button) findViewById(R.id.statusButton);
		status.setOnClickListener(this);

		params = (Button) findViewById(R.id.paramView);
		params.setOnClickListener(this);

		hud = (Button) findViewById(R.id.hudButton);
		hud.setOnClickListener(this);

		mission = (Button) findViewById(R.id.missionButton);
		mission.setOnClickListener(this);

		mode = (Button) findViewById(R.id.modeButton);
		mode.setOnClickListener(this);
		
		cli = (Button) findViewById(R.id.cliButton);
		cli.setOnClickListener(this);
		
		connectLabel = (TextView) findViewById(R.id.connectLabel);
		extraInfo1 = (TextView) findViewById(R.id.extraInfo1);
		extraInfo2 = (TextView) findViewById(R.id.extraInfo2);
		extraInfo3 = (TextView) findViewById(R.id.extraInfo3);
		extraInfo4 = (TextView) findViewById(R.id.extraInfo4);
		extraInfo5 = (TextView) findViewById(R.id.extraInfo5);
		
		ba.init();
		new XmlTask().execute(urlGet);
		//SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
		if ( settings.getBoolean(getString(R.string.keepScreenOn), true) ){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}	
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
	    switch (item.getItemId()) {	
		    case R.id.menuConnect:
		        ba.module.setFirstTimeEnableDevice(true);
		        ba.module.setFirstTimeListDevices(true);
		        connectLabel.setTextColor(0xFFFF0000);
		        connectLabel.setText("Connecting to Bridge");
		        ba.sendMessage(BluetoothModule.MSG_GET_STATUS);
		    	return true;
		    	
		    case R.id.ttsOptions:
		    	startActivity(new Intent(this, ttsOptionsWidget.class));
		    	return true;
		    	
		    case R.id.exit:
		    	finish();
		    	return true;
		    
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
/*
	private void selectLink() {
		final CharSequence[] items = {"Bluetooth", "USB (Not Working)"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a Link type");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	
				// Save the modem for next time
				SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				
				if(item == 1 && IProxy.isUSBSupported()){
					//editor.putString(CommunicationClient.LINK_TYPE, ProtocolParser.LINK_USB_);
					//ProtocolParser.currentLink = ProtocolParser.LINK_USB;
				    connectLabel.setText("USB Not Supported");
				}else{
					editor.putString(CommunicationClient.LINK_TYPE, CommonSettings.LINK_BLUE);
					CommonSettings.currentLink = CommonSettings.LINK_BLUETOOTH;
				    connectLabel.setText("Bluetooth Selected");
				    
				}
				
			    editor.commit();

			    
			}
		});
		
		AlertDialog alert = builder.create();
		
		alert.show();
	}

	private void selectOrientation() {
		final CharSequence[] items = {"Portrait", "Landscape", "App Recommended"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Default Screen Orientation");
		
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    
				SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				
				switch( item){
					case 0:
						editor.putString(CommunicationClient.DEFAULT_ORIENTATION, CommonSettings.PORTRAIT);
						CommonSettings.desiredOrientation = CommonSettings.ORIENTATION_PORTRAIT;
						break;
					case 1:
						editor.putString(CommunicationClient.DEFAULT_ORIENTATION, CommonSettings.LANDSCAPE);
						CommonSettings.desiredOrientation = CommonSettings.ORIENTATION_LANDSCAPE;
						break;
					case 2:
						editor.putString(CommunicationClient.DEFAULT_ORIENTATION, CommonSettings.DEFAULT);
						CommonSettings.desiredOrientation = CommonSettings.ORIENTATION_DEFAULT;
						break;
				}
				
			    editor.commit();

			}
		});
		
		AlertDialog alert = builder.create();
		
		alert.show();
	}
*/	
	private void selectProtocol() {
		final CharSequence[] items = {"ArduCopter1", "MAVLink"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a Protocol");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	
				// Save the modem for next time
				SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				if(item == 0){
					editor.putString(CommunicationClient.ACTIVE_PROTOCOL, CommonSettings.AC1);
					CommonSettings.currentProtocol = CommonSettings.AC1_PROTOCOL;
					
				}else{
					editor.putString(CommunicationClient.ACTIVE_PROTOCOL, CommonSettings.MAVLink);
					CommonSettings.currentProtocol = CommonSettings.MAVLINK_PROTOCOL;
					
				}
				
			    editor.commit();
			    connectLabel.setText("Protocol Selected");
			}
		});
		
		AlertDialog alert = builder.create();
		
		alert.show();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_window, menu);
	    return true;
	    
	}
	/*
	private static String byte2int(byte [] buffer){  
        String h = "";  
          
        for(int i = 0; i < buffer.length; i++){  
        	int temp = (buffer[i] & 0xFF);

            h = h + " "+ temp;  
        }  
          
        return h;  
          
    }  
	*/
	public void onClick(View v) {
		if (v == setupButton) {
			startActivity(new Intent(this, SetupActivity.class));
					
		} else if (v == GPSButton) {
			startActivity(new Intent(this, GPSActivity.class));
			
		} else if (v == readouts) {
			startActivity(new Intent(this, ReadOutsActivity.class));

		} else if (v == status) {
			startActivity(new Intent(this, StatusActivity.class));
			
		} else if(v == params){
			startActivity(new Intent(this, ParameterViewActivity.class));
//			msg_waypoint_request_list list = new msg_waypoint_request_list();
//			list.target_component = 0;
//			list.target_system = MAVLink.CURRENT_SYSID;
//			Log.d("sd", "writing : "+byte2int(MAVLink.createMessage(list)));
						
		} else if(v == hud){
			startActivity(new Intent(this, HUDActivity.class));
			
		} else if(v == mission){
			startActivity(new Intent(this, MissionActivity.class));
			
		} else if(v == mode){
			startActivity(new Intent(this, ModeSelectionActivity.class));

		} else if(v == cli){
			startActivity(new Intent(this, CLIActivity.class));
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ba.onDestroy();
	}
	
    protected void onResume() {
    	super.onResume();
		//Toast toast=Toast.makeText(this, "Resume", Toast.LENGTH_SHORT);
	   // toast.setGravity(Gravity.TOP, 0, 50);
	   // toast.show();
	}
    
	protected void onStop() {
		super.onStop();
		//Toast toast=Toast.makeText(this, "Stop", Toast.LENGTH_SHORT);
	   // toast.setGravity(Gravity.TOP, 0, 50);
	   // toast.show();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		ba.onActivityResult(requestCode, resultCode, data);

	}

	CommunicationClient ba = new CommunicationClient(this) {
		
		@Override
		public void notifyConnected() {
			
			if( CommonSettings.isNone())
				selectProtocol();
			
			if( CommonSettings.isProtocolAC1()){
				connectLabel.setText(R.string.connected);
				params.setEnabled(false);
				
			}
			else if( CommonSettings.isProtocolMAVLink()){
				connectLabel.setText(R.string.linked);

				msg_heartbeat msg = new msg_heartbeat();
				msg.type = MAVLink.MAV_TYPE.MAV_TYPE_GCS;
				msg.autopilot = MAVLink.MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC;
				sendBytesToComm(MAVLink.createMessage(msg));
			
			}else{
				connectLabel.setText(R.string.protocolSelected);
				
			}
			connectLabel.setTextColor(0xFF00FF00);

		}

		@Override
		public void notifyDeviceNotAvailable() {
			connectLabel.setText(R.string.deviceAvailable);
			connectLabel.setTextColor(0xFFFF0000);
		}

		@Override
		public void notifyDisconnected() {
			if( CommonSettings.isNone())
				connectLabel.setText(R.string.protocolError);
			
			if( CommonSettings.isProtocolAC1())
				connectLabel.setText("Disconnected");

			else if( CommonSettings.isProtocolMAVLink())
				connectLabel.setText(R.string.mavlinkError);
				connectLabel.setTextColor(0xFFFF0000);
			
		}
		
		@Override
		public void notifyReceivedData(int count,  IMAVLinkMessage m) {
			if( m != null){
				switch(m.messageType){
					case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:{
						MAVLink.CURRENT_SYSID = m.sysID;
						MAVLink.ARDUCOPTER_COMPONENT_ID = m.componentID;
						
						connectLabel.setText(R.string.heartbeat);
						if (connectLabel.getCurrentTextColor() == 0xFF00FF00){
							connectLabel.setTextColor(0xFFFF00FF);
						}else{
							connectLabel.setTextColor(0xFF00FF00);
						}
						if ( first ){
							startup();
							first = false;
						}
						break;
					}
					case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:{
						msg_statustext msg = (msg_statustext) m;
						
						String s = new String(msg.text);
						s = s.replaceAll("[^A-Za-z0-9 ]", "");
						
						extraInfo1.setText(extraInfo2.getText());
						extraInfo2.setText(extraInfo3.getText());
						extraInfo3.setText(extraInfo4.getText());
						extraInfo4.setText(extraInfo5.getText());
						extraInfo5.setText(s);
						
						if (s.contains("Ready to FLY")){
							extraInfo5.setText("");
							extraInfo4.setText("");
							extraInfo3.setText("");
							extraInfo2.setText("");
							extraInfo1.setText("");
						}
						break;
					}
				}
			}
		}
	};
	
	public class XmlTask extends AsyncTask<String, Void, String>{

	    public String doInBackground(String... urls){
	        String url = urls[0];
	        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	        String uid = tManager.getDeviceId();
	        PackageInfo pinfo = null;
			
			try {
				pinfo = getPackageManager().getPackageInfo("com.bvcode.ncopter", 0);
			} catch (NameNotFoundException e1) {
				e1.printStackTrace();
			}
			
	        //int versionNumber = pinfo.versionCode;
	        String versionName = pinfo.versionName;
	        
	        HttpClient httpClient = new DefaultHttpClient();
	        HttpContext localContext = new BasicHttpContext();
	        //Log.d("Debug:",url + "?id=" + uid + "&version=" + versionName);
	        HttpGet httpGet = new HttpGet(url + "?id=" + uid + "&version=" + versionName);
	        HttpResponse response = null ;
			try {
				response = httpClient.execute(httpGet, localContext);
				HttpEntity entity = response.getEntity();
		        String xml = null;
				try {
					xml = EntityUtils.toString(entity);
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		        return xml;
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
	    }
	    public void onPostExecute(String xml){
	    	if (xml == null){
	    		return;
	    	}
	    	//Log.d("Debug:", "" + xml);
	    	String tagOpen = "<UPDATE>";
            String tagClose = "</UPDATE>";
            String value = null;
            if (xml.indexOf(tagOpen) != -1) {
                int start = xml.indexOf(tagOpen) + tagOpen.length();
                int end = xml.indexOf(tagClose);
                value = xml.substring(start, end);
            }
            tagOpen = "<MSG>";
            tagClose = "</MSG>";
            String message = null;
            if (xml.indexOf(tagOpen) != -1) {
                int start = xml.indexOf(tagOpen) + tagOpen.length();
                int end = xml.indexOf(tagClose);
                message = xml.substring(start, end);
            }
            tagOpen = "<URL>";
            tagClose = "</URL>";
            String tmp = null;
            if (xml.indexOf(tagOpen) != -1) {
            	int start = xml.indexOf(tagOpen) + tagOpen.length();
            	int end = xml.indexOf(tagClose);
            	tmp = xml.substring(start, end);
            }
            final String gotoUrl = tmp;
            if( value.indexOf("update") != -1 ){
		        AlertDialog.Builder alertbox = new AlertDialog.Builder(MainActivity.this);
		        alertbox.setMessage(message);
		        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface arg0, int arg1) {
		            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gotoUrl));
						startActivity(browserIntent);
		            }
		        });
		        alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface arg0, int arg1) {
						finish();
		            }
		        });
		        alertbox.show();
            }    
	    }
	}
}
