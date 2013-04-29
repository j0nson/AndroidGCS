package com.bvcode.ncopter;

import java.lang.reflect.Field;
import java.util.HashMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.MAVLink;
import com.MAVLink.MAVLink.MAV_CMD;
import com.MAVLink.MAVLink.MODES_ARDUCOPTER;
import com.MAVLink.MAVLink.MODES_ARDUPILOT;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_heartbeat;
import com.MAVLink.Messages.common.msg_param_request_list;
import com.MAVLink.Messages.common.msg_param_set;
import com.MAVLink.Messages.common.msg_param_value;
import com.MAVLink.Messages.common.msg_rc_channels_raw;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.bvcode.ncopter.comms.CommunicationClient;
import com.bvcode.ncopter.CommonSettings;

public class ModeSelectionActivity extends Activity implements OnClickListener{
	CheckBox checkbox1, checkbox2, checkbox3, checkbox4, checkbox5, checkbox6;
	int count = 0;
	//boolean[] simpleModeArray = new boolean[6];
	Spinner spinners[] = new Spinner[6];
	TextView text[] = new TextView[6];
	TextView modeCurrentText;
	TextView modePWM;
	private TextView ind;
	public boolean intToBool(int intValue){
		return (intValue != 0);
	}
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if( CommonSettings.setOrientation(this, -1))
			return;
		//set audio stream controls
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
				
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
		
		modeCurrentText = (TextView)findViewById(R.id.modeCurrentText);
		modePWM = (TextView)findViewById(R.id.modePWM);
		
		checkbox1 = (CheckBox) findViewById(R.id.checkBox1);
        checkbox2 = (CheckBox) findViewById(R.id.checkBox2);
        checkbox3 = (CheckBox) findViewById(R.id.checkBox3);
        checkbox4 = (CheckBox) findViewById(R.id.checkBox4);
        checkbox5 = (CheckBox) findViewById(R.id.checkBox5);
        checkbox6 = (CheckBox) findViewById(R.id.checkBox6);
  
        //checkbox1.setOnCheckedChangeListener(this);
        checkbox2.setOnClickListener(this);
        checkbox3.setOnClickListener(this);
        checkbox4.setOnClickListener(this);
        checkbox5.setOnClickListener(this);
        checkbox6.setOnClickListener(this);
        checkbox1.setOnClickListener(this);
        
		ind = (TextView)findViewById(R.id.modeIndicator);
		
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
	
