package com.bvcode.ncopter.gps;

import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.MAVLink.Messages.common.msg_waypoint;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class GuidedModeOverlay extends ItemizedOverlay<OverlayItem> {
	GPSActivity context;
	private MapView map;
	private GestureDetector mGestureDetector;
	GeoPoint gotoPoint = null;
	
	public GuidedModeOverlay(Drawable defaultMarker, GPSActivity context,	MapView map) {
		super(boundCenterBottom(defaultMarker));
		this.map = map;
		this.context = context;
		
		mGestureDetector = new GestureDetector(context, new LearnGestureListener());
		populate();
		
	}

	@Override
	protected OverlayItem createItem(int i) {

		if( gotoPoint != null)
			return new OverlayItem(gotoPoint, "Guided Mode", "Target");
		
		return null;
		
	}

	@Override
	public int size() {
		if( gotoPoint == null)
			return 0;
		
		return 1;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		mGestureDetector.onTouchEvent(event);
		
		return super.onTouchEvent(event, mapView);
		
	}
	
	class LearnGestureListener extends GestureDetector.SimpleOnGestureListener {
		
	
		@Override
		public boolean onSingleTapUp(MotionEvent ev) {
			return true;
		}

		@Override
		public void onShowPress(MotionEvent ev) {
		
		}

		@Override
		public void onLongPress(MotionEvent ev) {
			GeoPoint pt = map.getProjection().fromPixels((int)ev.getX(), (int)ev.getY());

			msg_waypoint msg = new msg_waypoint();
			msg.current = 2; // A guided packet
			msg.x = (float)pt.getLatitudeE6() / 1000000.0f;
			msg.y = (float)pt.getLongitudeE6() / 1000000.0f;
			msg.z = 0;

			context.setGuidedTarget(msg, true);
			
			setGotoPoint(pt);
    		
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,	float distanceX, float distanceY) {
			return true;
		}

		@Override
		public boolean onDown(MotionEvent ev) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
			return true;
		}
	}

	public void setGotoPoint(GeoPoint geoPoint) {
		gotoPoint = geoPoint;
		populate();
		
	}
}