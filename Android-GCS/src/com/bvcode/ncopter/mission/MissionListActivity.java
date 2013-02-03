package com.bvcode.ncopter.mission;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.common.msg_waypoint;
import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.widgets.MissionWidget;

public class MissionListActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, OnDismissListener{
	MissionListView listView = null;
	private MissionListAdapter adapter;
	private MissionWidget currentMissionWidget = null;

	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		if( CommonSettings.setOrientation(this, -1))
			return;

		setContentView(R.layout.mission_list);
		
		listView = (MissionListView) findViewById(R.id.missionView);
		adapter = new MissionListAdapter(this, listView);

		// Set this blank adapter to the list view
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		
	}

	public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
        Dialog dialog = new Dialog(this);
        currentMissionWidget = new MissionWidget(this, null);
        currentMissionWidget.setPacket( MissionActivity.getWaypoint(paramInt));
        dialog.setContentView(currentMissionWidget);
        dialog.setTitle("Waypoint "+ paramInt + " Edit");
        dialog.setCancelable(true);
		dialog.show();
		dialog.setOnDismissListener(this);
		
		
	}

	public boolean onItemLongClick(AdapterView<?> paramAdapterView, View paramView, final int paramInt, long paramLong) {
		
        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        alertbox.setMessage("Are you sure waypoint " + paramInt +" should be deleted?");
        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            // do something when the button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
        		MissionActivity.remove(paramInt);
        		listView.requestLayout();
        		
            	Toast.makeText(getApplicationContext(), "Deleted waypoint " + paramInt, Toast.LENGTH_SHORT).show();
            	
            }
        });
        alertbox.setNegativeButton("No", null);
        alertbox.show();

		return true;
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}

	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mission_list, menu);
	    return true;
	    
	}

	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.missionAddNew:
	    		
	    		msg_waypoint msg = new msg_waypoint();
	    		msg_waypoint last = MissionActivity.getLastWaypoint();
	    		if( last != null){
		    		msg.seq = last.seq+1;
		    		msg.x = last.x;
		    		msg.y = last.y;
		    		msg.z = last.z;
		    		
	    		}else{
		    		msg.seq = 0;
		    		msg.x = msg.y = msg.z = 0;
		    		
		    	}
	    		
	    		msg.frame = MAVLink.MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
	    		msg.command = 16;  //MAVLink.MAV_CMD.MAV_CMD_NAV_WAYPOINT;	    		
	    		msg.param1 = msg.param2 = msg.param3 = msg.param4 = 0; 
	    		msg.autocontinue = 1;
	    		
	    		MissionActivity.add(msg);			
	    		
	    		listView.requestLayout();
		    	
	    		return true;
	    		    		
	    	case R.id.missionClear:
	    		MissionActivity.waypointsClear();
	    		listView.requestLayout();
				return true;
    		
	    }
	    return false;
	}

	public void onDismiss(DialogInterface paramDialogInterface) {
		if(currentMissionWidget != null){
			currentMissionWidget.saveData();
			currentMissionWidget = null;
		}
		
		listView.invalidateViews();
		
		
	}

}
