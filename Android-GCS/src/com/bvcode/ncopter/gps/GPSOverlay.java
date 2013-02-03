package com.bvcode.ncopter.gps;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class GPSOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	OverlayItem currentCopter = null;
	
	private Context mContext;
	
	public GPSOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		populate();
		
	}
	
	public GPSOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		populate();
		
	}
	
	public void setCopterOverlay(OverlayItem overlay) {
		if( currentCopter != null)
			mOverlays.remove(currentCopter);
		
	    mOverlays.add(overlay);
	    currentCopter = overlay;

		populate();
	    
	}
	
	@Override
	protected boolean onTap(int index) {
		
	  OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  String msg = (double)item.getPoint().getLatitudeE6()/1000000.0 + " " +
	  			   (double)item.getPoint().getLongitudeE6()/1000000.0;
	  
	  dialog.setMessage(msg);			
	  dialog.show();

	  return true;
	  
	}
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
		
	}

	@Override
	public int size() {
		return mOverlays.size();
		
	}

}
