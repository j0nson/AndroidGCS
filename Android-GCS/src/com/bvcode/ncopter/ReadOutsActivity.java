package com.bvcode.ncopter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_raw_imu;
import com.MAVLink.Messages.common.msg_raw_pressure;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.bvcode.ncopter.AC1Data.FlightData;
import com.bvcode.ncopter.AC1Data.ProtocolParser;
import com.bvcode.ncopter.comms.CommunicationClient;
import com.bvcode.ncopter.widgets.ScopeDisplay;

public class ReadOutsActivity extends Activity implements OnLongClickListener, OnClickListener, OnCheckedChangeListener{
	ScopeDisplay scope;
	boolean waitingForResult = false;
	private TableLayout layout;
	private Button doneSettingsButton;

	// Settings overlay layout
	private int nRows = 4;
	private int nColumns = 3;

	String[] labels = { "Gyro X","Gyro Y","Gyro Yaw",
			"Accel X","Accel Y","Accel Z", 
			"Mag X", "Mag Y", "Mag Z", 
			"Baro", "Temp"};
	private SeekBar sb;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if( CommonSettings.setOrientation(this, -1))
			return;

		setContentView(R.layout.readouts_main);
		//set audio stream controls
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
				
		scope = (ScopeDisplay) findViewById(R.id.scope);
		scope.setOnLongClickListener(this);

		int[] colors = {Color.RED, Color.BLUE, Color.GREEN, 
						Color.CYAN, Color.MAGENTA, Color.YELLOW, 
						0xFF800000, 0xff008000, 0xFF000080,
						0xFF008080, 0xFF800080};
		
		Paint[] p = new Paint[colors.length];
		for( int i = 0; i<p.length;i++){
			p[i] = new Paint();
			p[i].setColor(colors[i]);
			
		}
		scope.setDataSize(labels.length);
		scope.setNames(labels);
		scope.setColors(p);
		
		scope.enableEntry(0);
		scope.enableEntry(1);
		scope.enableEntry(2);
		
        layout = (TableLayout)findViewById(R.id.readoutMenu);
        layout.setGravity( Gravity.CENTER);
        
        ba.init();
        setupOverlay();
        
        SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
      	if ( settings.getBoolean(getString(R.string.keepScreenOn), true) ){
      		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      	}
	}

	void setupOverlay(){
		int i = 0;
		
		for(int y = 0; y < nRows; y++){
			TableRow tr = new TableRow(this);
			tr.setGravity( Gravity.CENTER);
			layout.addView(tr);
			for( int x = 0; x < nColumns; x++){
				if( i < labels.length){
					CheckBox cb = new CheckBox(layout.getContext());
					cb.setOnCheckedChangeListener(this);
					
					tr.addView(cb);
					cb.setTag(new Integer(i));
					cb.setText(labels[i]);
					cb.setTextColor(scope.getEntryColor((Integer)cb.getTag()));
					cb.setChecked(scope.isActive((Integer)cb.getTag()));
					i++;
				}
			}
		}
		
		TableRow tr = new TableRow(this);
		tr.setGravity( Gravity.CENTER);
		doneSettingsButton = new Button(this);
		doneSettingsButton.setOnClickListener(this);
		doneSettingsButton.setText("Done");
		
		sb = new SeekBar(this);
		sb.setMax(40);
		sb.setProgress(20);
		sb.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
			
			
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				scope.setDrawRate(progress);
				
			}
		});
		
		TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.span = 3;
		sb.setLayoutParams(params);
		
		tr.addView(sb);
		tr.addView(doneSettingsButton);
		layout.addView(tr);
	
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
		
		private int press_abs = 0;
		private int temp = 0;

		@Override
		public void notifyConnected() {
			if( CommonSettings.isProtocolAC1()){
				sendBytesToComm(ProtocolParser.requestFlightData());
				
			}else if (CommonSettings.isProtocolMAVLink()){

				scope.setDrawRate(sb.getProgress());

				msg_request_data_stream req = new msg_request_data_stream();
				req.req_message_rate = 20;
				req.req_stream_id = MAVLink.MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS;
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
					double data[] = {fd.gyroX, fd.gyroY, fd.gyroYaw,
									 fd.accelX, fd.accelY, fd.accelZ,
									 fd.magX, fd.magY, fd.magZ,
									 0, 0};
	
					scope.newFlightData(data);
					
				}
			}else if (CommonSettings.isProtocolMAVLink()){
				switch(m.messageType){
					case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU:{
						msg_raw_imu msg = (msg_raw_imu)m;
						double data[] = {
									msg.xgyro, msg.ygyro, msg.zgyro,
									msg.xacc,msg.yacc,msg.zacc,
									msg.xmag, msg.ymag, msg.zmag,
									press_abs, temp };
						
						scope.newFlightData(data);
						break;						
					}
					case msg_raw_pressure.MAVLINK_MSG_ID_RAW_PRESSURE:{
						msg_raw_pressure msg = (msg_raw_pressure)m;
						press_abs = msg.press_abs;
						temp = msg.temperature;
						
						break;
					}
				}

				
			}
			
		}
	};
	
	
	public boolean onLongClick(View v) {
		layout.setVisibility(TableLayout.VISIBLE);
		return false;

	}

	
	public void onClick(View v) {
		layout.setVisibility(TableLayout.INVISIBLE);
		
	}

	
	public void onCheckedChanged(CompoundButton bv, boolean isChecked) {
		Integer dataIndex = (Integer) bv.getTag();		
		
		if(isChecked){
			// Add dataIndex to rendered items
			int c = scope.enableEntry(dataIndex);
			if( c == -1){
				bv.setChecked(false);
				
			}else
				bv.setTextColor(c);
			
		}else{
			scope.disableEntry(dataIndex);
			bv.setTextColor(Color.WHITE);
			
		}
	
	}
}
