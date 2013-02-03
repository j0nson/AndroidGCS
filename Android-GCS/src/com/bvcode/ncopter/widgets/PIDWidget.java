package com.bvcode.ncopter.widgets;

import java.util.Hashtable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bvcode.ncopter.R;

public class PIDWidget extends TableLayout{
	
	Hashtable<String, DoubleNumberPicker> pickers = new Hashtable<String, DoubleNumberPicker>();
	private TableLayout tl;
	private LayoutParams layout;
	AttributeSet attrs;
	
	public PIDWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.attrs = attrs;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    inflater.inflate(R.layout.pid_widget, this, true);
	    
	    tl = (TableLayout) findViewById(R.id.table);
	    
	    layout = new LayoutParams();
	    layout.height = LayoutParams.WRAP_CONTENT;
	    layout.width = LayoutParams.FILL_PARENT;
	    layout.weight = 1;
	}
		
	public void setName(String name){
		TextView n = (TextView) this.findViewById(R.id.PIDWidgetName);
		n.setText(name);
				
	}
	public String[] getEntries(){
		return pickers.keySet().toArray(new String[0]);
		
	}

	public float get(String val) {
		if( pickers.containsKey(val))
			return pickers.get(val).getValue();
		
		return 0;
	}

	public void set(String val, float param_value, boolean isConfirm) {
		if( pickers.containsKey(val))
			pickers.get(val).setValue(param_value, isConfirm);
		
	}
	public void addEntry(String label, String parameter) {
		DoubleNumberPicker t = new DoubleNumberPicker(getContext(), attrs);
	    t.setLayoutParams(layout);
	    t.setLabel("   "+label+"   ");
	    tl.addView(t); 
	    pickers.put(parameter, t);
		
	}	
	
	public boolean containsEntry(String key){
		return pickers.containsKey(key);
		
	}
	
	@Deprecated
	public void createAC1(){
	    DoubleNumberPicker d = new DoubleNumberPicker(getContext(), attrs);
	    d.setLayoutParams(layout);
	    d.setLabel("   P   ");
	    tl.addView(d);
	    pickers.put("P", d);
	    
	    d = new DoubleNumberPicker(getContext(), attrs);
	    d.setLayoutParams(layout);
	    d.setLabel("   I   ");
	    tl.addView(d);
	    pickers.put("I", d);
	    
	    d = new DoubleNumberPicker(getContext(), attrs);
	    d.setLayoutParams(layout);
	    d.setLabel("   D   ");
	    tl.addView(d); 	    
	    pickers.put("D", d);
	    
	}
	@Deprecated
	public double[] get_AC1_PIDDouble(){
		double[] a = {get("P"), get("I"), get("D") };
		return a;
		
	}
	
	@Deprecated
	public void set_AC1_PID(double P, double I, double D){
		set("P", (float)P, false);
		set("I", (float)I, false);
		set("D", (float)D, false);
		
	}

	public void saving(String s) {
		if( pickers.containsKey(s))
			pickers.get(s).saving();
		
	}

}
