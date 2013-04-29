package com.bvcode.ncopter.gps;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_gps_raw_int;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.MAVLink.Messages.common.msg_waypoint;
import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.AC1Data.GPSData;
import com.bvcode.ncopter.AC1Data.ProtocolParser;
import com.bvcode.ncopter.comms.CommunicationClient;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GPSActivity extends MapActivity{

	private MapController mapController;
	boolean firstGPS = true;
	
	List<Overlay> mapOverlays;
	GPSOverlay itemizedoverlay;
	
	MyLocationOverlay phoneLocationOverlay;
	PathOverlay pathOverlay = new PathOverlay();
	private MapView mapView = null;
	private GuidedModeOverlay guidedModeOverlay;
	private float lastAltitude = 0;
	
	private boolean enableLoiterSet = false;
	private boolean enableGCSOffsetMode;
	private GeoPoint lastCopter = null;
	private int offsetX;
	private int offsetY;
	
	private TextView mapMode;
		
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
	    
	    itemizedoverlay = new GPSOverlay(this.getResources().getDrawable(R.drawable.bubble), this);
	    
	    guidedModeOverlay = new GuidedModeOverlay(this.getResources().getDrawable(R.drawable.crosshair), this, mapView);
	    
	    phoneLocationOverlay = new MyLocationOverlay(this, mapView);
	    phoneLocationOverlay.enableCompass();
	    phoneLocationOverlay.enableMyLocation();
	    
	    // Add the overlays
	    mapOverlays = mapView.getOverlays();
	    mapOverlays.add(guidedModeOverlay);
	    mapOverlays.add(phoneLocationOverlay);
	    mapOverlays.add(pathOverlay);
	    mapOverlays.add(itemizedoverlay);
	    
	    phoneLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapController.setCenter(phoneLocationOverlay.getMyLocation());
				firstGPS = false;
				
			}
		});

	    mapController = mapView.getController();
	    mapController.setZoom(20);

	    ba.init();
	    
	    SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
	  	if ( settings.getBoolean(getString(R.string.keepScreenOn), true) ){
	  		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	  	}
	}
		
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(phoneLocationOverlay != null){
			phoneLocationOverlay.disableMyLocation();
			phoneLocationOverlay.disableCompass();
		}
		
		if( mapOverlays != null)
			mapOverlays.clear();

		ba.onDestroy();
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		ba.onActivityResult(requestCode, resultCode, data);
		
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.menu_gcs_offset:
	    		enableGCSOffsetMode = !enableGCSOffsetMode;
	    		
	    		//TODO conditional on the accuracy
	    		mapMode = (TextView) findViewById(R.id.mapMode);
	    		if(lastCopter == null){
	    			Toast toast=Toast.makeText(this, "No GPS Fix", Toast.LENGTH_LONG);
	    		    toast.setGravity(Gravity.TOP, 0, 50);
	    		    toast.show();
	    		}else if(lastCopter != null && enableGCSOffsetMode){
	    			mapMode.setText("Follow Me On");
	    			GeoPoint phone = phoneLocationOverlay.getMyLocation();
	    			offsetX = lastCopter.getLatitudeE6() - phone.getLatitudeE6();
		    		offsetY = lastCopter.getLongitudeE6() - phone.getLongitudeE6();
		    		
	    			Toast toast=Toast.makeText(this, "Follow Me Enabled", Toast.LENGTH_LONG);
	    		    toast.setGravity(Gravity.TOP, 0, 50);
	    		    toast.show();
	    		    
		    		item.setTitle(R.string.disableFollowMe);
	    		}else{
	    			mapMode.setText("");
	    			enableGCSOffsetMode = false;
	    			Toast toast=Toast.makeText(this, "Follow Me Disabled", Toast.LENGTH_LONG);
	    		    toast.setGravity(Gravity.TOP, 0, 50);
	    		    toast.show();
	    		    
	    		    item.setTitle(R.string.enableFollowMe);
	    		}
	 
	    		break;
	    		
	    	case R.id.menu_set_loiter:
	    		enableLoiterSet = !enableLoiterSet;
	    		mapMode = (TextView) findViewById(R.id.mapMode);
	    		if(enableLoiterSet){
	    			mapMode.setText("Guided Mode On");
	    			Toast toast=Toast.makeText(this, "Enabled Guided Goto", Toast.LENGTH_LONG);
	    		    toast.setGravity(Gravity.TOP, 0, 50);
	    		    toast.show();
	    		    item.setTitle(R.string.disableLoiterSet);
	    		}else{
	    			mapMode.setText("");
	    			Toast toast=Toast.makeText(this, "Disabled Guided Goto", Toast.LENGTH_LONG);
	    		    toast.setGravity(Gravity.TOP, 0, 50);
	    		    toast.show();
	    		    item.setTitle(R.string.enableLoiterSet);
	    		}
	    		break;
	 
	    }
	    
	    return false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.gps_menu, menu);
	    return true;
	    
	}
	
	CommunicationClient ba = new CommunicationClient(this) {

		@Override
		public void notifyConnected() {
			if( CommonSettings.isProtocolAC1())
				ba.sendBytesToComm(ProtocolParser.requestGPSData());
			else if (CommonSettings.isProtocolMAVLink()){
				msg_request_data_stream req = new msg_request_data_stream();
				req.req_message_rate = 1;
				
				// position stream not working...
				//req.req_stream_id = MAVLink.MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION;
				req.req_stream_id = MAVLink.MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS;
				req.start_stop = 1;
				req.target_system = MAVLink.CURRENT_SYSID;
				req.target_component = 0;
				ba.sendBytesToComm( MAVLink.createMessage(req));
				
			}
		}

		@Override
		public void notifyDisconnected() {
			
		}

		@Override
		public void notifyDeviceNotAvailable() {
			
		}
		
		@Override
		public void notifyReceivedData(int count, IMAVLinkMessage m) {

			if( GPSData.class.isInstance(m) ){
				GPSData gd = (GPSData)m;
				if(gd.longitude != 0){
					GeoPoint point = new GeoPoint(gd.latitude, gd.longitude);
					itemizedoverlay.setCopterOverlay(new OverlayItem(point, "UAV", "Current Location"));
					
					pathOverlay.addNewLocation( point, gd.altitude);
					
					mapView.postInvalidate();
				
					if(firstGPS){
						mapController.setCenter(point);
						firstGPS = false;
						
					}
				}			
			}else{
				switch(m.messageType){
					case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:{
						msg_gps_raw_int msg = (msg_gps_raw_int) m;
						
						if( msg.fix_type > 1){
							GeoPoint point = new GeoPoint((int)(msg.lat/10), (int)(msg.lon/10));
							itemizedoverlay.setCopterOverlay(new OverlayItem(point, "UAV", "Current Location"));
							
							lastAltitude  = msg.alt;
							lastCopter = new GeoPoint((int)(msg.lat/10),((int)(msg.lon/10)));
							
							pathOverlay.addNewLocation( point, (int) msg.alt);
							
							mapView.postInvalidate();

							if(firstGPS){
								mapController.setCenter(point);
								firstGPS = false;
							}
							
							if(enableGCSOffsetMode){
								GeoPoint phone = phoneLocationOverlay.getMyLocation();
								
								msg_waypoint msg1 = new msg_waypoint();
								msg1.current = 2; // A guided packet
								msg1.x = ((float)phone.getLatitudeE6() + offsetX)/ 1000000.0f ;
								msg1.y = ((float)phone.getLongitudeE6()+ offsetY)/ 1000000.0f ;
								msg1.z = lastAltitude;
								//Toast.makeText(getApplicationContext(), " " + msg1.y, Toast.LENGTH_SHORT).show();
								guidedModeOverlay.setGotoPoint(new GeoPoint((int)(msg1.x * 1000000), (int)(msg1.y*1000000)));
								ba.sendBytesToComm(MAVLink.createMessage(msg1));
								
							}
						}
						
						break;
					}
				}		
			}
		}
	};
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;

	}

	public void setGuidedTarget(msg_waypoint msg, boolean setAltitude) {
		
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(50);
			
		
		if(enableLoiterSet){
			if( lastAltitude != 0){
				if( setAltitude )
					msg.z = lastAltitude;
				
				ba.sendBytesToComm(MAVLink.createMessage(msg));
				
				Toast toast=Toast.makeText(this, "Sent Guided Target", Toast.LENGTH_SHORT);
			    toast.setGravity(Gravity.TOP, 0, 50);
			    toast.show();
				
			}else{
				
				Toast toast=Toast.makeText(this, "No GPS Altitude.\nCan't set guided mode target altitude.\nWait for GPS lock.", Toast.LENGTH_SHORT);
			    toast.setGravity(Gravity.TOP, 0, 50);
			    toast.show();			
				
			}		
		}else{
			Toast toast=Toast.makeText(this, "Goto Mode Not Enabled", Toast.LENGTH_SHORT);
		    toast.setGravity(Gravity.TOP, 0, 50);
		    toast.show();			
		
			
		}
	}
}
