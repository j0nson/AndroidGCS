package com.bvcode.ncopter.AC1Data;

import com.MAVLink.Messages.IMAVLinkMessage;

public class FlightData extends IMAVLinkMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4415739332852026673L;
	public double gyroX, gyroY, gyroYaw;
	public double accelX, accelY, accelZ;
	public double magX, magY, magZ;
	
}
