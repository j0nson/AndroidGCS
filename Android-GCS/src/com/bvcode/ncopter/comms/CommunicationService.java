package com.bvcode.ncopter.comms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_gps_raw;
import com.MAVLink.Messages.common.msg_gps_raw_int;
import com.MAVLink.Messages.common.msg_heartbeat;
import com.MAVLink.Messages.common.msg_param_request_list;
import com.MAVLink.Messages.common.msg_param_value;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.MAVLink.Messages.common.msg_statustext;
import com.MAVLink.Messages.common.msg_sys_status;
import com.MAVLink.Messages.common.msg_waypoint_current;
import com.MAVLink.Messages.common.msg_waypoint_reached;
import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.MainActivity;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.AC1Data.GPSData;
import com.bvcode.ncopter.AC1Data.ProtocolParser;
import com.bvcode.ncopter.AC1Data.RawByte;

/**
 * Communications module. Should be thread safe handles communications between
 * phone and quad parses the strings
 * 
 * @author bart
 * 
 */
public class CommunicationService extends Service implements OnInitListener {
	private NotificationManager mNM;
	
	private TextToSpeech tts;
	//private boolean connected = false;
	
	ReceiveThread receive = null;
	
	// Messenging
	ArrayList<Messenger> msgCenter = new ArrayList<Messenger>();
    final Messenger mMessenger = new Messenger(new IncomingHandler());
	
    // Constants
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    
    static final int MSG_DEVICE_CONNECTED = 11;
    static final int MSG_DEVICE_DISCONNECTED = 12;
    static final int MSG_CONNECT_DEVICE = 13;
    
    static final int MSG_COPTER_RECEIVED = 20;
    static final int MSG_COPTER_SEND = 21; 
   
    
    private boolean clearToSend = true;
    
	public String lastString = "";
	public int cmdRecvCount;
	public long lastBatteryWarning;
	
	public int nextWaypoint = 0;
	
	private FileWriter kml = null;
	private FileWriter tlog = null;
	
    ICommunicationModule module = null;
    
    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            	case MSG_COPTER_SEND:
            		Bundle b = msg.getData();
	            	byte[] s = b.getByteArray("msgBytes");
	            		            	
