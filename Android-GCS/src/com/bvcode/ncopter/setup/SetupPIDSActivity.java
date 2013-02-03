package com.bvcode.ncopter.setup;

import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.MAVLink.MAVLink;
import com.MAVLink.VerifiedMAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_param_request_list;
import com.MAVLink.Messages.common.msg_param_set;
import com.MAVLink.Messages.common.msg_param_value;
import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.AC1Data.AcroPIDS;
import com.bvcode.ncopter.AC1Data.AltitudePids;
import com.bvcode.ncopter.AC1Data.GPSPids;
import com.bvcode.ncopter.AC1Data.ProtocolParser;
import com.bvcode.ncopter.AC1Data.StablePids;
import com.bvcode.ncopter.comms.CommunicationClient;
import com.bvcode.ncopter.widgets.PIDWidget;

public class SetupPIDSActivity extends Activity implements OnClickListener {

	private static final String[] names_ac1 = {"Acro", "Stable", "Position", "Altitude"};
	private static final byte[][] cmds = {ProtocolParser.requestAcroPIDs(),
		  ProtocolParser.requestStablePIDs(),
		  ProtocolParser.requestGPSPIDs(),
		  ProtocolParser.requestAltitudePIDs()};

	Button leftButton, rightButton, saveButton;
	ViewFlipper vf;

	// AC1
	PIDWidget stableRoll, stablePitch, stableYaw;
	PIDWidget acroRoll, acroPitch, acroYaw;
	PIDWidget GPSPitch, GPSRoll;
	PIDWidget AltitudePID;
	
	// Dynamic 
	HashMap<String, LinearLayout> pages = new HashMap<String, LinearLayout>();
	HashMap<String, PIDWidget> widgets = new HashMap<String, PIDWidget>();
	HashMap<String, Vector<PIDWidget>> pageWidgets = new HashMap<String, Vector<PIDWidget>>();
	Vector<String> pageNames = new Vector<String>();
	
	VerifiedMAVLink verifiedMAVLink;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		if( CommonSettings.isProtocolAC1()){
			setupAC1();
		
		}else if(CommonSettings.isProtocolMAVLink()){
			setContentView(R.layout.setup_pids_mavlink);

			vf = (ViewFlipper) findViewById(R.id.mavlink_profileSwitcher);
			
			leftButton = (Button) findViewById(R.id.mavlink_leftButton);
			leftButton.setOnClickListener(this);
			rightButton = (Button) findViewById(R.id.mavlink_rightButton);
			rightButton.setOnClickListener(this);
			saveButton = (Button) findViewById(R.id.mavlink_saveButton);
			saveButton.setOnClickListener(this);
			
		}else{
			finish();
			return;
			
		}
				
		setButtonNames(false);
		
		ba.init();
		
