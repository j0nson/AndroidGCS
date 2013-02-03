package com.bvcode.ncopter.mission;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.common.msg_waypoint;
import com.bvcode.ncopter.R;

public class MissionListAdapter implements ListAdapter{
	MissionListActivity parent;
	MissionListView list;
	
	public MissionListAdapter(MissionListActivity missionListActivity, MissionListView listView) {
		parent = missionListActivity;
		list = listView;
	}

	public void registerDataSetObserver(DataSetObserver paramDataSetObserver) {
		
	}

	public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver) {
		
	}

	public int getCount() {
		return MissionActivity.getWaypointSize();
		
	}

	public Object getItem(int paramInt) {
		if(paramInt < MissionActivity.getWaypointSize())
			return MissionActivity.getWaypoint(paramInt);
		
		return null;
	}

	public long getItemId(int paramInt) {
		return paramInt;
		
	}

	public boolean hasStableIds() {
		return true;
		
	}

	public View getView(int paramInt, View convertView, ViewGroup parent) {
		String group;
		
		Object o = getItem(paramInt);
		if( o == null){
			group = "Not Loaded Yet";
			
		}else{
			msg_waypoint msg = (msg_waypoint)o;
			group = MAVLink.getMavCmd(msg.command) + "	Altitude: " + msg.z + "m\n	P1: " + msg.param1 + " P2: " + msg.param2 + " P3: " + msg.param3 + " P4: " + msg.param4;
			group = group.replace("MAV_CMD_", "");
			group = group.replace("NAV_WAYPOINT", "WAYPOINT");
			group = group.replace("DO_", "");
			if ( msg.seq == 0 )
				group = "Home           ";
			if( msg.command == 0)
				group = "No Command";
			if( group.equals(""))
				group = "Unknown Command";
			
		}

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate( R.layout.mission_group_layout, null);
		
		}
		
		TextView tv = (TextView) convertView.findViewById(R.id.missionText);
		tv.setText(paramInt + " " + group);
		return convertView;

	}

	public int getItemViewType(int paramInt) {
		return 0;
		
	}

	public int getViewTypeCount() {
		return 1;
		
	}

	public boolean isEmpty() {
		return MissionActivity.getWaypointSize() == 0;

	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int paramInt) {		
		return true;
	}
	
	public void onDrop(int from, int to) {
		MissionActivity.swap(from, to);
		
	}
}