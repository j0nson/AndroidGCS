package com.bvcode.ncopter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.WindowManager;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_attitude;
import com.MAVLink.Messages.common.msg_gps_raw;
import com.MAVLink.Messages.common.msg_gps_raw_int;
import com.MAVLink.Messages.common.msg_gps_status;
import com.MAVLink.Messages.common.msg_heartbeat;
import com.MAVLink.Messages.common.msg_nav_controller_output;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.MAVLink.Messages.common.msg_sys_status;
import com.MAVLink.Messages.common.msg_waypoint_current;
import com.MAVLink.Messages.common.msg_waypoint_reached;
import com.bvcode.ncopter.AC1Data.FlightData;
import com.bvcode.ncopter.AC1Data.ProtocolParser;
import com.bvcode.ncopter.comms.CommunicationClient;
import com.bvcode.ncopter.widgets.HUD;

public class HUDActivity extends Activity{
	HUD hud;
	private int lastMode = 100;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if( CommonSettings.setOrientation(this, -1))
			return;
		//set audio stream controls
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
				
		setContentView(R.layout.hud);

		hud = (HUD) findViewById(R.id.hudWidget);
		
        ba.init();
        
        SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
      	if ( settings.getBoolean(getString(R.string.keepScreenOn), true) ){
      		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
			if( CommonSettings.isProtocolAC1())
				sendBytesToComm(ProtocolParser.requestFlightData());
			else if (CommonSettings.isProtocolMAVLink()){
				
				msg_request_data_stream req = new msg_request_data_stream();
				req.req_message_rate = 20;
				req.req_stream_id = MAVLink.MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1; // MAV_DATA_STREAM_RAW_CONTROLLER;
																					// MAV_DATA_STREAM_RAW_SENSORS;
				req.start_stop = 1;
				req.target_system = MAVLink.CURRENT_SYSID;
				req.target_component = 0;
				ba.sendBytesToComm( MAVLink.createMessage(req));
				

				req = new msg_request_data_stream();
				req.req_message_rate = 1;
				req.req_stream_id = MAVLink.MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS; // MAV_DATA_STREAM_RAW_CONTROLLER;
																					// MAV_DATA_STREAM_RAW_SENSORS;
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
			if( CommonSettings.isProtocolAC1()){
				if( FlightData.class.isInstance(m)){
					FlightData fd = (FlightData)m;
//					double g[] = {fd.gyroX, fd.gyroY, fd.gyroYaw};
					double a[] = {fd.accelX, fd.accelY, fd.accelZ};
//					double mag[] = {fd.magX, fd.magY, fd.magZ};

					float roll = (float)-Math.atan2(a[0], a[2]), 
						 pitch = (float)-Math.atan2(a[1], a[2]), 
						 yaw=0;
					
					hud.newFlightData(roll, pitch, yaw);
					hud.setBatteryRemaining(0);
					hud.setbatteryMVolt(0);
					hud.setNavMode("");
					
				}
				
			}else if (CommonSettings.isProtocolMAVLink()){
				switch(m.messageType){
					case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:{
						//Resources res = getResources();
						//String[] mavModes = res.getStringArray(R.array.mode_array);
						msg_heartbeat msg = (msg_heartbeat) m;
						if ((int)msg.custom_mode != lastMode){
							Resources res = getResources();
							String[] mavModes = null;
							if ( CommonSettings.uavType == 2){ //quad
								mavModes = res.getStringArray(R.array.mode_array_copter);
							}else { // if ( CommonSettings.uavType == 1){ //fixed wing
								mavModes = res.getStringArray(R.array.mode_array);
							}
							String mode = mavModes[(int)msg.custom_mode];
							hud.setNavMode(mode);
							lastMode = (int)msg.custom_mode;
						}
						break;
					}
					case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:{
						msg_attitude msg = (msg_attitude) m;
						hud.newFlightData(msg.roll, msg.pitch, msg.yaw);
						break;
					}
					case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:{
						msg_sys_status msg = (msg_sys_status) m;
						hud.setBatteryRemaining(msg.battery_remaining);
						hud.setbatteryMVolt(msg.voltage_battery);
						break;
					}	
					case msg_waypoint_current.MAVLINK_MSG_ID_WAYPOINT_CURRENT:{
						msg_waypoint_current msg = (msg_waypoint_current) m;
						hud.setCurrentWP(msg.seq);
						break;
					}
					case msg_waypoint_reached.MAVLINK_MSG_ID_WAYPOINT_REACHED:{
						msg_waypoint_reached msg = (msg_waypoint_reached) m;
						hud.setReachedWP(msg.seq);
						break;
					}
					
					case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:{
						msg_gps_raw_int msg = (msg_gps_raw_int) m;
						hud.setAltitude((int)(msg.alt/1000));
						String s;
						switch(msg.fix_type){
							case 0:
								s="No Fix";
	                            break;
							case 1:
	                            s="No Fix";
	                            break;
	                        case 2:
	                        	s="2D Fix";
	                            break;

	                        case 3:
	                        	s="3D Fix";
	                            break;
	                        default:
	                            s="No GPS";
						}
						hud.setGPSFix(s);
						break;
					}
					case msg_gps_status.MAVLINK_MSG_ID_GPS_STATUS:{
						break;
					}
					case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:{
						break;
					}					
				}				
			}
		}
	};
}
