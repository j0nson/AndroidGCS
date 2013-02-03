package com.bvcode.ncopter.AC1Data;

import com.MAVLink.Messages.IMAVLinkMessage;

public class StablePids extends IMAVLinkMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3599506416931436307L;
	public double[] rollPID = new double[3];
	public double[] pitchPID = new double[3];
	public double[] yawPID = new double[3];
	public double KP_rate;
	public double magnetometer;
	
}
