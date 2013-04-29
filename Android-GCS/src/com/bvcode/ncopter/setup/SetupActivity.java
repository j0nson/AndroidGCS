package com.bvcode.ncopter.setup;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.TabHost;

import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.R;

public class SetupActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if( CommonSettings.setOrientation(this, -1))
			return;
		//set audio stream controls
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
				
        setTitle("Setup");
        
        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;
               
        // Add the PID page
        intent = new Intent().setClass(this, SetupPIDSActivity.class);
        spec = tabHost.newTabSpec("PIDS");
        spec.setIndicator("PIDS", res.getDrawable(R.drawable.ic_generic_tab_icon));
        spec.setContent(intent);
        tabHost.addTab(spec);
    
        // Add the hardware setup page
        if(CommonSettings.isProtocolMAVLink()){
	        intent = new Intent().setClass(this, SetupHardwareActivity.class);
	        spec = tabHost.newTabSpec("Hardware");
	        spec.setIndicator("Hardware", res.getDrawable(R.drawable.ic_generic_tab_icon));
	        spec.setContent(intent);
	        tabHost.addTab(spec);

        }
        
        if( CommonSettings.isProtocolAC1()){
	        // Add the offsets tab
	        intent = new Intent().setClass(this, SetupOffsetsActivity.class);
	        spec = tabHost.newTabSpec("Offsets");
	        spec.setIndicator("Offsets", res.getDrawable(R.drawable.ic_generic_tab_icon));
	        spec.setContent(intent);
	        tabHost.addTab(spec);
        }
        
        // Add the transmitter tab
        intent = new Intent().setClass(this, SetupTransmitterActivity.class);
        spec = tabHost.newTabSpec("Transmitter");
        spec.setIndicator("Transmitter", res.getDrawable(R.drawable.ic_generic_tab_icon));
        spec.setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
        
    }
}