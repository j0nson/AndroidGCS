package com.bvcode.ncopter;

import java.util.HashMap;

import android.app.Activity;
import android.content.pm.ActivityInfo;

public class CommonSettings {
	
	//logging
	public static boolean kmlLog = false;
	public static boolean tlog = false;
	
	// auv Info
	public static int uavType = 1;
	public static boolean[] simpleModeArray = new boolean[20];
	//protected static final boolean[] simpleModeArray = null;
	
	// Protocol Stuff
	public static final int NO_PROTOCOL = 0;
	
	public static final int AC1_PROTOCOL = 1;
	public static final String AC1 = "AC1Protocol";
	
	public static final int MAVLINK_PROTOCOL = 2;	
	public static final String MAVLink = "MAVLink";
	
	
	// Link stuff
	public static final int LINK_NONE = 0;
	
	public static final int LINK_BLUETOOTH= 1;
	public static final String LINK_BLUE= "Bluetooth";
	
	public static final int LINK_USB= 2;
	public static final String LINK_USB_= "USB";
	
	public static boolean CONNECTED = false;
	public static long HEARTBEAT_TIME = 0;
	
	// Orientation settings
	public static final String DEFAULT = "none";
	public static final int ORIENTATION_DEFAULT = 0;
	public static final String PORTRAIT = "PORTRAIT";
	public static final int ORIENTATION_PORTRAIT = 1;
	public static final String LANDSCAPE = "LANDSCAPE";
	public static final int ORIENTATION_LANDSCAPE = 2;
	
	public static int desiredOrientation = ORIENTATION_DEFAULT;
	public static int currentProtocol = MAVLINK_PROTOCOL; //NO_PROTOCOL;
	public static int currentLink = LINK_BLUETOOTH; //NO_PROTOCOL;
	
	public static boolean setOrientation(Activity act, int orientation){
		
		if(orientation == -1)
			orientation = desiredOrientation;

		if(orientation == ORIENTATION_DEFAULT)
			return false;
		
		if(act.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){			
			if(orientation == ORIENTATION_LANDSCAPE){
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				return true;
			}
		}
			
		if (act.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){			
			if(orientation == ORIENTATION_PORTRAIT){
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				return true;
			}
    	}
		
		return false;
		
	}
	
	public static int getCurrentOrientation(){
		return desiredOrientation;
		
	}
	
	public static boolean isProtocolMAVLink() {
		return currentProtocol == MAVLINK_PROTOCOL;
		
	}

	public static boolean isProtocolAC1() {
		return currentProtocol == AC1_PROTOCOL;
		
	}

	public static boolean isNone() {
		return currentProtocol == NO_PROTOCOL;
	
	}
}
