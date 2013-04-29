package com.bvcode.ncopter.mission;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
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
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.MAVLink;
import com.MAVLink.VerifiedMAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_command_ack;
import com.MAVLink.Messages.common.msg_command_long;
import com.MAVLink.Messages.common.msg_gps_raw_int;
import com.MAVLink.Messages.common.msg_heartbeat;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.MAVLink.Messages.common.msg_waypoint;
import com.MAVLink.Messages.common.msg_waypoint_ack;
import com.MAVLink.Messages.common.msg_waypoint_clear_all;
import com.MAVLink.Messages.common.msg_waypoint_count;
import com.MAVLink.Messages.common.msg_waypoint_current;
import com.MAVLink.Messages.common.msg_waypoint_reached;
import com.MAVLink.Messages.common.msg_waypoint_request;
import com.MAVLink.Messages.common.msg_waypoint_request_list;
import com.MAVLink.Messages.common.msg_waypoint_set_current;
import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.MainActivity;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.AC1Data.ProtocolParser;
import com.bvcode.ncopter.comms.CommunicationClient;
import com.bvcode.ncopter.gps.GPSOverlay;
import com.bvcode.ncopter.gps.PathOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MissionActivity extends MapActivity{

	private MapController mapController = null;
	boolean firstGPS = true;
		
	List<Overlay> mapOverlays = null;
	
	GPSOverlay itemizedoverlay = null;  
	MyLocationOverlay phoneLocationOverlay = null;
	PathOverlay pathOverlay = new PathOverlay();
	
	static MapView mapView = null;
	static MissionOverlay missionOverlay = null;
	
	VerifiedMAVLink verifiedMAVLink = null;
	
	private int lastMode = 100;
	private TextView mapMode;
	private TextView mapNextWP;
	private TextView mapLastWP;
	
	static protected LinkedList<msg_waypoint > waypoints= new LinkedList<msg_waypoint >();
	
	static Comparator<msg_waypoint> comparator = new Comparator<msg_waypoint>() {
		public int compare(msg_waypoint paramT1, msg_waypoint paramT2) {
			return	new Integer(paramT1.seq).compareTo( paramT2.seq);
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		if( CommonSettings.setOrientation(this, -1)){
			return;
		}else{
			switch (this.getResources().getConfiguration().orientation){
				case Configuration.ORIENTATION_PORTRAIT:
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
				case Configuration.ORIENTATION_LANDSCAPE:
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					break;
			}
		}
		//set audio stream controls
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
				
	    setContentView(R.layout.gps_view);
	    
	    mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    mapView.setSatellite(true);
	    itemizedoverlay = new GPSOverlay(this.getResources().getDrawable(R.drawable.crosshair_blue), this);
	 
	    missionOverlay= new MissionOverlay(this.getResources().getDrawable(R.drawable.map_icon), this, mapView);
	        
	    phoneLocationOverlay = new MyLocationOverlay(this, mapView);
	    //phoneLocationOverlay.enableCompass();
	    phoneLocationOverlay.enableMyLocation();
	    
	    // Add the overlays
	    mapOverlays = mapView.getOverlays();
	    mapOverlays.add(missionOverlay);
	    mapOverlays.add(phoneLocationOverlay);
	    mapOverlays.add(itemizedoverlay);
	    mapOverlays.add(pathOverlay);
	    	    
	    phoneLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapController.setCenter(phoneLocationOverlay.getMyLocation());
				firstGPS = false;
				SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
				if ( ! settings.getBoolean(getString(R.string.gpsOnMission), true) ){
					phoneLocationOverlay.disableMyLocation();
				}
			}
		});

	    mapController = mapView.getController();
	    mapController.setZoom(19);

	    ba.init();
	    
	    verifiedMAVLink = new VerifiedMAVLink(ba);
	    
	    SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
	  	if ( settings.getBoolean(getString(R.string.keepScreenOn), true) ){
	  		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	  	}
	}
		
	public void setCurrentWP(int WP){
		msg_waypoint_set_current list = new msg_waypoint_set_current();
		list.target_component = MAVLink.MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;
		list.target_system = MAVLink.CURRENT_SYSID;
		list.seq = WP;
		ba.sendBytesToComm(MAVLink.createMessage(list));
		Log.d("Set WP Sent:", "" + WP);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mission, menu);
	    return true;
	    
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.menuEnableEdit:
	    		missionOverlay.enableEdit = !missionOverlay.enableEdit;
	    		if (missionOverlay.enableEdit){
	    			item.setTitle(R.string.disableWPDrag);
	    			
	    		}else{
	    			item.setTitle(R.string.enableWPDrag);
	    		}
	    		break;
	    	case R.id.menuEditMission:
	    		startActivity(new Intent(this, MissionListActivity.class));
	    		break;
	    		
	    	case R.id.menuClearMission:
	    		waypointsClear();
	    		msg_waypoint_clear_all t = new msg_waypoint_clear_all();
	    		t.target_system = MAVLink.CURRENT_SYSID;
				t.target_component = MAVLink.MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;//WAYPOINTPLANNER;
    			ba.sendBytesToComm(  MAVLink.createMessage( t ) );
	    		missionOverlay.notifyDataChanged();
	    		break;
	    		
	    	case R.id.menuSaveMission:
    			if( getWaypointSize() != 0){
	    			msg_waypoint_count test = new msg_waypoint_count();
	    			test.target_system = MAVLink.CURRENT_SYSID;
					test.target_component = MAVLink.MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;//WAYPOINTPLANNER;
					test.count = getWaypointSize();
	    			ba.sendBytesToComm(  MAVLink.createMessage( test ) );

	    		}else{
	    			Toast toast=Toast.makeText(this, "No Mission to Save", Toast.LENGTH_SHORT);
	    		    toast.setGravity(Gravity.TOP, 0, 50);
	    		    toast.show();
	    		}
	    		break;
	    	case R.id.menuCommands:{
	    		final CharSequence[] items = {"Start Mission", "Return to Launch","Servo 6 Set"};

	    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    		builder.setTitle("Send a Command");
	    		builder.setItems(items, new DialogInterface.OnClickListener() {
	    		    public void onClick(DialogInterface dialog, int item) {
	    		    	msg_command_long list = new msg_command_long();
	    		    	list.target_component = 0;
	                	list.target_system = MAVLink.CURRENT_SYSID;
	                	list.confirmation = 0;
	    		    	switch (item){
	    		    		case 0:{
	    		    			//MAV_CMD_MISSION_START
	    		    			list.command = 300; 
	    		    			ba.sendBytesToComm(MAVLink.createMessage(list));

	    		    			Log.d("Mission Activity", "Attempting to Send Command: MAV_CMD_MISSION_START");
	    		    			
	    		    			break;
	    		    		}
	    		    		case 1:{
	    		    			//MAV_CMD_NAV_RETURN_TO_LAUNCH
	    		            	list.command = 20; 
	    		            	list.param1 = 0;
	    		            	ba.sendBytesToComm(MAVLink.createMessage(list));
	    		    			
	    		    			Log.d("Mission Activity", "Attempting to Send Command: MAV_CMD_NAV_RETURN_TO_LAUNCH");
	    		    			
	    		    			break;
	    		    		}
	    		    		case 2:{
	    		    			//DO_REPEAT_SERVO=184 Cycle a between its nominal setting and a desired PWM for a desired number of cycles with a desired period. |Servo number| PWM (microseconds, 1000 to 2000 typical)| Cycle count| Cycle time (seconds)| Empty| Empty| Empty|  </summary>
	    		            	list.command = 184;
	    		            	list.param1 = 6;
	    		            	list.param2 = 2000;
	    		            	list.param3 = 1;
	    		            	list.param4 = 1;
	    		    			ba.sendBytesToComm(MAVLink.createMessage(list));
				    		    
				    		    Log.d("Mission Activity", "Attempting to Send Command: DO_REPEAT_SERVO");
				    		    
	    		    			break;
	    		    		}
	    		    	}
	    		    }
	    		});
	    		builder.setNegativeButton("Cancel", null);
	    		AlertDialog alert = builder.create();
	    		
	    		alert.show();
	    		//Log.d("Debug Command", "COMMAND 3");
	    		
	    		//msg_command_long list = new msg_command_long();

	    		//list.target_component = MAVLink.MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;//WAYPOINTPLANNER;
            	//list.target_system = MAVLink.CURRENT_SYSID;
            	//list.confirmation = 0;
            	
            	//list.command = 300;//MAV_CMD_MISSION_START - works
            	
            	//DO_SET_RELAY=181 Set a relay to a condition. |Relay number| Setting (1=on, 0=off, others possible depending on system hardware)| Empty| Empty| Empty| Empty| Empty|  </summary>
            	//list.command = 181;
            	//list.param1 = 1;
            	//list.param2 = 1;
            	
            	//DO_REPEAT_RELAY=182 Cycle a relay on and off for a desired number of cyles with a desired period. |Relay number| Cycle count| Cycle time (seconds, decimal)| Empty| Empty| Empty| Empty|  </summary>
            	//list.command = 182;
            	//list.param1 = 1;
            	//list.param2 = 1;
            	//list.param3 = 1;
            	
            	//DO_SET_SERVO=183 Set a servo to a desired PWM value. |Servo number| PWM (microseconds, 1000 to 2000 typical)| Empty| Empty| Empty| Empty| Empty|  </summary>
            	//list.command = 183;
            	//list.param1 = 1;
            	//list.param2 = 1200;
            	
            	//DO_REPEAT_SERVO=184 Cycle a between its nominal setting and a desired PWM for a desired number of cycles with a desired period. |Servo number| PWM (microseconds, 1000 to 2000 typical)| Cycle count| Cycle time (seconds)| Empty| Empty| Empty|  </summary>
            	//list.command = 184;
            	//list.param1 = 1;
            	//list.param2 = 1200;
            	//list.param3 = 1;
            	
            	//list.command = 179;//MAV_CMD_DO_SET_HOME
            	//list.param1 = 3;
            	//list.param2 = 0;
            	
            	//list.command = 177;//MAV_CMD_DO_JUMP - doesnt work yet
            	//list.param1 = 3;
            	//list.param2 = 0;
            	
            	//MAV_CMD_NAV_RETURN_TO_LAUNCH - works
            	//list.command = 20; 
            	//list.param1 = 0;
            	
            	//list.param1 = 0;
            	//list.param2 = 0;
            	//list.param3 = 0;
            	//list.param4 = 0;
            	//list.param5 = 0;
            	//list.param6 = 0;
            	//list.param7 = 0;
            	//ba.sendBytesToComm(MAVLink.createMessage(list));
            	
            	/*
    		    AlertDialog.Builder alertbox = new AlertDialog.Builder(MissionActivity.this);
		        alertbox.setMessage("Would you like to set Home as current position?");
		        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface arg0, int arg1) {
		            	msg_command list = new msg_command();
		            	list.target_system = MAVLink.CURRENT_SYSID;
		            	list.target_component = 0;
		            	//list.command = 179;//MAV_CMD_DO_SET_HOME
		            	//list.command = 176;//MAV_CMD_DO_SET_MODE=176
		            	//list.command = 300; //MAV_CMD_MISSION_START=300
		          //  	list.command = 177;	//MAV_CMD_DO_JUMP=177
		            	list.confirmation = 1;
		            	list.param1 = 4;
		            	//public int target_system; ///< System which should execute the command
		            	//public int target_component; ///< Component which should execute the command, 0 for all components
		            	//public int command; ///< Command ID, as defined by MAV_CMD enum.
		            	//public int confirmation; ///< 0: First transmission of this command. 1-255: Confirmation transmissions (e.g. for kill command)
		            	//public float param1; ///< Parameter 1, as defined by MAV_CMD enum.
		            	//public float param2; ///< Parameter 2, as defined by MAV_CMD enum.
		            	//public float param3; ///< Parameter 3, as defined by MAV_CMD enum.
		            	//public float param4; ///< Parameter 4, as defined by MAV_CMD enum.
		            	ba.sendBytesToComm(MAVLink.createMessage(list));
		            }
		        });
		        alertbox.setNegativeButton("No", null);
		        alertbox.show();	 
		        */   		
		    	}	
	    		break;
	    	case R.id.menuLoadMission:{
	    		waypointsClear();
	    		
	    		// Request the current waypoints as well.
				msg_waypoint_request_list list = new msg_waypoint_request_list();
				list.target_component = 0;
				list.target_system = MAVLink.CURRENT_SYSID;
				ba.sendBytesToComm(MAVLink.createMessage(list));
	    		return true;
	    	}	    
	    }
	    return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		//if( phoneLocationOverlay != null)
			//phoneLocationOverlay.enableCompass();
		if( missionOverlay != null)
			missionOverlay.notifyDataChanged();
		if( mapView != null)
			mapView.invalidate();
	}

	@Override
	public void onPause() {
		super.onPause();	
		//if( phoneLocationOverlay != null)
			//phoneLocationOverlay.disableCompass();
	} 
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(phoneLocationOverlay != null){
			phoneLocationOverlay.disableMyLocation();
			//phoneLocationOverlay.disableCompass();
		}
		if( mapOverlays != null)
			mapOverlays.clear();
		ba.onDestroy();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		ba.onActivityResult(requestCode, resultCode, data);
	}
	
	CommunicationClient ba = new CommunicationClient(this) {
		@Override
		public void notifyConnected() {
			if( CommonSettings.isProtocolAC1())
				ba.sendBytesToComm(ProtocolParser.requestGPSData());
			else if (CommonSettings.isProtocolMAVLink()){
				msg_request_data_stream req = new msg_request_data_stream();
				req.req_message_rate = 1;

				req.req_stream_id = MAVLink.MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS;
				req.start_stop = 1;
				req.target_system = MAVLink.CURRENT_SYSID;
				req.target_component = 0;
				ba.sendBytesToComm( MAVLink.createMessage(req));
			}
			
			synchronized (waypoints) {
				if( waypoints.size()==0){
			        AlertDialog.Builder alertbox = new AlertDialog.Builder(MissionActivity.this);
			        alertbox.setMessage("Would you like to load the Mission from the UAV?");
			        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		
			            // do something when the button is clicked
			            public void onClick(DialogInterface arg0, int arg1) {
			            	msg_waypoint_request_list list = new msg_waypoint_request_list();
							list.target_component = 0;
							list.target_system = MAVLink.CURRENT_SYSID;
							ba.sendBytesToComm(MAVLink.createMessage(list));
			            }
			        });
			        alertbox.setNegativeButton("No", null);
			        alertbox.show();
				}
			}

		}

		@Override
		public void notifyReceivedData(int count, IMAVLinkMessage m) {

			if(CommonSettings.isProtocolMAVLink()){
				switch(m.messageType){
					case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:{
						msg_command_ack msg = (msg_command_ack) m;
						Toast toast = Toast.makeText(MissionActivity.this, "CMD ID: " + MAVLink.getMavCmd(msg.command) + "\nResult: " + MAVLink.getMavResult(msg.result), Toast.LENGTH_LONG);
						toast.setGravity(Gravity.TOP, 0, 50);
						toast.show();
						//Log.d("Receiving", "Command ACK");
						}
						break;
					case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:{
				    	msg_heartbeat msg = (msg_heartbeat) m;
				    	if ((int)msg.custom_mode != lastMode){
				    		mapMode = (TextView) findViewById(R.id.mapMode);
				    		Resources res = getResources();
							String[] mavModes = null;
							if ( CommonSettings.uavType == 2){ //quad
								mavModes = res.getStringArray(R.array.mode_array_copter);
							}else { // if ( CommonSettings.uavType == 1){ //fixed wing
								mavModes = res.getStringArray(R.array.mode_array);
							}
				    		//mapMode.setTextColor(0xFFFF0000);
				    		if ( msg.custom_mode <= mavModes.length ){
				    			mapMode.setText(mavModes[(int)msg.custom_mode]);
				    			lastMode = (int)msg.custom_mode;
				    		}
				    	}	
				    	break;
					}
					case msg_waypoint_current.MAVLINK_MSG_ID_WAYPOINT_CURRENT:{
						msg_waypoint_current msg = (msg_waypoint_current) m;
						mapNextWP = (TextView) findViewById(R.id.mapNextWP);
						mapNextWP.setText("Next WP: " + msg.seq);
						break;
					}
					case msg_waypoint_reached.MAVLINK_MSG_ID_WAYPOINT_REACHED:{
						msg_waypoint_reached msg = (msg_waypoint_reached) m;
						mapLastWP = (TextView) findViewById(R.id.mapLastWP);
						mapLastWP.setText("WP Reached: " + msg.seq);
						break;
					}
					case msg_waypoint_ack.MAVLINK_MSG_ID_WAYPOINT_ACK:{
						msg_waypoint_ack msg = (msg_waypoint_ack) m;
						if ( msg.type == 0 ){
							Toast toast = Toast.makeText(MissionActivity.this, "Received Waypoint Acknowledgement\nAll waypoints saved.", Toast.LENGTH_LONG);
							toast.setGravity(Gravity.TOP, 0, 50);
							toast.show();
						}else{
							Toast toast = Toast.makeText(MissionActivity.this, "Error Saving Waypoints", Toast.LENGTH_LONG);
							toast.setGravity(Gravity.TOP, 0, 50);
							toast.show();
						}
						break;
					}
					case msg_waypoint_request.MAVLINK_MSG_ID_WAYPOINT_REQUEST:{
						msg_waypoint_request msg = (msg_waypoint_request)m;
						if ( msg != null ){
							msg_waypoint waypoint = getWaypoint(msg.seq);
							waypoint.target_system = MAVLink.CURRENT_SYSID;
							waypoint.target_component = MAVLink.MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;//WAYPOINTPLANNER;
							ba.sendBytesToComm(  MAVLink.createMessage( waypoint ) );
						}    		
						break;
					}
					case msg_waypoint.MAVLINK_MSG_ID_WAYPOINT:{
						msg_waypoint msg = (msg_waypoint)m;
						verifiedMAVLink.verifyReceipt(""+msg.seq);
						
						if( verifiedMAVLink.isDone() ){
							Toast toast=Toast.makeText(MissionActivity.this, "Loaded entire Mission", Toast.LENGTH_LONG);
			    		    toast.setGravity(Gravity.TOP, 0, 50);
			    		    toast.show();
								
			    		    // Acknowledge receipt
							msg_waypoint_ack waypoint = new msg_waypoint_ack();
							waypoint.target_system = MAVLink.CURRENT_SYSID;
							waypoint.target_component = MAVLink.MAV_COMPONENT.MAV_COMP_ID_MISSIONPLANNER;//WAYPOINTPLANNER;
							ba.sendBytesToComm(  MAVLink.createMessage( waypoint ) );    
						}
						synchronized (waypoints) {
							boolean found = false;
							for( msg_waypoint exist: waypoints){
								if( exist.seq == msg.seq){
									exist = msg;
									found = true;
									
								}
							}
							
							if( !found ){
								waypoints.add(msg);
								Collections.sort(waypoints, comparator);
								missionOverlay.notifyDataChanged();
								
							}
						}
						break;	
					}
					
					case msg_waypoint_count.MAVLINK_MSG_ID_WAYPOINT_COUNT:{
						msg_waypoint_count msg = (msg_waypoint_count) m;
						waypointsClear();
						missionOverlay.notifyDataChanged();

						if( msg.count > 0){
							
							for( int i = 0; i < msg.count; i++){
								msg_waypoint_request req = new msg_waypoint_request();
								req.target_component = 0;
								req.target_system = MAVLink.CURRENT_SYSID;
								req.seq = i;
								verifiedMAVLink.put(""+i, MAVLink.createMessage(req));
								
							}
							verifiedMAVLink.start(5);
							
						}
						
						break;
					}
								
					case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:{
						msg_gps_raw_int msg = (msg_gps_raw_int) m;
						if( msg.fix_type > 1){
							GeoPoint point = new GeoPoint((int)(msg.lat/10), (int)(msg.lon/10));
							mapController.setCenter(point); //follow uav
							itemizedoverlay.setCopterOverlay(new OverlayItem(point, "UAV Location", "Current Location"));
							pathOverlay.addNewLocation( point, (int) msg.alt);
							
							mapView.postInvalidate();
						
							if(firstGPS){
								mapController.setCenter(point);
								firstGPS = false;
								
							}
						}
						
						break;
					}
				}		
			}
		}

		@Override
		public void notifyDisconnected() {
			
		}

		@Override
		public void notifyDeviceNotAvailable() {
			
		}
		
	};
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;

	}

	public static void add(msg_waypoint msg) {
		synchronized (waypoints) {
			waypoints.add( msg);
			Collections.sort(waypoints, comparator);
			
		}
	}

	public static int getWaypointSize() {
		int res;
		synchronized (waypoints) {
			res = waypoints.size();
			
		}
		return res;
	}

	
	public static msg_waypoint getWaypoint(int i) {
		msg_waypoint res;
		synchronized (waypoints) {
			res = waypoints.get(i);
			
		}
		return res;
	}

	public static msg_waypoint getLastWaypoint() {
		msg_waypoint res = null;
		synchronized (waypoints) {
			if( waypoints.size() > 0)
				res = waypoints.getLast();
			
		}
		return res;
	}

	public static void waypointsClear() {
		synchronized (waypoints) {
			while (waypoints.size() > 1) {
				waypoints.removeLast();
		    }
		}
	}

	public static void swap(int from, int to){
		synchronized (waypoints) {
			
			msg_waypoint A = waypoints.get(from);
			
			if( from > to){
				for(int i = to; i < waypoints.size(); i++)
					waypoints.get(i).seq += 1;
					
			}else if ( from < to){
				for(int i = from; i <= to; i++)
					waypoints.get(i).seq -= 1;
					
			}
			
			A.seq = to;
			
			Collections.sort(waypoints, comparator);
		
		}		
	}
	
	public static void remove(int paramInt) {
		synchronized (waypoints) {
			waypoints.remove(paramInt);
			
			Collections.sort(waypoints, comparator);
			
			// Reorder the numbers.
			for( int i = 0; i < waypoints.size(); i++)
				waypoints.get(i).seq = i;
			
			// Update the screen
			missionOverlay.notifyDataChanged();
			mapView.invalidate();
			
		}	
	}
	public void vibe(int len){
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(len);
		
	}
	public double getAltitude(double latitude, double longitude) {
        double result = Double.NaN;
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        String url = "http://maps.googleapis.com/maps/api/elevation/"
                + "xml?locations=" + String.valueOf(latitude)
                + "," + String.valueOf(longitude)
                + "&sensor=true";
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet, localContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String respStr = EntityUtils.toString(entity);
                String tagOpen = "<elevation>";
                String tagClose = "</elevation>";
                if (respStr.indexOf(tagOpen) != -1) {
                    int start = respStr.indexOf(tagOpen) + tagOpen.length();
                    int end = respStr.indexOf(tagClose);
                    String value = respStr.substring(start, end);
                    result = (double)(Double.parseDouble(value));
                }
            }
        } catch (ClientProtocolException e) {} 
        catch (IOException e) {}
        if (Double.isNaN(result)){
        	result = 75;
        }
        return result;
    }
}