	            	if( CommonSettings.isProtocolAC1()){
	            		sendString(ProtocolParser.requestStopDataFlow());
	            	
		            	while( !clearToSend)
							try {
								Thread.sleep(1);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
	            	}
	            	sendString(s);
	            	break;
	            	
                case MSG_REGISTER_CLIENT:
                	synchronized (msgCenter) {
                    	if(!msgCenter.contains(msg.replyTo))
                    		msgCenter.add(msg.replyTo);
						
					}
                	// Intentional drop through when registering!
                case BluetoothModule.MSG_GET_STATUS:
                	module.serviceHandleStatusRequest(msg);
                    
                    break;
                    
                case MSG_UNREGISTER_CLIENT:
                	synchronized (msgCenter) {
                		msgCenter.remove(msg.replyTo);
                		
                	}
                	break;
                   
                case MSG_CONNECT_DEVICE:
                	if( msg.getData().containsKey(module.getDeviceAddressString())){
	                	String address = msg.getData().getString(module.getDeviceAddressString());
	                	if( address.length() != 0){
		                	connect(address);
		                	
	                	}
	    				
                	}
                	
                	break;
                	
                default:
                    super.handleMessage(msg);
                    
            }
        }
    }
	
    /**
     * Sends data out to the device.
     * @param s
     */
	public void sendString( byte[] s){
		try {
			if(module.isConnected() && s != null){
				if( CommonSettings.isProtocolAC1() )
					clearToSend = false;
				module.write(s);
				if( CommonSettings.isProtocolAC1() )
					lastString = new String(s);
				cmdRecvCount = 0;
			}else{
				notifyDeviceDisconnected();	
			}
		} catch (IOException e) {
			notifyDeviceDisconnected();
		}
			
	}
	/*
	private Handler mHandler = new Handler();
	Runnable r = new Runnable() {
		public void run() {
			//disconnect();
			CommonSettings.CONNECTED = false;
			nHandler.removeCallbacks(reconnect);
			nHandler.postDelayed(reconnect, 10000);
		}
	};
	*/
	private Handler nHandler = new Handler();
	Runnable reconnect = new Runnable(){
		public void run(){
			CommonSettings.CONNECTED = false;
			SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
			if ( settings.getBoolean(getString(R.string.speakConnectionStatus), true) ){
				tts.speak("No Connection", TextToSpeech.QUEUE_FLUSH, null);
			}
			//final Handler handler = new Handler();
			//handler.postDelayed(new Runnable() {
			//	public void run() {
			//		nHandler.removeCallbacks(reconnect);
			//		nHandler.postDelayed(reconnect, 10000);
			//	}
			//}, 10000);//ms
		}
	};
	private class ReceiveThread extends Thread{
		//InputStream in;
		public boolean running;
		private double lastLatitude;
		private double lastLongitude;
		private double lastAltiude;
		private int lastMode;
				
		public ReceiveThread() {
			super();
			running = true;
			//in = i;

		}

		@Override
		public void run() {
			int read = 0;
			byte b[] = new byte[1000];
			long lastRead = System.currentTimeMillis();
			
			boolean received_data;
			while(running){
				try {
					received_data = false;
					
					if(module.available() ){ // might lie to us...
						
						read += module.readByte(b, read, 1);
		    			
						if( read > 0){
							received_data = true;
			    			lastRead = System.currentTimeMillis();
			    			
			    			if( ProtocolParser.passThrough){
			    				// This mode for the CLI. Just pass through whatever we get
			    				RawByte raw = new RawByte();
			    				raw.b = b[0];
			    				read = 0;
			    				notifyNewMessage(0, raw);
			    				
			    			}else{
				    			if( CommonSettings.isProtocolAC1()){
					    			if( read == 1 && b[0] == 13)//eat this at the start??
					    				read = 0;
					    				
					    			if( read > 1 && b[read-1] == 10){
					    				if(read > 2 && b[read-2] == '\r')
					    					b[read-2] = '\0';
					    				
				    					b[read-1] = '\0';
				    					IMAVLinkMessage data = ProtocolParser.parseCopterData(lastString, new String(b,0, read));
				    					
				    					if( data != null)
				    						notifyNewMessage(cmdRecvCount, data);
					    				
				    					cmdRecvCount++;
				    				
					    				read = 0;
																	
									}
				    			}else if (CommonSettings.isProtocolMAVLink()){
				    				//Use the MAVLink protocol
				    				//Log.d("Receiving", "-");
				    				IMAVLinkMessage msg = MAVLink.receivedByte(b[0]);
				    				read = 0; //just use the first element in the buffer, AC1 legacy
				    				
				    				if( msg != null){
				    					switch(msg.messageType){
				    						case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:{
				    							CommonSettings.HEARTBEAT_TIME = System.currentTimeMillis();
				    							msg_heartbeat msg1 = (msg_heartbeat) msg;
				    							if (CommonSettings.CONNECTED == false){
				    								SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
				    								if ( settings.getBoolean(getString(R.string.speakConnectionStatus), true) ){
				    									tts.speak("Connected", TextToSpeech.QUEUE_FLUSH, null);
				    								}
				    								CommonSettings.uavType = msg1.type;
				    								//nHandler.removeCallbacks(reconnect);
				    								CommonSettings.CONNECTED = true;
				    							}
				    							//nHandler.removeCallbacks(reconnect);
				    							//mHandler.removeCallbacks(r);
				    							//mHandler.postDelayed(r, 5000);
				    							
				    							nHandler.removeCallbacks(reconnect);
				    							nHandler.postDelayed(reconnect, 5000);
				    							
				    							if ((int)msg1.custom_mode != lastMode){
				    								Resources res = getResources();
				    								String[] mavModes = null;
				    								if ( CommonSettings.uavType == 2){ //quad
				    									mavModes = res.getStringArray(R.array.mode_array_copter);
				    								}else { // if ( CommonSettings.uavType == 1){ //fixed wing
				    									mavModes = res.getStringArray(R.array.mode_array);
				    								}
				    								//Log.d("Debug CommService: 259", String.valueOf(msg1.custom_mode));
				    								
				    								if ( msg1.custom_mode <= mavModes.length ){
				    									SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
					    								if ( settings.getBoolean(getString(R.string.speakModeChange), true) ){
					    									tts.speak(mavModes[(int)msg1.custom_mode] + ".", TextToSpeech.QUEUE_FLUSH, null);
					    								}
				    									showNotification("Mode Change: " + mavModes[(int)msg1.custom_mode], 1);	
				    									lastMode = (int)msg1.custom_mode;
				    								}
				    							}
				    							break;
				    						}
				    						/*
				    						case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:{
				    							msg_param_value msg1 = (msg_param_value)msg;
				    							String name = MAVLink.convertIntNameToString(msg1.param_id);
				    							if (name.startsWith("SIMPLE")){
				    								String temp = Integer.toBinaryString((int)msg1.param_value);		    								
				    							    while (temp.length() < 6){
				    							    	temp = "0" + temp;
				    							    }
				    							    for (int i = 6; i > 0; i--){
				    							    	CommonSettings.simpleModeArray[i] = (Integer.valueOf(temp.substring((i-1), i)) != 0);
				    							    }
				    							}
				    							break;
				    						}
				    						*/
				    						case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:{
				    							msg_sys_status m = (msg_sys_status) msg;
				    							if ( m.voltage_battery < 9600 && ( System.currentTimeMillis() - lastBatteryWarning > 30000 )){
				    								SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
				    								if ( settings.getBoolean(getString(R.string.speakLowBattery), true) ){
				    									tts.speak("Low Battery", TextToSpeech.QUEUE_FLUSH, null);
				    								}
				    								lastBatteryWarning = System.currentTimeMillis();
				    							}
				    							break;
				    						}
				    						case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:{
				    							msg_statustext m = (msg_statustext) msg;
				    							String s = new String(m.text);
				    							s = s.replaceAll("[^A-Za-z0-9 ]", "");
				    							
				    							if (s.contains("Ready to FLY")){
				    								SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
				    								if ( settings.getBoolean(getString(R.string.speakLowBattery), true) ){
				    									tts.speak("Ready to fly", TextToSpeech.QUEUE_FLUSH, null);
				    								}
				    								showNotification("Ready to fly", 1);
				    							}
				    							//for(char test : m.text){
				    							//	Log.d("Received Status Text:", test);
				    							//}
			    								Log.d("Received Status Text:", s);
			    								break;
				    						}
				    						case msg_waypoint_current.MAVLINK_MSG_ID_WAYPOINT_CURRENT:{
				    							msg_waypoint_current m = (msg_waypoint_current) msg;
				    							if (m.seq != nextWaypoint){
				    								SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
				    								if ( settings.getBoolean(getString(R.string.speakWaypointNext), true) ){
				    									tts.speak("Next Waypoint, " + m.seq, TextToSpeech.QUEUE_FLUSH, null);
				    								}
				    								showNotification("Next Waypoint: " + m.seq, 1);
				    								nextWaypoint = m.seq;
				    							}
				    							break;
				    						}
				    						case msg_waypoint_reached.MAVLINK_MSG_ID_WAYPOINT_REACHED:{
				    							msg_waypoint_reached m = (msg_waypoint_reached) msg;
				    							SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
			    								if ( settings.getBoolean(getString(R.string.speakWaypointReached), true) ){
			    									tts.speak("Waypoint Reached, " + m.seq, TextToSpeech.QUEUE_FLUSH, null);
			    								}
				    							showNotification("Waypoint Reached: " + m.seq, 1);
				    							break;
				    						}
				    					}
				    					//////////////////////////////////////////////////////////////////debug				    					
				    					//Log.d("Received:", "" + msg.messageType + ": " + msg.getClass().getName());
				    					notifyNewMessage(cmdRecvCount, msg);
				    				}
					    						
				    			}	
			    			}
						}							
		    		}

					if(!received_data){
		    			if( System.currentTimeMillis() - lastRead > 100 ){
		    				if( CommonSettings.isProtocolAC1())
		    					clearToSend = true;
		    				
		    			}		    			
		    		}
						
				} catch (IOException e) {
					disconnect();
				}				
			}
			running = false;
			
		}
		
		private void notifyNewMessage(int cmdRecvCount, IMAVLinkMessage s) {
			
			try {
				
				synchronized (msgCenter) {
					for( Messenger c: msgCenter){
				    	if( c == null)
				    		continue;
						Message msg = Message.obtain(null, CommunicationService.MSG_COPTER_RECEIVED);
						Bundle data = new Bundle();
						data.putInt("recvCount", cmdRecvCount);
						data.putSerializable("msg", s);
						msg.setData(data);			    	
				    	c.send(msg);
				    }
			    }
			} catch (RemoteException e) {
				e.printStackTrace();
			}	
			//tlog
			//if (CommonSettings.tlog){
			//	saveRaw(s);
			//}
			//save KML
			if (CommonSettings.kmlLog){
				saveGPS(s);
			}
		}
		private void saveRaw(IMAVLinkMessage s){
			//if ( byte b ){
				try {
					tlog.write( System.currentTimeMillis() + ": " + s + "\n");
					//tlog.write( "" + s );
				} catch (IOException e) { 
					e.printStackTrace();
				}
			//}
		}
		private void saveGPS(IMAVLinkMessage s) {
			if( s.messageType == msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT){	
				msg_gps_raw_int msg = (msg_gps_raw_int) s;
				if( msg.fix_type > 1){
					if (kml == null){
						kmlLogSetup();
					}
					
					//How much have we moved
					float[] results = new float[1];
					Location.distanceBetween( // in meters
							lastLatitude, lastLongitude,
							(msg.lat/10000000.0), (msg.lon/10000000.0),
							results);
						
					// get the overall length, including altitude motion.
					results[0] = (float) Math.sqrt( results[0]*results[0] + Math.pow((msg.alt/1000) - lastAltiude, 2));
						        
					// Ensure we have moved more than 3m.
					if(kml != null && (results[0] > 3)){
						// Store the values
						lastLatitude = (msg.lat/10000000.0);
						lastLongitude = (msg.lon/10000000.0);
						lastAltiude = (msg.alt/1000);
							
						try {
							kml.write( lastLongitude + "," + lastLatitude + "," + lastAltiude + " \n");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	};
	
	
	private void notifyDeviceConnected() {
		try {
        	synchronized (msgCenter) {
				for (Messenger c: msgCenter)
					c.send(Message.obtain(null, CommunicationService.MSG_DEVICE_CONNECTED));
        	}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void notifyDeviceDisconnected() {
		try {
        	synchronized (msgCenter) {
				for (Messenger c: msgCenter)
					c.send(Message.obtain(null, CommunicationService.MSG_DEVICE_DISCONNECTED));
        	}
		} catch (RemoteException e) {
			e.printStackTrace();
		}	
	}
		
	@Override
	public IBinder onBind(Intent intent) {				
		return mMessenger.getBinder();
	}

    @Override
    public void onCreate() {	
    	// tts
    	tts = new TextToSpeech(this, this);
    	
    	// which parser to use?
		SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
		
		//Load in the proper comm module.
		if(settings.contains(CommunicationClient.LINK_TYPE)){
	    	String link = settings.getString(CommunicationClient.LINK_TYPE, "");
	    	
	    	if(link.equals(CommonSettings.LINK_BLUE)){
	        	module = new BluetoothModule();
	        	CommonSettings.currentLink = CommonSettings.LINK_BLUETOOTH;
	        	
	    	//}else if(link.equals(ProtocolParser.LINK_USB_) && IProxy.isUSBSupported()){
	    	//	module = new USBModule(this);
	    	//	ProtocolParser.currentLink = ProtocolParser.LINK_USB;
	        	
	    	}else{
	    		
	    		
	    	}
		}else{
    		Log.v("Communication Service", "Defaulting to Bluetooth");
    		module = new BluetoothModule();
    		
    	}		
    	    
		if(settings.contains(CommunicationClient.DEFAULT_ORIENTATION)){
			String address = settings.getString(CommunicationClient.DEFAULT_ORIENTATION, "");
			
	    	if(address.equals(CommonSettings.LANDSCAPE)){
	    		CommonSettings.desiredOrientation = CommonSettings.ORIENTATION_LANDSCAPE;
	    		
	    	}else if(address.equals(CommonSettings.PORTRAIT)){
	    		CommonSettings.desiredOrientation = CommonSettings.ORIENTATION_PORTRAIT;
	    		
	    	}else
	    		CommonSettings.desiredOrientation = CommonSettings.ORIENTATION_DEFAULT;
	    	
			
		}
		
		if(settings.contains(CommunicationClient.ACTIVE_PROTOCOL)){
	    	String address = settings.getString(CommunicationClient.ACTIVE_PROTOCOL, "");
		
	    	if(address.equals(CommonSettings.AC1)){
	    		CommonSettings.currentProtocol = CommonSettings.AC1_PROTOCOL;
	    		
	    	}else if(address.equals(CommonSettings.MAVLink)){
	    		CommonSettings.currentProtocol = CommonSettings.MAVLINK_PROTOCOL;
	    	}

		}
		
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification("Service Activated", 1);
        
        //-------------------------------------
        // Ensure that when the service starts we allow the client to reconnect
        // After a first request, don't bother the user.
        module.setFirstTimeEnableDevice(true);
        module.setFirstTimeListDevices(true);  
    }
    //@Override
    public void onDestroy() {
    	nHandler.removeCallbacks(reconnect);
    	//shut down tts
    	if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    	
        // Cancel the persistent notification.
        mNM.cancel(R.string.local_service_started);
        mNM.cancelAll();

		disconnect();
		
		if(kml != null){
			try {			
				kml.write(	"</coordinates>\n" +
							"</LineString>\n" +
							"</Placemark>\n" + 
							"</Document>\n" +
							"</kml>\n");
				kml.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(tlog != null){
			try {			
				tlog.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//final Resources res = getResources();
		//File sdCard = Environment.getExternalStorageDirectory();
		//File dir = new File(sdCard.getAbsolutePath() + res.getString(R.string.LogPath));
		//for ( File  child : dir.listFiles()){
		//	if (child.length() < 425){
			//if (child.length() == 0){
		        //Toast.makeText(getApplicationContext(), "Deleting: " + child.getName(), Toast.LENGTH_LONG).show();
				//child.delete();
		//	}
			//break;
		//}
	}

    public void connect(final String device) {
    	disconnect();
		
		new Thread(){
			public void run(){
				try {
					
					if(receive != null)
						receive.running = false;
					receive = null;

					module.connect(device);
				
					// Start a receive thread.
					receive = new ReceiveThread();
					receive.start();
					
					notifyDeviceConnected();
					
					showNotification("Connected to Bridge", 1);
					
				} catch (IOException e) {
					module.setSocketNull();

					if(receive != null)
						receive.running = false;
					Log.v("Connect Error", e.toString());
					
					notifyDeviceDisconnected();
					
					showNotification("Disconnected from Bridge", 1);			
				}		
			}		
		}.start();

	}
    
    public void disconnect(){
    	showNotification("Disconnected from UAV", 1);	
    	try {

    		if(receive!=null)
    			receive.running = false;
    		receive = null;
    		        
	        module.disconnect();
			
			if( receive != null)
				receive.running = false;
	        receive = null;
	        
	        mNM.cancel(R.string.local_service_started);
	        mNM.cancelAll();
	        
			notifyDeviceDisconnected();

		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(CharSequence text, int type) {
    	
    	// Set the icon, scrolling text and timestamp
    	Notification notification = new Notification(R.drawable.level_list, text, System.currentTimeMillis());

        if (type == 1){
    		// The PendingIntent to launch our activity if the user selects this notification
    		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

    		// Set the info for the views that show in the notification panel.
    		notification.setLatestEventInfo(this, getText(R.string.local_service_label), text, contentIntent);

        	//vibrate
        	//long[] vibrate = {0,150};
        	//notification.vibrate = vibrate;
        	
        	//sound
        	notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pop);
        	notification.flags |= Notification.FLAG_ONGOING_EVENT;
        	
        	// Send the notification
            mNM.notify(R.string.local_service_started, notification);
           
        }else if (type == 2){
        	Toast.makeText(getApplicationContext(), "Service Activated", Toast.LENGTH_LONG).show();
        }else if (type == 3){
        	Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }else if (type == 4){
        	//Log.d("notification level1:", "" + notification.iconLevel);
        	if ( notification.iconLevel == 0){
        		notification.iconLevel = 1;
        	}else{
        		notification.iconLevel = 0;
        	}
        	//Log.d("notification level2:", "" + notification.iconLevel);
        }
    }
    public void kmlLogSetup(){
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	final Resources res = getResources();
		    // Record the data
		    File sdCard = Environment.getExternalStorageDirectory();
		    
		    // Just make sure our subdirectory exists
		    File dir = new File(sdCard.getAbsolutePath() + res.getString(R.string.LogPath));
		    if(! dir.exists())
		    	dir.mkdir();
		    
		    // And open for writing
		    final Calendar c = Calendar.getInstance();
		    int mYear = c.get(Calendar.YEAR);
		    int mMonth = c.get(Calendar.MONTH);
		    int mDay = c.get(Calendar.DAY_OF_MONTH);
		    int mMinute = c.get(Calendar.MINUTE);
		    int mHour = c.get(Calendar.HOUR_OF_DAY);
		    int mSecond = c.get(Calendar.SECOND);
		    //System.currentTimeMillis()
		    try {
		    	File file = new File(sdCard.getAbsolutePath() + res.getString(R.string.LogPath) + "/Log_" + mYear + "-" + mMonth + "-" + mDay + "_" + mHour + mMinute + mSecond + ".kml");
		    	kml = new FileWriter(file);
		    	kml.write("<?xml version='1.0' encoding='UTF-8'?>\n"
		    				+ "<kml xmlns='http://www.opengis.net/kml/2.2'>\n"
		    				+ "<Document>\n"
		    				+ "<Style id=\"arduplane\">\n"
		    				+ "<LineStyle>\n"
		    				+ "<width>5</width>\n"
		    				+ "<color>7dff0000</color>\n"
		    				+ "</LineStyle>\n"
		    				+ "<PolyStyle>\n"
		    				+ "<color>7f00ff00</color>\n"
		    				+ "</PolyStyle>\n"
		    				+ "</Style>\n"
		    				+ "<Placemark>\n"
		    				+ "<name>"  + mYear + "-" + mMonth + "-" + mDay + "_" + mHour + mMinute + "</name>\n"
		    				+ "<styleUrl>#arduplane</styleUrl>\n"
		    				+ "<LineString>\n"
		    				+ "<tessellate>1</tessellate>\n"
		    				+ "<extrude>1</extrude>\n"
		    				+ "<altitudeMode>absolute</altitudeMode>\n"
		    				+ "<coordinates>\n");
			} catch (IOException e) {
				kml = null;
			}
	    }	
    }
    public void tlogSetup(){
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	final Resources res = getResources();
		    // Record the data
		    File sdCard = Environment.getExternalStorageDirectory();
		    
		    // Just make sure our subdirectory exists
		    File dir = new File(sdCard.getAbsolutePath() + res.getString(R.string.LogPath));
		    if(! dir.exists())
		    	dir.mkdir();
		    
		    // And open for writing
		    final Calendar c = Calendar.getInstance();
		    int mYear = c.get(Calendar.YEAR);
		    int mMonth = c.get(Calendar.MONTH);
		    int mDay = c.get(Calendar.DAY_OF_MONTH);
		    int mMinute = c.get(Calendar.MINUTE);
		    int mHour = c.get(Calendar.HOUR_OF_DAY);
		    int mSecond = c.get(Calendar.SECOND);
		    //System.currentTimeMillis()
		    try {
		    	File file = new File(sdCard.getAbsolutePath() + res.getString(R.string.LogPath) + "/" + mYear + "-" + mMonth + "-" + mDay + " " + mHour + "-" + mMinute + "-" + mSecond + ".tlog");
		    	tlog = new FileWriter(file);
			} catch (IOException e) {
				tlog = null;
			}
	    }	
    }
    public void onInit(int status) {
    	if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_LONG).show();
                Log.e("TTS", "Language is not supported");
            }
        } else {
            Toast.makeText(this, "TTS Initilization Failed", Toast.LENGTH_LONG).show();
            Log.e("TTS", "Initilization Failed");
        }
    }
    /*
    public void sendHeartbeat() {
    	msg_heartbeat msg1 = new msg_heartbeat();
    	msg1.type = MAVLink.MAV_TYPE.MAV_TYPE_GCS;
    	msg1.autopilot = MAVLink.MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC;
    	ba.sendBytesToComm(MAVLink.createMessage(msg1));
    }
    */
}
