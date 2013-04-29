package com.bvcode.ncopter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_param_request_list;
import com.MAVLink.Messages.common.msg_param_set;
import com.MAVLink.Messages.common.msg_param_value;
import com.bvcode.ncopter.comms.CommunicationClient;

public class ParameterViewActivity extends Activity {
	ExpandableListView listView = null;
    private ExpandableListAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		if( CommonSettings.setOrientation(this, -1))
			return;
		//set audio stream controls
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
				
	    setContentView(R.layout.parameter_view);
	    
	    listView = (ExpandableListView) findViewById(R.id.gridview);
	    
	    adapter = new ExpandableListAdapter(this, 
	    		  new ArrayList<String>(),
                  new ArrayList<ArrayList<paramInfo>>());

        // Set this blank adapter to the list view
        listView.setAdapter(adapter);       
        ba.init();
        
        SharedPreferences settings = getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
      	if ( settings.getBoolean(getString(R.string.keepScreenOn), true) ){
      		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      	}
	}
	
	class paramInfo{
		char[] valueName;		
		float value;
		
	}

	public class ExpandableListAdapter extends BaseExpandableListAdapter {

	    @Override
	    public boolean areAllItemsEnabled()
	    {
	        return true;
	    }

	    private Context context;
	    private ArrayList<String> groups;
	    private ArrayList<ArrayList<paramInfo>> children;

	    public ExpandableListAdapter(Context context, ArrayList<String> groups,
	            ArrayList<ArrayList<paramInfo>> children) {
	        this.context = context;
	        this.groups = groups;
	        this.children = children;
	        
	    }

	    class clickHandler implements View.OnClickListener{
	    	public char[] valueName;
			EditText value;
			
	    	
			public void onClick(View v) {
    			msg_param_set set = new msg_param_set();
				set.target_system = MAVLink.CURRENT_SYSID;
				set.target_component = 0;
				set.param_id = valueName;
				set.param_value = Float.valueOf( value.getText().toString());
				ba.sendBytesToComm( MAVLink.createMessage(set));
				
			}
	    }
	    
	    /**
	     * Convert the parameter name to group / child entries
	     * @param paramName
	     */
	    public void addItem(paramInfo inf) {
	    	//Default group
	    	String grpName = "Misc";
	    	
	    	String paramName = MAVLink.convertIntNameToString(inf.valueName);
	    	
	    	// Try to split it...
	    	if( paramName.contains("_") )
	    		grpName = paramName.split("_")[0];
	    	
	        if (!groups.contains(grpName)){
	            groups.add(grpName);
	            children.add(new ArrayList<paramInfo>());
		        
	        }

	        int index = groups.indexOf(grpName);
	        
	        paramInfo found = null;
	        for (paramInfo i : children.get(index) ) {
				if( paramName.equals( MAVLink.convertIntNameToString(i.valueName)))
					found = i;
			}
	        
	        if( found == null)
	        	children.get(index).add(inf);
	        else
	        	found.value = inf.value;
	        
	    }

	    
	    public Object getChild(int groupPosition, int childPosition) {
	        return children.get(groupPosition).get(childPosition);
	    }

	    
	    public long getChildId(int groupPosition, int childPosition) {
	        return childPosition;
	    }
	    
	    // Return a child view. You can load your custom layout here.
	    
	    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
	            View convertView, ViewGroup parent) {
	    	
	    	paramInfo param = (paramInfo) getChild(groupPosition, childPosition);
	        
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater) context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.param_child_layout, null);
	            
	        }
	       
	        EditText tv = (EditText) convertView.findViewById(R.id.tvChild);
	        tv.setText(param.value + "");
	        	        
	        Button but = (Button) convertView.findViewById(R.id.saveData);
	        but.setText(MAVLink.convertIntNameToString(param.valueName));
	        // Add a handler to save the value.
	        clickHandler c =  new clickHandler() ;
			c.value = tv;
			c.valueName = param.valueName;
			but.setOnClickListener(c);
			
	        return convertView;
	    }

	    
	    public int getChildrenCount(int groupPosition) {
	        return children.get(groupPosition).size();
	        
	    }

	    
	    public Object getGroup(int groupPosition) {
	        return groups.get(groupPosition);
	        
	    }

	    
	    public int getGroupCount() {
	        return groups.size();
	        
	    }

	    
	    public long getGroupId(int groupPosition) {
	        return groupPosition;
	        
	    }

	    // Return a group view. You can load your custom layout here.
	    
	    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
	            ViewGroup parent) {
	    	
	        String group = (String) getGroup(groupPosition);
	        
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater) context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.param_group_layout, null);
	        }
	        TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
	        tv.setText(group);
	        
	        return convertView;
	        
	    }

	    
	    public boolean hasStableIds() {
	        return true;
	        
	    }

	    
	    public boolean isChildSelectable(int arg0, int arg1) {
	        return true;
	        
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
	
	CommunicationClient ba = new CommunicationClient(this) {
		public void notifyReceivedData(int count, IMAVLinkMessage m) {
			if (CommonSettings.isProtocolMAVLink()){
				switch(m.messageType){
					case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:{
						msg_param_value msg = (msg_param_value)m;

				        paramInfo inf = new paramInfo();;
				        inf.value = msg.param_value;
				        inf.valueName = msg.param_id;
				        adapter.addItem(inf);
				        
						adapter.notifyDataSetChanged();

						break;
					}
				}		
			}
		}
		
		@Override
		public void notifyConnected() {
			msg_param_request_list req = new msg_param_request_list();
			req.target_system = MAVLink.CURRENT_SYSID;
			req.target_component = 0;
			ba.sendBytesToComm( MAVLink.createMessage(req));
			
		}

		@Override
		public void notifyDisconnected() {
			
		}

		@Override
		public void notifyDeviceNotAvailable() {
			
		}
	};
}