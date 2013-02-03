package com.bvcode.ncopter;

import java.io.IOException;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_heartbeat;
import com.bvcode.ncopter.comms.BluetoothModule;
import com.bvcode.ncopter.comms.CommunicationClient;
import com.bvcode.ncopter.gps.GPSActivity;
import com.bvcode.ncopter.mission.MissionActivity;
import com.bvcode.ncopter.setup.SetupActivity;

public class MainActivity extends Activity implements OnClickListener {
	
	private Button setupButton, GPSButton, readouts, status, params, hud, mission, mode, cli;

	private TextView connectLabel;
//	private TextView extraInfo1;
//	private TextView extraInfo2;
//	private TextView extraInfo3;
//  private TextView extraInfo4;
	public MainActivity(){
		
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
		//extraInfo1 = (TextView) findViewById(R.id.extraInfo1);
		//extraInfo2 = (TextView) findViewById(R.id.extraInfo2);
		//extraInfo3 = (TextView) findViewById(R.id.extraInfo3);
		//extraInfo3 = (TextView) findViewById(R.id.extraInfo3);
		ba.init();
		new XmlTask().execute(urlGet);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.selectLink:
	    		selectLink();
	    		return true;
	    		
	    	/*case R.id.menuRC:
	    		startActivity(new Intent(this, RemoteControl.class));
				return true;*/
	    		
	    	case R.id.lockOrientation:
	    		selectOrientation();
	    		return true;
	    		
		    case R.id.menuConnect:
		        ba.module.setFirstTimeEnableDevice(true);
		        ba.module.setFirstTimeListDevices(true);
		        
		        connectLabel.setTextColor(0xFFFF0000);
		        connectLabel.setText("Connecting to Bridge");
				
		        ba.sendMessage(BluetoothModule.MSG_GET_STATUS);
				
		    	return true;
		    	
		    case R.id.setProtocol:
		    	selectProtocol();
		    	return true;
		    	
		    case R.id.clearDefaults:

		    	// Remove the default modem selection.
		    	SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
		        SharedPreferences.Editor editor = settings.edit();
		        editor.remove(CommunicationClient.DEFAULT_MODEM);
		        editor.remove(CommunicationClient.LINK_TYPE);
		        editor.remove(CommunicationClient.ACTIVE_PROTOCOL);
		        editor.remove(CommunicationClient.DEFAULT_ORIENTATION);
		        editor.commit();

		    	return true;
		    	
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}

	private void selectLink() {
		final CharSequence[] items = {"Bluetooth", "USB (Not Working)"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a Link type");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	
				// Save the modem for next time
				SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				
				if(item == 1 /*&& IProxy.isUSBSupported()*/){
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
	private static String byte2int(byte [] buffer){  
        String h = "";  
          
        for(int i = 0; i < buffer.length; i++){  
        	int temp = (buffer[i] & 0xFF);

            h = h + " "+ temp;  
        }  
          
        return h;  
          
    }  
	
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
	
			if( m.messageType == msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT){
				MAVLink.CURRENT_SYSID = m.sysID;
				MAVLink.ARDUCOPTER_COMPONENT_ID = m.componentID;

				connectLabel.setText(R.string.heartbeat);
				
				if (connectLabel.getCurrentTextColor() == 0xFF00FF00){
					connectLabel.setTextColor(0xFFFF00FF);
				}else{
					connectLabel.setTextColor(0xFF00FF00);
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
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	    }

	    public void onPostExecute(String xml){
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
