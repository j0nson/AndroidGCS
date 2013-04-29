package com.bvcode.ncopter.setup;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.MAVLink.Messages.IMAVLinkMessage;
import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.AC1Data.ProtocolParser;
import com.bvcode.ncopter.AC1Data.SensorData;
import com.bvcode.ncopter.AC1Data.SensorOffsets;
import com.bvcode.ncopter.comms.CommunicationClient;
import com.bvcode.ncopter.widgets.NumberPicker;

public class SetupOffsetsActivity extends Activity implements OnClickListener{
	
	NumberPicker roll, pitch, Z;
	TextView rollValue, pitchValue, zValue;
	
	Button saveOffsets;
	
	Button driftLeft, driftRight, driftForward, driftBackward;
	
	int[] gyroOffsets = null;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.setup_offsets);
		//set audio stream controls
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
						
		roll = (NumberPicker) findViewById(R.id.rollOffset);
		roll.setRange(2000, 3000);
		pitch = (NumberPicker) findViewById(R.id.pitchOffset);
		pitch.setRange(2000, 3000);
		Z = (NumberPicker) findViewById(R.id.zOffset);
		Z.setRange(2000, 3000);
		
		rollValue = (TextView) findViewById(R.id.rollValue);
		pitchValue = (TextView) findViewById(R.id.pitchValue);
		zValue = (TextView) findViewById(R.id.zValue);

		saveOffsets = (Button) findViewById(R.id.saveoffsets);
		saveOffsets.setOnClickListener(this);
		
		driftLeft = (Button) findViewById(R.id.driftLeft);
		driftLeft.setOnClickListener(this);
		
		driftRight= (Button) findViewById(R.id.driftRight);
		driftRight.setOnClickListener(this);
		
		driftForward = (Button) findViewById(R.id.driftForward);
		driftForward.setOnClickListener(this);
		
		driftBackward = (Button) findViewById(R.id.driftBackwards);
		driftBackward.setOnClickListener(this);
		
		
		ba.init();
	}

	
	
	public void onClick(View v) {
		if( CommonSettings.isProtocolAC1()){
			if( v == saveOffsets){			
				if(gyroOffsets != null && gyroOffsets.length == 6){
					ba.sendBytesToComm(
							ProtocolParser.saveOffsets(gyroOffsets[0], gyroOffsets[1], gyroOffsets[2], 
													   roll.getCurrent(), pitch.getCurrent(), Z.getCurrent()));
					ba.sendBytesToComm(ProtocolParser.requestSensorOffsets());
				}
				
			}else{
				
				int offsetRoll = 0, offsetPitch = 0;
				
				// counter the drift, relative to the APM
				// Frame mounting (at this time) is not relevant.
				if( v == driftLeft){
					offsetPitch --;
					
				}else if( v == driftRight){
					offsetPitch ++;
					
				}else if( v == driftForward){
					offsetRoll --;
					
				}else if( v == driftBackward){
					offsetRoll ++;
				
				}
				
				if( offsetRoll != 0 || offsetPitch != 0){
					// One was changed. Add the 
					roll.setCurrent( roll.getCurrent() + offsetRoll);
					pitch.setCurrent( pitch.getCurrent() + offsetPitch);
					
				}
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

	CommunicationClient ba = new CommunicationClient(this){
		
		@Override
		public void notifyReceivedData(int count, IMAVLinkMessage m) {
		
			if( CommonSettings.isProtocolAC1()){
				if(SensorData.class.isInstance(m)){
					SensorData sd = (SensorData)m;
					pitchValue.setText("Roll: " + sd.roll);
					rollValue.setText("Pitch: " + sd.pitch);
					zValue.setText("Z: " + sd.z);
				}
				else if( SensorOffsets.class.isInstance(m)){
					SensorOffsets so = (SensorOffsets) m;
					
					gyroOffsets = so.lastOffsets;
					if(gyroOffsets != null && gyroOffsets.length == 6){
						roll.setCurrent(so.roll); // accel offsets
						pitch.setCurrent(so.pitch); 
						Z.setCurrent(so.z);
						// Continue receiving transmitter values
						sendBytesToComm(ProtocolParser.requestSensorData());
	
					}else
						sendBytesToComm(ProtocolParser.requestSensorOffsets());
					
				}
			}
		}


		@Override
		public void notifyConnected() {
			sendBytesToComm(ProtocolParser.requestSensorOffsets());
			
		}

		@Override
		public void notifyDisconnected() {
			
		}

		@Override
		public void notifyDeviceNotAvailable() {
			
			
		}	
	};
}
