package com.bvcode.ncopter.mission;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.common.msg_waypoint;
import com.bvcode.ncopter.R;
import com.bvcode.ncopter.widgets.MissionWidget;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class MissionOverlay extends ItemizedOverlay<OverlayItem> {
	private Context mContext;
	MissionActivity fContext;
	private MissionWidget currentMissionWidget = null;
	// Dragging stuff.
	private Drawable marker = null;
	private msg_waypoint inDrag = null;
	private ImageView dragImage = null;
	private int xDragImageOffset = 0;
	private int yDragImageOffset = 0;
	private int xDragTouchOffset = 0;
	private int yDragTouchOffset = 0;

	private MapView map;

	public boolean enableEdit = false;
	private GestureDetector mGestureDetector;
	private long lastWaypointAdd = 0;

	public MissionOverlay(Drawable defaultMarker, MissionActivity context,	MapView map) {
		
		super(boundCenterBottom(defaultMarker));
		
		mContext = context;
		this.map = map;
		this.marker = defaultMarker;
		
		
		MapActivity parent = (MapActivity) context;

		mGestureDetector = new GestureDetector(context, new LearnGestureListener());

		// Setup for dragging
		this.dragImage = (ImageView) parent.findViewById(R.id.drag);
		xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth() / 2;
		yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();

		populate();
		
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,	long when) {
		synchronized (MissionActivity.waypoints) {
			
			Projection projection = mapView.getProjection();
			if (shadow == false) {
	
				Paint paint = new Paint();
				paint.setColor(Color.YELLOW);
				paint.setAntiAlias(true);
				paint.setStrokeWidth(8);
				msg_waypoint e;
	
				// skip empty bubbles
				// skip home
				for (int i = 2; i < MissionActivity.getWaypointSize(); i++) {
	
					e = MissionActivity.getWaypoint(i - 1);
					// Lets assume we always start at a "good" point.
					if( e.x == 0 && e.y == 0) // so this point is not good
						continue;
					
					GeoPoint p1 = new GeoPoint((int) (e.x * 1000000.0),	(int) (e.y * 1000000.0));
					Point point = new Point();
					projection.toPixels(p1, point);
	
					// Grab the next good waypoint. This skips over non-gps points.
					// They will get drawn, but at 0,0 long/latitude.
					e = MissionActivity.getWaypoint(i);
					while( e.x == 0 && e.y == 0){
						i++;
						if( i >= MissionActivity.getWaypointSize())
							break;
						e = MissionActivity.getWaypoint(i);
					}
						
					p1 = new GeoPoint((int) (e.x * 1000000.0), (int) (e.y * 1000000.0));
					Point point2 = new Point();
					projection.toPixels(p1, point2);
	
					canvas.drawLine((float) point.x, (float) point.y, (float) point2.x, (float) point2.y, paint);
	
				}
			}
			return super.draw(canvas, mapView, shadow, when);

		}
	}
	
	@Override
	protected boolean onTap(int index) {

		if(System.currentTimeMillis() - lastWaypointAdd > 1000){
			Dialog dialog = new Dialog(mContext);
	        currentMissionWidget = new MissionWidget(mContext, null);
	        currentMissionWidget.setPacket( MissionActivity.getWaypoint(index));
	        dialog.setContentView(currentMissionWidget);
	        if ( index == 0 ){
				dialog.setTitle("Home");
			}else{ 
				dialog.setTitle("Waypoint "+ index + " Edit");
			}
	        dialog.setCancelable(true);
			dialog.show();
			dialog.setOnDismissListener(new OnDismissListener() {
		        public void onDismiss(final DialogInterface arg0) {
		        	if(currentMissionWidget != null){
		    			currentMissionWidget.saveData();
		    			currentMissionWidget = null;
		    		}
		        }
		    });
			
			//final msg_waypoint p = MissionActivity.getWaypoint(index);
			//AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			//if ( p.seq == 0 ){
			//	dialog.setTitle("Home");
			//}else{ 
			//	dialog.setTitle("Mission Item");
			//}
			//if ( p.seq == 0 ){
			//	dialog.setMessage("Waypoint " + p.seq + "\nType: " + MAVLink.getMavCmd(p.command) + "\nElevation: " + p.z + "m");
			//}else{
			//	dialog.setMessage("Waypoint " + p.seq + "\nType: " + MAVLink.getMavCmd(p.command) + "\nAltitude: " + p.z + "m");
			//	dialog.setPositiveButton("Goto Now", new DialogInterface.OnClickListener() {
		    //        public void onClick(DialogInterface arg0, int arg1 ) {
		    //        	//Log.d("On Click:", "" + p.seq);  
		    //        	((MissionActivity)mContext).setCurrentWP(p.seq);
		    //        }
		    //    });
			//}
			//dialog.setNegativeButton("Dismiss", null);
			//dialog.show();
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {

		if (enableEdit){
			final int action = event.getAction();
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			boolean result = false;
	
			if (action == MotionEvent.ACTION_DOWN) {
				for( int i = 0; i < MissionActivity.getWaypointSize(); i++){
					msg_waypoint e = MissionActivity.getWaypoint(i);
					Point p = new Point(0, 0);
	
					GeoPoint point = new GeoPoint((int) (e.x * 1000000.0), (int) (e.y * 1000000.0));
					String title = "Mission";
					String description = "Waypoint " + e.seq + "\nType: " + MAVLink.getMavCmd(e.command) + "\nAltitude: " + e.z + "m";
					
					if (i == 0 ){
						title = "Mission";
						description = "Type: " + MAVLink.getMavCmd(e.command) + "\nElevation: " + e.z + "m";
					}
					OverlayItem item = new OverlayItem(point, title, description);
					map.getProjection().toPixels(point, p);
	
					if (hitTest(item, marker, x - p.x, y - p.y)) {
						result = true;
						inDrag = e;
	
						populate();
	
						setDragImagePosition(p.x, p.y);
						dragImage.setVisibility(View.VISIBLE);
	
						xDragTouchOffset = x - p.x;
						yDragTouchOffset = y - p.y;
	
						return result;
	
					}
				}
	
			} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
				setDragImagePosition(x, y);
				result = true;
	
			} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
				dragImage.setVisibility(View.GONE);
	
				GeoPoint pt = map.getProjection().fromPixels(
						x - xDragTouchOffset,
						y - yDragTouchOffset);
	
				inDrag.x = pt.getLatitudeE6() / 1000000.0f;
				inDrag.y = pt.getLongitudeE6() / 1000000.0f;
	
				populate();
	
				inDrag = null;
				result = true;
			}
	
			return (result || super.onTouchEvent(event, mapView));
		}
		
		
		
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
			//set new waypoint
			vibe(50);
			
			GeoPoint pt = map.getProjection().fromPixels((int)ev.getX(), (int)ev.getY());
			
			msg_waypoint msg = new msg_waypoint();
    		msg_waypoint last = MissionActivity.getLastWaypoint();
    		if( last != null){
	    		msg.seq = last.seq+1;
	    		
    		}else{
	    		msg.seq = 0;
	    		
	    	}
    		
    		msg.x = pt.getLatitudeE6()/1000000.0f;
    		msg.y = pt.getLongitudeE6()/1000000.0f;
    		msg.z = 75; //TODO: currently in meters, set pop up to choose altitude
    		if ( msg.seq == 0 ){
    			msg.z = (float) ((MissionActivity)mContext).getAltitude(pt.getLatitudeE6()/1000000.0f, pt.getLongitudeE6()/1000000.0f);
    			msg.frame = MAVLink.MAV_FRAME.MAV_FRAME_GLOBAL;
    		}else{
    			msg.frame = MAVLink.MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
    		}
    		//TODO: set pop up to choose mission type, and parameters
    		msg.command = 16;//MAVLink.MAV_CMD.MAV_CMD_NAV_WAYPOINT;
    		if ( msg.seq == 1 ){
    			msg.current = 1;
    		}
    		msg.param1 = msg.param2 = msg.param3 = msg.param4 = 0; 
    		msg.autocontinue = 1;
    		
    		MissionActivity.add(msg);			
			notifyDataChanged();
			map.invalidate();
			
			lastWaypointAdd = System.currentTimeMillis();
    		
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,	float distanceX, float distanceY) {
			return true;
		}

		@Override
		public boolean onDown(MotionEvent ev) {
			return true;
		}
		public void vibe(int len){
			Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(len);
			
		}
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
			return true;
		}
	}
	
	private void setDragImagePosition(int x, int y) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage.getLayoutParams();

		lp.setMargins(x - xDragImageOffset - xDragTouchOffset, 
					  y - yDragImageOffset - yDragTouchOffset, 0, 0);
		dragImage.setLayoutParams(lp);
	}

	@Override
	protected OverlayItem createItem(int i) {
		msg_waypoint msg = MissionActivity.getWaypoint(i);
		
		GeoPoint point = new GeoPoint((int) (msg.x * 1000000.0), (int) (msg.y * 1000000.0));
		String title = "Mission";
		if ( i == 0 ){
			title = "Home";
		}
		return new OverlayItem(point, title, "Waypoint " + msg.seq + "\nAltitude: " + msg.z + "m");

	}
	
	@Override
	public int size() {
		return MissionActivity.getWaypointSize();

	}

	public void notifyDataChanged() {
		setLastFocusedIndex(-1);
		populate();

	}
}

