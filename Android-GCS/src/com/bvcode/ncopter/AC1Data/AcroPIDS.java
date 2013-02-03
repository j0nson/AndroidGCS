package com.bvcode.ncopter.AC1Data;

import com.MAVLink.Messages.IMAVLinkMessage;

public class AcroPIDS extends IMAVLinkMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1634418030794680492L;
	public double[] rollPID = new double[3];
	public double[] pitchPID = new double[3];
	public double[] yawPID = new double[3];
	public double xMitFactor;
}
