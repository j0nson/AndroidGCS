package com.bvcode.ncopter;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_heartbeat;
import com.MAVLink.Messages.common.msg_param_request_list;
import com.MAVLink.Messages.common.msg_param_set;
import com.MAVLink.Messages.common.msg_param_value;
import com.MAVLink.Messages.common.msg_rc_channels_raw;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.bvcode.ncopter.comms.CommunicationClient;

public class ModeSelectionActivity extends Activity{

	Spinner spinners[] = new Spinner[6];
	TextView text[] = new TextView[6];
	private TextView ind;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if( CommonSettings.setOrientation(this, -1))
			return;

		setContentView(R.layout.mode_selection); 
	
		setupSpinner(0, R.id.mode1);
		setupSpinner(1, R.id.mode2);
		setupSpinner(2, R.id.mode3);
		setupSpinner(3, R.id.mode4);
		setupSpinner(4, R.id.mode5);
		setupSpinner(5, R.id.mode6);
		
		text[0] = (TextView)findViewById(R.id.text1);
		text[1] = (TextView)findViewById(R.id.text2);
		text[2] = (TextView)findViewById(R.id.text3);
		text[3] = (TextView)findViewById(R.id.text4);
		text[4] = (TextView)findViewById(R.id.text5);
		text[5] = (TextView)findViewById(R.id.text6);
	
		
		ind = (TextView)findViewById(R.id.modeIndicator);
		
		ba.init();

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ba.onDestroy();
			
	}
	
	CommunicationClient ba = new CommunicationClient(this) {

		int count = 0;
		
		@Override
		public void notifyConnected() {
			count = 0;
			
			msg_param_request_list req = new msg_param_request_list();
			req.target_system = MAVLink.CURRENT_SYSID;
			req.target_component = 0;
			ba.sendBytesToComm( MAVLink.createMessage(req));
			
			msg_request_data_stream req1 = new msg_request_data_stream();
			req1.req_message_rate = 5;
			req1.req_stream_id = MAVLink.MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS;
			req1.start_stop = 1;
			req1.target_system = MAVLink.CURRENT_SYSID;
			req1.target_component = 0;
			ba.sendBytesToComm( MAVLink.createMessage(req1));
			
		}

		@Override
		public void notifyDisconnected() {
			
		}

		@Override
		public void notifyDeviceNotAvailable() {
			
		}

		@Override
		public void notifyReceivedData(int count_, IMAVLinkMessage m) {
		
			if (CommonSettings.isProtocolMAVLink()){
				switch(m.messageType){
					case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:{
						msg_param_value msg = (msg_param_value)m;
	
						String name = MAVLink.convertIntNameToString(msg.param_id);
						if( name.startsWith("FLTMODE") && ! name.endsWith("_CH")){
							Log.v("Mode Select Activity - Name:", name);
							int index = Integer.parseInt(name.replace("FLTMODE", ""))-1;
							Log.v("Mode Select Activity - Mode Number:", String.valueOf(index));
							Log.v("Mode Select Activity - Value:", String.valueOf((int) msg.param_value));
							spinners[index].setSelection( ( (int) msg.param_value ) );
							count++;
						}

						ind.setText(count + " Loaded"); 
									
						break;
					}
					case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:{
						msg_heartbeat msg = (msg_heartbeat) m;
						int mode = (int)msg.custom_mode;
						for( int i = 0; i < 6; i++){
							if( mode == i)
								text[i].setBackgroundColor( Color.YELLOW );
							else
								text[i].setBackgroundColor( Color.BLACK );	
						}
						break;
					}
					/*
					case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:{
						msg_rc_channels_raw msg = (msg_rc_channels_raw) m;

						int pulsewidth = msg.chan8_raw;
						int mode = 0;

						//from control_modes.pde
					 	if (pulsewidth > 1230 && pulsewidth <= 1360)    	 mode = 1;
					 	else if (pulsewidth > 1360 && pulsewidth <= 1490)    mode = 2;
					 	else if (pulsewidth > 1490 && pulsewidth <= 1620)    mode = 3;
					 	else if (pulsewidth > 1620 && pulsewidth <= 1749)    mode = 4;
					 	else if (pulsewidth >= 1750)                         mode = 5;       
					 	else 												 mode = 0;		// Hardware Manual
						
						for( int i = 0; i < 6; i++){
							if( mode == i)
								text[i].setBackgroundColor( Color.YELLOW );
							else
								text[i].setBackgroundColor( Color.BLACK );
							
						}
						break;
					}
					*/
				}		
			}			
		}		
	};
		
	private void setupSpinner(int num, int mode) {
		spinners[num] = (Spinner) findViewById(mode);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.mode_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinners[num].setAdapter(adapter);	    
	    spinners[num].setOnItemSelectedListener(new clickListen(num));
		
	}

	
	class clickListen implements OnItemSelectedListener{

		int myNum;
		boolean first = true;
		
		public clickListen(int num) {
			myNum = num;
			
		}

		public void onItemSelected(AdapterView<?> parent,View view, int pos, long id) {

			if( first ){
				first = false;
				return;
			}
			
			String valueName = "FLTMODE"+(myNum+1);
			
			char[] name = MAVLink.StringNameToInt(valueName); 
			
			msg_param_set set = new msg_param_set();
			set.target_system = MAVLink.CURRENT_SYSID;
			set.target_component = 0;
			set.param_id = name;
			set.param_value = pos;
			ba.sendBytesToComm( MAVLink.createMessage(set));
			
			ind.setText("Saved mode " + (myNum));
		}

		
		public void onNothingSelected(AdapterView<?> arg0) {
						
		}
	}	
}
