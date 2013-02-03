package com.bvcode.ncopter.AC1Data;

import com.MAVLink.Messages.IMAVLinkMessage;

public class GPSPids extends IMAVLinkMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1317604964597716776L;
	public double[] rollPID = new double[3];
	public double[] pitchPID = new double[3];
	public double maxAngle;
	public double geog_correctionFactor;
	
}
