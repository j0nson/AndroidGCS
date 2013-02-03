package com.bvcode.ncopter.AC1Data;

import com.MAVLink.Messages.IMAVLinkMessage;

public class AltitudePids extends IMAVLinkMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = -899563155099653069L;
	public double[] altitude;
}
