package com.bvcode.ncopter.AC1Data;

import com.MAVLink.Messages.IMAVLinkMessage;

public class GPSData extends IMAVLinkMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8400227868354721806L;
	public int latitude;
	public int longitude;
	public int altitude;
	public int time;
	
	public double groundSpeed, groundCourse;
	public int fix, num_sats;
	
}
