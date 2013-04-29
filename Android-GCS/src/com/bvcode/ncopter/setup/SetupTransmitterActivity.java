package com.bvcode.ncopter.setup;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.MAVLink;
import com.MAVLink.VerifiedMAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_param_set;
import com.MAVLink.Messages.common.msg_param_value;
import com.MAVLink.Messages.common.msg_rc_channels_raw;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.MAVLink.Messages.common.msg_servo_output_raw;
import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.AC1Data.ProtocolParser;
import com.bvcode.ncopter.AC1Data.TransmitterValues;
import com.bvcode.ncopter.comms.CommunicationClient;
import com.bvcode.ncopter.widgets.MinMaxCurrentSlider;

public class SetupTransmitterActivity extends Activity{

	MinMaxCurrentSlider[] minMax;
	private TextView midR, midP, midY;
	VerifiedMAVLink verifiedMAVLink;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup_transmitter);
		//set audio stream controls
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
				
		minMax = new MinMaxCurrentSlider[7];
		
		initBar(0, R.id.trans_Roll, "Roll");
		initBar(1, R.id.trans_Pitch, "Pitch");
		initBar(2, R.id.trans_Throttle, "Throttle");
		initBar(3, R.id.trans_Yaw, "Yaw");
		initBar(4, R.id.trans_Mode, "Mode");
		initBar(5, R.id.trans_Aux1, "Aux1");
		initBar(6, R.id.trans_Aux2, "Aux2");
		
		midR = (TextView) findViewById(R.id.midR);
		midP = (TextView) findViewById(R.id.midP);
		midY = (TextView) findViewById(R.id.midY);
	     
		ba.init();
		verifiedMAVLink = new VerifiedMAVLink(ba);
		
	}
		
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.transmitter_menu, menu);
	    return true;
	    
	}
	
	byte[] saveParameter(String valueName, double value){
		
		char[] name = MAVLink.StringNameToInt(valueName);
		msg_param_set set = new msg_param_set();
		set.target_system = MAVLink.CURRENT_SYSID;
		set.target_component = 0;
		set.param_id = name;
		set.param_value = (float)value;
		return MAVLink.createMessage(set);
		
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menuStartCalib:

		    	//Reset all sliders
		    	for( int i = 0; i < minMax.length; i++)
 					minMax[i].resetMinMax(1500);
 				
		    	
		    	if(CommonSettings.isProtocolAC1()){
	 				double slope[] = {1,1,1,1,1,1};
	 				double offset[] = {0,0,0,0,0,0};
	 				ba.sendBytesToComm(ProtocolParser.setTransmitterScale(slope, offset));
	 				ba.sendBytesToComm(ProtocolParser.requestTransmitterValues());
		    		
		    	}
		    		    	
		    	return true;
		    case R.id.menuSaveMidpoints:
		    	if(CommonSettings.isProtocolAC1()){
			    	ba.sendBytesToComm(ProtocolParser.saveTransmitterOffsets());
					ba.sendBytesToComm(ProtocolParser.requestTransmitterScale());
					
		    	}else if(CommonSettings.isProtocolMAVLink()){

		    		for( int i = 0; i < minMax.length; i++)
		    			verifiedMAVLink.put("RC"+(i+1)+"_TRIM", saveParameter("RC"+(i+1)+"_TRIM", minMax[i].getCurrent()));
		    		
		    		verifiedMAVLink.start(5);
		    		
		    	}
		    	return true;
		    	
		    case R.id.menuSaveCalib:
		    	if(CommonSettings.isProtocolAC1()){
		    		double[] slope = new double[6];
					double[] offset = new double[6];
					
					//Compute the slope and offset.
					for(int i = 0; i < 6; i++){
						slope[i] = 1000.0 / (double) (minMax[i].getMaxValue()-minMax[i].getMinValue());
						offset[i] = 1000.0 - minMax[i].getMinValue() * slope[i];
					
					}
					
					ba.sendBytesToComm(ProtocolParser.setTransmitterScale( slope, offset));
					ba.sendBytesToComm(ProtocolParser.saveCommand());
					ba.sendBytesToComm(ProtocolParser.requestTransmitterValues());
					
		    	}else if(CommonSettings.isProtocolMAVLink()){
		    		
		    		for( int i = 0; i < minMax.length; i++){
		    			
		    			int min = minMax[i].getMinValue();
		    			int max = minMax[i].getMaxValue();
		    			
		    			if( min > 800 && min < 2200 
		    			 && max > 800 && max < 2200){
		    				
			    			verifiedMAVLink.put("RC"+(i+1)+"_MIN", saveParameter("RC"+(i+1)+"_MIN", min));
			    			verifiedMAVLink.put("RC"+(i+1)+"_MAX", saveParameter("RC"+(i+1)+"_MAX", max));
			    			
		    			}
		    		}	
		    		
		    		verifiedMAVLink.start(5);
		    		
		    	}
		    	
		    	return true;
		    	
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	/**
	 * Setup an indicator bar. Default to neutral(1500)
	 * @param id SeekBar ID to setup
	 */
	public void initBar(int i, int id, String name){
		
		// Due to stupid base, need to shift min to 0....
		minMax[i] = (MinMaxCurrentSlider) findViewById(id);
		minMax[i].setName(name);
		minMax[i].setRange(1000, 2000);
		minMax[i].resetMinMax(1500);
		minMax[i].setCurrent(1500);
		
		
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
		public void notifyReceivedData(int count, IMAVLinkMessage m) {
			if( CommonSettings.isProtocolAC1()){
				if( TransmitterValues.class.isInstance(m)){
					TransmitterValues ts2 = (TransmitterValues)m;
					int[] transValues = ts2.transValues;
					
					if( transValues.length == 9){
						for(int i = 0; i < 6; i++){
							minMax[i].setCurrent(transValues[i]);

						}
						
						midR.setText("Mid: " + (int)(transValues[6]));
						midP.setText("Mid: " + (int)(transValues[7]));
						midY.setText("Mid: " + (int)(transValues[8]));
						
					}				
				}
				
			}else if(CommonSettings.isProtocolMAVLink()){
				switch(m.messageType){
				//mavlink_msg_rc_channels_raw_send
					case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:{
						msg_rc_channels_raw msg = (msg_rc_channels_raw) m;
						
						minMax[0].setCurrent(msg.chan1_raw);
						minMax[1].setCurrent(msg.chan2_raw);
						minMax[2].setCurrent(msg.chan3_raw);
						minMax[3].setCurrent(msg.chan4_raw);
						minMax[4].setCurrent(msg.chan5_raw);
						minMax[5].setCurrent(msg.chan6_raw);
						minMax[6].setCurrent(msg.chan7_raw);
						break;
					}
					case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:{
						//msg_servo_output_raw msg = (msg_servo_output_raw)m;
						
						break;
					}
					case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:{
						msg_param_value msg = (msg_param_value)m;
						
						char[] valueName = new char[ msg.param_id.length];
						int i = 0;
						for(; i < msg.param_id.length; i++){
							valueName[i] = (char)msg.param_id[i];
							if( valueName[i] == 0)
								break;
							
						} 
						
						boolean isConfirm = msg.param_index == 65535; // indicates that this is a confirmation message
						String name = new String(valueName, 0, i);
						
						if(isConfirm){
							verifiedMAVLink.verifyReceipt( name);
							if( verifiedMAVLink.isDone()){
								Toast toast= Toast.makeText(SetupTransmitterActivity.this, "Confirmed Min/Max Calibration", 2000);
				    		    toast.setGravity(Gravity.TOP, -30, 50);
				    		    toast.show();
				    		
								
							}
						}
						
						int num = 0;
						if( name.startsWith("RC1")) num = 1;
						if( name.startsWith("RC2")) num = 2;
						if( name.startsWith("RC3")) num = 3;
						if( name.startsWith("RC4")) num = 4;
						if( name.startsWith("RC5")) num = 5;
						if( name.startsWith("RC6")) num = 6;
						if( name.startsWith("RC7")) num = 7;
						
						if( num != 0){
							num--;
							MinMaxCurrentSlider slider = minMax[num];
							
							if( name.contains("MIN")){
								slider.setMin((int)msg.param_value);
								
							}else if(name.contains("MAX")){
								slider.setMax((int)msg.param_value);
								
							}
						}
						
						break;
					}
				}
			}
			
		}
		
		 @Override
			public void notifyConnected() {
			 	if( CommonSettings.isProtocolAC1())
			 		sendBytesToComm(ProtocolParser.requestTransmitterValues());
			 	
			 	else if(CommonSettings.isProtocolMAVLink()){
					msg_request_data_stream req = new msg_request_data_stream();
					req.req_message_rate = 5;
					req.req_stream_id = MAVLink.MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS;
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
	};
}
