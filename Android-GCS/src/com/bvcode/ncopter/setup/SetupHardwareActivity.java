package com.bvcode.ncopter.setup;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_param_request_list;
import com.MAVLink.Messages.common.msg_param_set;
import com.MAVLink.Messages.common.msg_param_value;
import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.comms.CommunicationClient;

public class SetupHardwareActivity extends Activity implements OnClickListener{
	
	private Spinner battery_spin;

	CheckBox enable_compass, enable_sonar, enable_airspeed;

	private EditText declination, batt_capacity;

	private Button saveButton;
	
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.setup_hardware);
		//set audio stream controls
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
				
		// Setup Spinner
		battery_spin = (Spinner) findViewById(R.id.hardware_batt_spin);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.battery_modes, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    battery_spin.setAdapter(adapter);	    
	    battery_spin.setEnabled(false);
	    
	    enable_compass = (CheckBox) findViewById(R.id.hardware_compass);
	    enable_compass.setEnabled(false);
	    
	    enable_sonar = (CheckBox) findViewById(R.id.hardware_sonar);
	    enable_sonar.setEnabled(false);
	    
	    enable_airspeed = (CheckBox) findViewById(R.id.hardware_airspeed);
	    enable_airspeed.setEnabled(false);
	    
	    declination = (EditText) findViewById(R.id.hardware_decl);
	    declination.setEnabled(false);
	    
	    batt_capacity = (EditText) findViewById(R.id.hardware_batt_cap);
	    batt_capacity.setEnabled(false);
	    
	    saveButton = (Button) findViewById(R.id.hardware_save);
	    saveButton.setOnClickListener(this);
		ba.init();
		
	}

	public void onClick(View v) {
		if( v == saveButton){
			
			if(batt_capacity.isEnabled())
				setValue("BATT_CAPACITY", Float.parseFloat(batt_capacity.getText().toString()) );
			if(battery_spin.isEnabled())
				setValue("BATT_MONITOR", battery_spin.getSelectedItemPosition());
			
			if(enable_sonar.isEnabled())
				setValue("SONAR_ENABLE", enable_sonar.isChecked()? 1 : 0);
			
			if(declination.isEnabled())
				// value is shown in degrees, send as RAD
				setValue("COMPASS_DEC", Float.parseFloat(declination.getText().toString()) /180.0f * (float)Math.PI);
			
			if(enable_compass.isEnabled())
				setValue("MAG_ENABLE", enable_compass.isChecked()? 1 : 0);
			
			
			if((CommonSettings.isProtocolMAVLink() &&
			   		MAVLink.CURRENT_SYSID == MAVLink.MAVLINK_ARDU_MEGA_SYSID) &&
			   		enable_airspeed.isEnabled())
					setValue("FLOW_ENABLE", enable_airspeed.isChecked()? 1 : 0);
				
				
			
		}		
	}
	
	private void setValue(String valueName, float val){

		char[] name = MAVLink.StringNameToInt(valueName);
		
		msg_param_set set = new msg_param_set();
		set.target_system = MAVLink.CURRENT_SYSID;
		set.target_component = 0;
		set.param_id = name;
		set.param_value = val;
		ba.sendBytesToComm( MAVLink.createMessage(set));
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ba.onDestroy();
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		ba.onActivityResult(requestCode, resultCode, data);
		
	}

	CommunicationClient ba = new CommunicationClient(this){
		
		@Override
		public void notifyReceivedData(int count, IMAVLinkMessage m) {
			if(CommonSettings.isProtocolMAVLink()){
				switch(m.messageType){
					case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:{
						msg_param_value msg = (msg_param_value)m;
						
						char[] valueName = new char[ msg.param_id.length];
						int i = 0;
						for(; i < msg.param_id.length; i++){
							valueName[i] = (char)msg.param_id[i];
							if( valueName[i] == 0)
								break;
							
						}
	
						String name = new String(valueName, 0, i);
						if( name.equals("BATT_CAPACITY")){
							batt_capacity.setText(msg.param_value+"");
							batt_capacity.setEnabled(true);
							
						}
	
						if( name.equals("BATT_MONITOR")){
							battery_spin.setEnabled(true);
							battery_spin.setSelection((int) msg.param_value);
							
						}
						
						if( name.equals("SONAR_ENABLE")){
							enable_sonar.setEnabled(true);
							enable_sonar.setChecked((int) msg.param_value != 0);
							
						}
											
		        		if((CommonSettings.isProtocolMAVLink() &&
		               		MAVLink.CURRENT_SYSID == MAVLink.MAVLINK_ARDU_MEGA_SYSID) &&		 
							name.equals("FLOW_ENABLE")){
		        			
							enable_airspeed.setEnabled(true);
							enable_airspeed.setChecked((int) msg.param_value != 0);
							
						}
	
						if( name.equals("COMPASS_DEC")){
							float val = msg.param_value;
							// Convert radians to degrees for display.
							val = val / (float)Math.PI * 180.0f;
							declination.setText(val+"");
							declination.setEnabled(true);
						
						}
						
						if( name.equals("MAG_ENABLE")){
							enable_compass.setEnabled(true);
							enable_compass.setChecked((int) msg.param_value != 0);
							
						}
						
						break;
					}
				}			
			}
		}


		@Override
		public void notifyConnected() {
			msg_param_request_list req = new msg_param_request_list();
			req.target_system = MAVLink.CURRENT_SYSID;
			req.target_component = 0;
			ba.sendBytesToComm( MAVLink.createMessage(req));
					
		}

		@Override
		public void notifyDisconnected() {
			
		}

		@Override
		public void notifyDeviceNotAvailable() {
			
			
		}	
	};
}
