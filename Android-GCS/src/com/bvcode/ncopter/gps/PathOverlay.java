package com.bvcode.ncopter.gps;

import java.util.LinkedList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class PathOverlay extends Overlay{

	LinkedList<GeoPoint> mPointTrack = new LinkedList<GeoPoint>();
	
	Point start = new Point(), end = new Point();
	
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		
		Projection proj = mapView.getProjection();
		
		if( mPointTrack.size() <= 1)
			return;
		
		// Draw the rest
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(false);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		Path path = new Path();
		GeoPoint p;
		
		for ( int i = 0; i < mPointTrack.size(); i++) {
			
			p = mPointTrack.get(i);
			proj.toPixels(p, end);
			
			if( i == 0)
				path.moveTo(end.x, end.y);
			else
				path.lineTo(end.x, end.y);
			
		}

		canvas.drawPath(path, paint);
		super.draw(canvas, mapView, shadow);
		
	}

	public boolean addNewLocation(GeoPoint point, int altitude) {
		if( mPointTrack.size() == 0){
			mPointTrack.add(point);
			return true;
		
		}
		
		// Have we moved enough to bother recording??
		GeoPoint lastPoint = mPointTrack.getLast();
		
		float[] results = new float[1];
		Location.distanceBetween( // in meters
				lastPoint.getLatitudeE6()/1000000.0, lastPoint.getLongitudeE6()/1000000.0,
				point.getLatitudeE6()/1000000.0, point.getLongitudeE6()/1000000.0,
				results);
		
		boolean res = false;
		if( results[0] > 3 ){
			mPointTrack.add(point);
			res = true;
			
		}
		
		if( mPointTrack.size() > 200)
			mPointTrack.remove(0);
	
		return res;
		
	}

}