	CommunicationClient ba = new CommunicationClient(this) {

		
		
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
						if (name.startsWith("SIMPLE")){
							checkbox1.setVisibility(0);
							checkbox2.setVisibility(0);
							checkbox3.setVisibility(0);
							checkbox4.setVisibility(0);
							checkbox5.setVisibility(0);
							checkbox6.setVisibility(0);
	        
							String temp = Integer.toBinaryString((int)msg.param_value);
						    while (temp.length() < 6){
						    	temp = "0" + temp;
						    }
						    for (int i = 6; i > 0; i--){
						    	CommonSettings.simpleModeArray[i] = (Integer.valueOf(temp.substring((i-1), i)) != 0);
						    }

					        checkbox1.setChecked(CommonSettings.simpleModeArray[1]);
					        checkbox2.setChecked(CommonSettings.simpleModeArray[2]);
					        checkbox3.setChecked(CommonSettings.simpleModeArray[3]);
					        checkbox4.setChecked(CommonSettings.simpleModeArray[4]);
					        checkbox5.setChecked(CommonSettings.simpleModeArray[5]);
					        checkbox6.setChecked(CommonSettings.simpleModeArray[6]);
						}
						if( name.startsWith("FLTMODE") && ! name.endsWith("_CH")){
							int index = Integer.parseInt(name.replace("FLTMODE", ""))-1;
							spinners[index].setSelection( ( (int) msg.param_value ) );
							count++;
							//if (count == 5){
							//	ind.setText("Loaded " + count);
							//}
						}		
						break;
					}
					case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:{
						msg_rc_channels_raw msg = (msg_rc_channels_raw) m;
						
						int pulsewidth = msg.chan8_raw; //fixed wing
						String ch = "8";
						if ( CommonSettings.uavType == 2){ //quad
							pulsewidth = msg.chan5_raw;
							ch = "5";
						}
						
						int mode = 0;

						//from control_modes.pde
						if (pulsewidth > 1230 && pulsewidth <= 1360)    	 mode = 2;
					 	else if (pulsewidth > 1360 && pulsewidth <= 1490)    mode = 3;
					 	else if (pulsewidth > 1490 && pulsewidth <= 1620)    mode = 4;
					 	else if (pulsewidth > 1620 && pulsewidth <= 1749)    mode = 5;
					 	else if (pulsewidth >= 1750)                         mode = 6;       
					 	else 												 mode = 1;
						
						modeCurrentText.setText(" " + mode);
						modePWM.setText( ch + ": " + pulsewidth); 
						
						for( int i = 1; i <= 6; i++){
							if( mode == i){
								text[i-1].setBackgroundColor( Color.GREEN);
								text[i-1].setTextColor( Color.BLACK  );
							}else{
								text[i-1].setBackgroundColor( Color.BLACK );
								text[i-1].setTextColor( Color.WHITE );
							}
						}
						break;
					}
				}		
			}			
		}		
	};
		
	private void setupSpinner(int num, int mode) {		
		
		spinners[num] = (Spinner) findViewById(mode);
		int modeList = 1;
		if ( CommonSettings.uavType == 2){ //copter
			modeList = R.array.mode_array_copter;	
		}else{ // CommonSettings.uavType == 1 //fixed wing
			modeList = R.array.mode_array;
		}
		
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//Field[] field = MAVLink.getCustomModeTextList( CommonSettings.uavType );
		//for (Field f : field) {
		//	adapter.add(f.getName());
		//}

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, modeList, android.R.layout.simple_spinner_item);
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
			//if (count < 2){
			//	return;
			//}
			if( first ){
				first = false;
				return;
			}
			
			String valueName = "FLTMODE" + (myNum+1);
			
			char[] name = MAVLink.StringNameToInt(valueName); 
			
			msg_param_set set = new msg_param_set();
			set.target_system = MAVLink.CURRENT_SYSID;
			set.target_component = 0;
			set.param_id = name;
			set.param_value = pos;
			ba.sendBytesToComm( MAVLink.createMessage(set));
			
			ind.setText("Saved mode " + (myNum+1));
		}

		
		public void onNothingSelected(AdapterView<?> arg0) {
						
		}
	}


	public void onClick(View v) {
		CommonSettings.simpleModeArray[1] = checkbox1.isChecked();
		CommonSettings.simpleModeArray[2] = checkbox2.isChecked();
		CommonSettings.simpleModeArray[3] = checkbox3.isChecked();
		CommonSettings.simpleModeArray[4] = checkbox4.isChecked();
		CommonSettings.simpleModeArray[5] = checkbox5.isChecked();
		CommonSettings.simpleModeArray[6] = checkbox6.isChecked();
		
		int pos = Integer.parseInt("" + ((checkbox1.isChecked()) ? 1 : 0) + ((checkbox2.isChecked()) ? 1 : 0) + ((checkbox3.isChecked()) ? 1 : 0) + ((checkbox4.isChecked()) ? 1 : 0) + ((checkbox5.isChecked()) ? 1 : 0) + ((checkbox6.isChecked()) ? 1 : 0),2 );
		char[] name = MAVLink.StringNameToInt("SIMPLE");
		
		Log.d("New Simple Mode:", ""+pos);
		
		msg_param_set set = new msg_param_set();
		set.target_system = MAVLink.CURRENT_SYSID;
		set.target_component = 0;
		set.param_id = name;
		set.param_value = pos;
		ba.sendBytesToComm( MAVLink.createMessage(set));
		
		ind.setText("Saved Simple Modes");
	}
	
}
