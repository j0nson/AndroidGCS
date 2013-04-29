package com.bvcode.ncopter.widgets;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bvcode.ncopter.R;
import com.bvcode.ncopter.comms.CommunicationClient;


public class ttsOptionsWidget extends Activity implements OnClickListener {
	CheckBox checkbox1, checkbox2, checkbox3, checkbox4, checkbox5, checkbox6, checkbox7, checkbox8, checkbox9, checkbox10;
	Button button1;
	
	public void onCreate(Bundle ttsOptions){
		super.onCreate(ttsOptions);
        setContentView(R.layout.tts_options_widget);
        //set audio stream controls
      	setVolumeControlStream(AudioManager.STREAM_MUSIC);
      		
        checkbox1 = (CheckBox) findViewById(R.id.checkBox1);
        checkbox2 = (CheckBox) findViewById(R.id.checkBox2);
        checkbox3 = (CheckBox) findViewById(R.id.checkBox3);
        checkbox4 = (CheckBox) findViewById(R.id.checkBox4);
        checkbox5 = (CheckBox) findViewById(R.id.checkBox5);
        checkbox6 = (CheckBox) findViewById(R.id.checkBox6);
        checkbox7 = (CheckBox) findViewById(R.id.checkBox7);
        checkbox8 = (CheckBox) findViewById(R.id.checkBox8);
        checkbox9 = (CheckBox) findViewById(R.id.checkBox9);
        checkbox10 = (CheckBox) findViewById(R.id.checkBox10);
        
        button1 = (Button) findViewById(R.id.button1);
        
        SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
        
        checkbox1.setChecked(settings.getBoolean(getString(R.string.speakModeChange), false));
        checkbox2.setChecked(settings.getBoolean(getString(R.string.speakWaypointReached), false));
        checkbox3.setChecked(settings.getBoolean(getString(R.string.speakConnectionStatus), false) );
        checkbox4.setChecked(settings.getBoolean(getString(R.string.speakLowBattery), false) );
        checkbox5.setChecked(settings.getBoolean(getString(R.string.speakWaypointNext), false) );
        checkbox6.setChecked(settings.getBoolean(getString(R.string.gpsOnMission), false) );
        checkbox7.setChecked(settings.getBoolean(getString(R.string.keepScreenOn), false) );
        checkbox8.setChecked(settings.getBoolean(getString(R.string.kmlLog), false) );
        checkbox9.setChecked(settings.getBoolean(getString(R.string.speakStatusMessage), false) );
        checkbox10.setChecked(settings.getBoolean(getString(R.string.tlog), false) );
        
        checkbox1.setOnClickListener(this);
        checkbox2.setOnClickListener(this);
        checkbox3.setOnClickListener(this);
        checkbox4.setOnClickListener(this);
        checkbox5.setOnClickListener(this);
        checkbox6.setOnClickListener(this);
        checkbox7.setOnClickListener(this);
        checkbox8.setOnClickListener(this);
        checkbox9.setOnClickListener(this);
        checkbox10.setOnClickListener(this);
        
        button1.setOnClickListener(this);
        
	}

	public void onClick(View arg0){
		//Toast.makeText(this, ((CompoundButton) arg0).getText() + " " + ((CompoundButton) arg0).isChecked(), Toast.LENGTH_LONG).show();
		SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		if ( arg0 == button1 ){
			editor.remove(CommunicationClient.DEFAULT_MODEM);
			Toast.makeText(this, "Bluetooth Settings Cleared", Toast.LENGTH_LONG).show();
		}else{
			editor.putBoolean( ""+((CompoundButton) arg0).getText(), ((CompoundButton) arg0).isChecked() );
		}
		editor.commit();

		//CommonSettings.currentLink = CommonSettings.LINK_BLUETOOTH;
	}
}
