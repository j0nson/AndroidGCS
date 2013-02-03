package com.bvcode.ncopter.AC1Data;

import com.MAVLink.Messages.IMAVLinkMessage;

public class SensorOffsets extends IMAVLinkMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5832875363231303524L;
	public int[] lastOffsets;
	public int roll, pitch, z;

}
