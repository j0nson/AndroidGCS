package com.bvcode.ncopter.widgets;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
 
import com.MAVLink.MAVLink;
import com.MAVLink.MAVLink.MAV_CMD;
import com.MAVLink.MAVLink.MAV_FRAME;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_waypoint;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.mission.MissionActivity;
import com.bvcode.ncopter.setup.SetupActivity;

public class MissionWidget extends TableLayout implements OnClickListener {
	msg_waypoint msg;
	
	AttributeSet attrs;

	Spinner command;
	ArrayAdapter<String> commandAdapter;
	
	Spinner frame;
	ArrayAdapter<String> frameAdapter; 
	
	EditText param1, param2, param3, param4, posX, posY, posZ;
	
	Button button1;
	
	CheckBox current, autoContinue;
	
	public MissionWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.attrs = attrs;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.mission_widget, this, true);
		
		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(this);
		
		param1 = (EditText) findViewById(R.id.Param1Value);
		param2 = (EditText) findViewById(R.id.Param2Value);
		param3 = (EditText) findViewById(R.id.Param3Value);
		param4 = (EditText) findViewById(R.id.Param4Value);
				
		posX = (EditText) findViewById(R.id.positionXValue);
		posY = (EditText) findViewById(R.id.positionYValue);
		posZ = (EditText) findViewById(R.id.positionZValue);
		
		current = (CheckBox) findViewById(R.id.checkBox1);
		autoContinue = (CheckBox) findViewById(R.id.checkBox2);
		
		command = (Spinner) findViewById(R.id.spinner1);
		commandAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
		commandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		Field[] field = MAV_CMD.class.getFields();
	    for (Field f : field) {
	    	//String tmp = f.getName();
	    	//tmp = tmp.replace("MAV_CMD", "");
	    	//tmp = tmp.replace("NAV_", "");
	    	//tmp = tmp.replace("_", " ");
	    	commandAdapter.add(f.getName());
		}

		command.setAdapter(commandAdapter);
		command.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		    	String s = (String) parent.getItemAtPosition(pos);

				try {
					msg.command = MAV_CMD.class.getField(s).getInt(null);
				} catch (IllegalArgumentException e) {
					msg.command = 16;
					e.printStackTrace();
				} catch (SecurityException e) {
					msg.command = 16;
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					msg.command = 16;
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					msg.command = 16;
					e.printStackTrace();
				}

		    }
		    public void onNothingSelected(AdapterView<?> parent) {
		    }
		});
		
		frame = (Spinner) findViewById(R.id.spinner2);
		frameAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
		frameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Field[] field2 = MAV_FRAME.class.getFields();
	    for (Field f : field2) {
			frameAdapter.add(f.getName());
		}
	    
		frame.setAdapter(frameAdapter);
		frame.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		    	String s = (String) parent.getItemAtPosition(pos);
		    	
				try {
					msg.frame = MAV_FRAME.class.getField(s).getInt(null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
		    }
		    public void onNothingSelected(AdapterView<?> parent) {
		    }
		});	
	}

	
	public void saveData(){
		msg.param1 = Float.valueOf(param1.getText().toString());
		msg.param2 = Float.valueOf(param2.getText().toString());
		msg.param3 = Float.valueOf(param3.getText().toString());
		msg.param4 = Float.valueOf(param4.getText().toString());

		msg.x = Float.valueOf(posX.getText().toString());
		msg.y = Float.valueOf(posY.getText().toString());
		msg.z = Float.valueOf(posZ.getText().toString());

		msg.current = current.isChecked()? 1 : 0;
		msg.autocontinue = autoContinue.isChecked()? 1 : 0;
		
	}
	
	public void setPacket(IMAVLinkMessage imavLinkMessage) {
		msg = (msg_waypoint) imavLinkMessage;
		
		param1.setText( msg.param1 + "");
		param2.setText( msg.param2 + "");
		param3.setText( msg.param3 + "");
		param4.setText( msg.param4 + "");
		
		posX.setText( msg.x + "");
		posY.setText( msg.y + ""); 
		posZ.setText( msg.z + "");
		
		//TODO need to select the proper option in the menu
		String cmd = MAVLink.getMavCmd( msg.command );
		int spinnerPosition = commandAdapter.getPosition(cmd);
		command.setSelection(spinnerPosition);

		String ref = MAVLink.getMavFrame(msg.frame);
		spinnerPosition = frameAdapter.getPosition(ref);
		frame.setSelection(spinnerPosition);

		current.setChecked( msg.current != 0);
		autoContinue.setChecked( msg.autocontinue != 0);
		
	}

	public void onClick(View v) {
		if (v == button1) {
			((MissionActivity)v.getContext()).setCurrentWP(msg.seq);
		}
	}
}
