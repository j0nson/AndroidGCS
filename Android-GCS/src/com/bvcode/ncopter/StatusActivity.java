package com.bvcode.ncopter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_heartbeat;
import com.MAVLink.Messages.common.msg_gps_raw;
import com.MAVLink.Messages.common.msg_gps_raw_int;
import com.MAVLink.Messages.common.msg_gps_status;
import com.MAVLink.Messages.common.msg_nav_controller_output;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.MAVLink.Messages.common.msg_sys_status;
import com.MAVLink.Messages.common.msg_waypoint_current;
import com.bvcode.ncopter.AC1Data.ProtocolParser;
import com.bvcode.ncopter.AC1Data.StringMessage;
import com.bvcode.ncopter.comms.CommunicationClient;

public class StatusActivity extends Activity implements OnLongClickListener{

	TextView[] lines;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_main);
		if( CommonSettings.setOrientation(this, -1))
			return;
		
		lines = new TextView[18];
		lines[0] = (TextView) findViewById(R.id.line1);
		lines[1] = (TextView) findViewById(R.id.line2);
		lines[2] = (TextView) findViewById(R.id.line3);
		lines[3] = (TextView) findViewById(R.id.line4);
		lines[4] = (TextView) findViewById(R.id.line5);
		lines[5] = (TextView) findViewById(R.id.line6);
		lines[6] = (TextView) findViewById(R.id.line7);
		lines[7] = (TextView) findViewById(R.id.line8);
		lines[8] = (TextView) findViewById(R.id.line9);
		lines[9] = (TextView) findViewById(R.id.line10);
		lines[10] = (TextView) findViewById(R.id.line11);
		lines[11] = (TextView) findViewById(R.id.line12);
		lines[12] = (TextView) findViewById(R.id.line13);
		lines[13] = (TextView) findViewById(R.id.line14);
		lines[14] = (TextView) findViewById(R.id.line15);
		lines[15] = (TextView) findViewById(R.id.line16);
		lines[16] = (TextView) findViewById(R.id.line17);
		lines[17] = (TextView) findViewById(R.id.line18);
		
		for(int i = 0; i < 17; i++)
			lines[i].setOnLongClickListener(this);
		
		ba.init();
	}
			
	
	private void updateStatusLine(int i, String s){
		//Show the GPS info.
		if( s.startsWith("gps:") == true && i+6 < lines.length){
			String arr2[] = s.split(" ");
			if(arr2.length == 9){
				lines[i+1].setText(arr2[1]); //long
				lines[i+2].setText( Integer.toString(Integer.parseInt(arr2[2]) / 10000000) ); //lat
				lines[i+3].setText( Double.toString(Integer.parseInt(arr2[3]) / 3.28084 ) ); //alt
				lines[i+4].setText("Ground Speed: " + arr2[4]); //Ground Speed
				lines[i+5].setText(arr2[6] + " " + arr2[7]);
				lines[i+6].setText(arr2[8]);
			}
			
		}else{
			if( i < lines.length){
				lines[i].setText(s.trim());
			}			
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
				sendBytesToComm(ProtocolParser.requestStatus());
			
			else if(CommonSettings.isProtocolMAVLink()){
				msg_request_data_stream req = new msg_request_data_stream();
				req.req_message_rate = 1;
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
			
			if( CommonSettings.isProtocolAC1()){
				if( StringMessage.class.isInstance(m)){
					StringMessage sm = (StringMessage)m;
					updateStatusLine(count, sm.message);
				
				}
				
			}else if(CommonSettings.isProtocolMAVLink()){
				Log.v("Status Activity:", m.getClass().getName());
				
				switch(m.messageType){
					case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:{
						msg_sys_status msg = (msg_sys_status) m;
						
						//TODO		
						//updateStatusLine(0, "NAV Mode: " + MAVLink.getNav(msg.nav_mode));
						//updateStatusLine(1, "Status: " + MAVLink.getState(msg.status));
						//if( msg.mode < 100)
						//	updateStatusLine(2, "Mode: " + MAVLink.getMode(msg.mode));
						//else
						//	updateStatusLine(2, "Mode: " + getAC2Mode(msg.mode));
						
						updateStatusLine(9, "Packet Drops: " + (float)(msg.drop_rate_comm)/1000.0);
						break;
						
					}
					case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:{
						msg_heartbeat msg = (msg_heartbeat) m;
						
						switch((int)msg.custom_mode){
						case 0:
							updateStatusLine(0, "Mode: " + "Manual");
							break;
						case 1:
							updateStatusLine(0, "Mode: " + "Circle");					
							break;
						case 2:
							updateStatusLine(0, "Mode: " + "Stabilize");					
							break;
						case 3:
							updateStatusLine(0, "Mode: " + "Training");					
							break;
						case 5:
							updateStatusLine(0, "Mode: " + "FBW-A");					
							break;
						case 6:
							updateStatusLine(0, "Mode: " + "FBW-B");					
							break;
						case 10:
							updateStatusLine(0, "Mode: " + "Auto");					
							break;
						case 11:
							updateStatusLine(0, "Mode: " + "Return to Launch");					
							break;
						case 12:
							updateStatusLine(0, "Mode: " + "Loiter");					
							break;
						case 15:
							updateStatusLine(0, "Mode: " + "Guided");					
							break;
						default:
							updateStatusLine(0, "Mode: " + msg.custom_mode);					
							break;
						
						}
						//Navigation mode 0: manual, 2: stabilize, 5: FBW, 11: return to launch, 10: auto
						//0	 Manual,1	 CIRCLE,2	 STABILIZE,3	 TRAINING,5	 FBWA,6	 FBWB,10	 Auto,11	 RTL,12	 Loiter,15	 Guided -->
						//MAV_MODE_PREFLIGHT=0; //, * System is not ready to fly, booting, calibrating, etc. No flag is set. | *
						//MAV_MODE_MANUAL_DISARMED=64; //, * System is allowed to be active, under manual (RC) control, no stabilization | *
						//MAV_MODE_TEST_DISARMED=66; //, * UNDEFINED mode. This solely depends on the autopilot - use with caution, intended for developers only. | *
						//MAV_MODE_STABILIZE_DISARMED=80; //, * System is allowed to be active, under assisted RC control. | *
						//MAV_MODE_GUIDED_DISARMED=88; //, * System is allowed to be active, under autonomous control, manual setpoint | *
						//MAV_MODE_AUTO_DISARMED=92; //, * System is allowed to be active, under autonomous control and navigation (the trajectory is decided onboard and not pre-programmed by MISSIONs) | *
						//MAV_MODE_MANUAL_ARMED=192; //, * System is allowed to be active, under manual (RC) control, no stabilization | *
						//MAV_MODE_TEST_ARMED=194; //, * UNDEFINED mode. This solely depends on the autopilot - use with caution, intended for developers only. | *
						//MAV_MODE_STABILIZE_ARMED=208; //, * System is allowed to be active, under assisted RC control. | *
						//MAV_MODE_GUIDED_ARMED=216; //, * System is allowed to be active, under autonomous control, manual setpoint | *
						//MAV_MODE_AUTO_ARMED=220; //, * System is allowed to be active, under autonomous control and navigation (the trajectory is decided onboard and not pre-programmed by MISSIONs) | *
						
						//updateStatusLine(0, "Nav Mode: " + MAVLink.getState(msg.base_mode));
						break;
					}
					case msg_gps_status.MAVLINK_MSG_ID_GPS_STATUS:{
						//msg_gps_status msg = (msg_gps_status) m;
						//updateStatusLine(1, "GPS Sats Vis: " + MAVLink.getState(msg.satellites_visible));
						//msg_gps_status msg = (msg_gps_status) m;
						
						break;
					}
					case msg_waypoint_current.MAVLINK_MSG_ID_WAYPOINT_CURRENT:{
						msg_waypoint_current msg = (msg_waypoint_current) m;
						updateStatusLine(1, "Current WP: " + msg.seq);
						break;
					}
					case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:{
						msg_gps_raw_int msg = (msg_gps_raw_int) m;
						updateStatusLine(3, "Sats Vis: " + msg.satellites_visible);
						switch(msg.fix_type){
							case 0:
							case 1:
								updateStatusLine(4, "Fix Type: no fix");					
								break;
							case 2:
								updateStatusLine(4, "Fix Type: 2D fix");					
								break;
							case 3:
								updateStatusLine(4, "Fix Type: 3D fix");					
								break;
						}
						updateStatusLine(5, "Latitude: " + (float)(msg.lat)/10000000.0);
						updateStatusLine(6, "Longitude: " + (float)(msg.lon)/10000000.0);
						updateStatusLine(7, "Altitude: " + (int)((msg.alt)/328.084));
						//updateStatusLine(8, "GPS Heading: " + msg.hdg);
						break;
					}
					case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:{
						msg_nav_controller_output msg = (msg_nav_controller_output) m;
						updateStatusLine(8, "Nav Bearing: " + msg.nav_bearing);
						break;
					}
					
				}				
			}			
		}

		
		
		private String getAC2Mode(int mode) {
			String modes[] = new String[]{"STABILIZE", "ACRO", "ALT_HOLD", "AUTO", "GUIDED", "LOITER", "RTL", "CIRCLE", "POSITION"};
	        //String modes[] = new String[]{"STABILIZE", "ACRO", "SIMPLE", "ALT_HOLD", "AUTO", "GUIDED", "LOITER", "RTL", "CIRCLE"};
			if( mode-100 < modes.length)
				return modes[mode-100];
			return "";
		}
	};

	
	public boolean onLongClick(View v) {
		if(CommonSettings.isProtocolAC1())	
			ba.sendBytesToComm(ProtocolParser.requestStatus());
		
		else if(CommonSettings.isProtocolMAVLink()){
			
			msg_request_data_stream req = new msg_request_data_stream();
			req.req_message_rate = 1;
			req.req_stream_id = MAVLink.MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS;
			req.start_stop = 1;
			req.target_system = MAVLink.CURRENT_SYSID;
			req.target_component = 0;
			ba.sendBytesToComm( MAVLink.createMessage(req));
			
		}
		return true;
	}
}