		verifiedMAVLink = new VerifiedMAVLink(ba);
		
	}

	

	private void setButtonNames(boolean requestData) {
		int i = vf.getDisplayedChild();
		
		if( CommonSettings.isProtocolAC1()){
			leftButton.setText( "<- " + names_ac1[ (i - 1 + names_ac1.length)% names_ac1.length]);
			rightButton.setText( names_ac1[ (i + 1 + names_ac1.length)% names_ac1.length] + " ->");
		
			if( requestData)
				ba.sendBytesToComm(cmds[i]);
			
		}else if(CommonSettings.isProtocolMAVLink()){
			String names[] = pageNames.toArray(new String[0]);

			if( names.length > 1){
				leftButton.setText( "<- " + names[ (i - 1 + names.length)% names.length]);
				rightButton.setText( names[ (i + 1 + names.length)% names.length] + " ->");
				
			}
			
			if(requestData){
				msg_param_request_list req = new msg_param_request_list();
				req.target_system = MAVLink.CURRENT_SYSID;
				req.target_component = 0;
				ba.sendBytesToComm( MAVLink.createMessage(req));
			}
		}
		
	}
	
	
	public void onClick(View v) {
		if (v == leftButton){
			vf.showPrevious();
			setButtonNames(true);
			
		}
		
		if (v == rightButton){
			vf.showNext();
			setButtonNames(true);
			
		}
		
		if(v == saveButton){
			if( CommonSettings.isProtocolAC1()){
				savePIDsAC1();
				
			}else if(CommonSettings.isProtocolMAVLink()){
				try {
					savePIDsMAVLink();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}			
		}
	}
	
	void saveParameter(String valueName, float value){
		char[] name = MAVLink.StringNameToInt(valueName);		
		msg_param_set set = new msg_param_set();
		set.target_system = MAVLink.CURRENT_SYSID;
		set.target_component = 0;
		set.param_id = name;
		set.param_value = value;
		
		verifiedMAVLink.put(valueName, MAVLink.createMessage(set));
		
	}

	// Sends all the entries in a widget to the copter
	public void saveAllEntries(PIDWidget p){
		String[] entries = p.getEntries();
		for(String s: entries){
			saveParameter(s, p.get(s));
			p.saving(s);
		
		}	
	}
	
	private void savePIDsMAVLink() throws InterruptedException {
	
		int i = vf.getDisplayedChild();
		String names[] = pageNames.toArray(new String[0]);
		PIDWidget toSave[] = pageWidgets.get(names[i]).toArray(new PIDWidget[0]);
		
		for( PIDWidget p: toSave)
			saveAllEntries(p);

		verifiedMAVLink.start(5);
		
	}

	double stable_KP_Rate = 0;
	double magnetometer = 0;
	double xMitFactor = 0;
	private double maxAngle;
	private double geog_correction_factor;
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ba.onDestroy();
		
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		ba.onActivityResult(requestCode, resultCode, data);
		
	}
	
	CommunicationClient ba = new CommunicationClient(this) {
		public void notifyReceivedData(int count, IMAVLinkMessage m) {
			if(CommonSettings.isProtocolAC1()){
				receivedAC1Values(m);			
				
			}else if(CommonSettings.isProtocolMAVLink()){
				
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
						boolean isConfirm = msg.param_index == 65535; // indicates that this is a confirmation message
						String name = new String(valueName, 0, i);
						
						if(isConfirm)
							verifiedMAVLink.verifyReceipt( name);
						
						receivedMAVLinkValues(name, msg.param_value, msg.param_id, isConfirm);
						
						break;
					}
				}
				Log.v("gsd", m.getClass().getName());
				
			}
		}

			
		private void receivedMAVLinkValues(String name, float param_value, char[] param_id, boolean isConfirm) {
		
			// structure: PageNAME_SUBunit_value 
			String split[] =name.split("_"); 
//			Log.d("gsd", name);
			if( split.length == 2){
				if(name.contains("XTRACK") ){
					split = new String[]{"XTRACK", split[0], split[1]};
					
				}				
			}
			
			if( split.length == 3){ // Expect three bits
				
				String pageName = split[0];
				String sub = split[1];
				String value = split[2];
				
				if(!( value.equals("P") ||
				  	  value.equals("I") ||
					  value.equals("D") ||
					  value.equals("IMAX") ))
					return;
				
				// Contruct or call up the layout
				LinearLayout layout = null;				
				if(!pages.containsKey(pageName)){
					ScrollView s = new ScrollView(vf.getContext());
					layout = new LinearLayout(s.getContext());
					layout.setOrientation( LinearLayout.VERTICAL);
					LayoutParams params = new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1);
					layout.setLayoutParams(params);
					s.addView(layout);
					vf.addView(s);
					
					pages.put(pageName, layout);
					
					pageNames.add(pageName);
					
					pageWidgets.put(pageName, new Vector<PIDWidget>());
					
					TextView tv = new TextView(layout.getContext());
					tv.setText(pageName);
					layout.addView(tv);
					
				}else
					layout = pages.get(pageName);
				
				// Construct or callup the widget
				PIDWidget pid;
				if(!widgets.containsKey(pageName +"_" + sub)){
					pid = new PIDWidget(layout.getContext(), null);
					LayoutParams params = new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
					pid.setLayoutParams(params);
					pid.setName(sub);
					
					layout.addView(pid);
					
					pageWidgets.get(pageName).add(pid);
					
					widgets.put(pageName+"_"+sub, pid);
					
				}else{
					pid = widgets.get(pageName +"_" + sub);
					
				}
				
				if(!pid.containsEntry(name))
					pid.addEntry(value, name);
				
				//if( value.equals("IMAX"))
				//	pid.set(name, param_value/100.0f, isConfirm);
				//else
					pid.set(name, param_value, isConfirm);
				
				setButtonNames(false);
					
			
			}
			
		}

		@SuppressWarnings("deprecation")
		private void receivedAC1Values(IMAVLinkMessage m) {
			if(AcroPIDS.class.isInstance(m)){
				AcroPIDS ap = (AcroPIDS)m;
				acroRoll.set_AC1_PID(ap.rollPID[0], ap.rollPID[1], ap.rollPID[2]);
				acroPitch.set_AC1_PID(ap.pitchPID[0], ap.pitchPID[1], ap.pitchPID[2]);
				acroYaw.set_AC1_PID(ap.yawPID[0], ap.yawPID[1], ap.yawPID[2]);
				xMitFactor = ap.xMitFactor;
				
			}else if(StablePids.class.isInstance(m)){
				StablePids sp = (StablePids)m;
				stableRoll.set_AC1_PID(sp.rollPID[0], sp.rollPID[1], sp.rollPID[2]);
				stablePitch.set_AC1_PID(sp.pitchPID[0], sp.pitchPID[1], sp.pitchPID[2]);
				stableYaw.set_AC1_PID(sp.yawPID[0], sp.yawPID[1], sp.yawPID[2]);
				stable_KP_Rate = sp.KP_rate;
				magnetometer = sp.magnetometer;
				
			}else if(GPSPids.class.isInstance(m)){
				GPSPids gps = (GPSPids)m;
				GPSRoll.set_AC1_PID(gps.rollPID[0], gps.rollPID[1], gps.rollPID[2]);
				GPSPitch.set_AC1_PID(gps.pitchPID[0], gps.pitchPID[1], gps.pitchPID[2]);
				maxAngle = gps.maxAngle;
				geog_correction_factor = gps.geog_correctionFactor;
				
			}else if(AltitudePids.class.isInstance(m)){
				AltitudePids alt = (AltitudePids)m;
				AltitudePID.set_AC1_PID(alt.altitude[0], alt.altitude[1], alt.altitude[2]);
				
			}
		} 
		
		@Override
		public void notifyConnected() {
			setButtonNames(true);

		}

		@Override
		public void notifyDisconnected() {
			
		}

		@Override
		public void notifyDeviceNotAvailable() {
			
		}
	};
	
	@SuppressWarnings("deprecation")
	private void savePIDsAC1() {
		//Send the values for the current screen
		byte[] cmd = null;
		switch( vf.getDisplayedChild()){
			case 0: //acro
				cmd = ProtocolParser.saveAcroPids( acroRoll.get_AC1_PIDDouble(),
												   acroPitch.get_AC1_PIDDouble(),
												   acroYaw.get_AC1_PIDDouble(), xMitFactor);
				break;
				
			case 1: //stable
				cmd = ProtocolParser.saveStablePids(stableRoll.get_AC1_PIDDouble(),
													stablePitch.get_AC1_PIDDouble(),
													stableYaw.get_AC1_PIDDouble(),
													stable_KP_Rate, 
													magnetometer);
				break;
				
			case 2: //GPS
				cmd = ProtocolParser.saveGPSPids(GPSRoll.get_AC1_PIDDouble(), 
												 GPSPitch.get_AC1_PIDDouble(), 
												 maxAngle, geog_correction_factor);
				break;
				
			case 3: //altitude
				cmd = ProtocolParser.saveAltitudePids(AltitudePID.get_AC1_PIDDouble());
				break;
		
		}
		if(cmd != null)
			ba.sendBytesToComm(cmd);
	}
	
	@SuppressWarnings("deprecation")
	private void setupAC1() {
		setContentView(R.layout.setup_pids);

		//Stable
		stableRoll = ((PIDWidget) findViewById(R.id.stable_roll_pid));
		stableRoll.setName("Roll");
		stableRoll.createAC1();
		stablePitch = ((PIDWidget) findViewById(R.id.stable_pitch_pid));
		stablePitch.setName("Pitch");
		stablePitch.createAC1();
		stableYaw = ((PIDWidget) findViewById(R.id.stable_yaw_pid));
		stableYaw.setName("Yaw");
		stableYaw.createAC1();

		//Acro
		acroRoll = ((PIDWidget) findViewById(R.id.acro_roll_pid));
		acroRoll.setName("Roll");
		acroRoll.createAC1();
		acroPitch = ((PIDWidget) findViewById(R.id.acro_pitch_pid));
		acroPitch.setName("Pitch");
		acroPitch.createAC1();
		acroYaw = ((PIDWidget) findViewById(R.id.acro_yaw_pid));
		acroYaw.setName("Yaw");
		acroYaw.createAC1();


		//GPS
		GPSRoll = ((PIDWidget) findViewById(R.id.gps_roll_pid));
		GPSRoll.setName("Roll");
		GPSRoll.createAC1();
		GPSPitch = ((PIDWidget) findViewById(R.id.gps_pitch_pid));
		GPSPitch.setName("Pitch");
		GPSPitch.createAC1();


		//Altitude
		AltitudePID = ((PIDWidget) findViewById(R.id.alt_pid));
		AltitudePID.setName("PIDS");		
		AltitudePID.createAC1();
				
		vf = (ViewFlipper) findViewById(R.id.profileSwitcher);
		
		leftButton = (Button) findViewById(R.id.leftButton);
		leftButton.setOnClickListener(this);
		rightButton = (Button) findViewById(R.id.rightButton);
		rightButton.setOnClickListener(this);
		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(this);
	}
}
