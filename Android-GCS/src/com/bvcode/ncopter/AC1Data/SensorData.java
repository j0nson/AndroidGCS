package com.bvcode.ncopter.AC1Data;

import com.MAVLink.Messages.IMAVLinkMessage;

public class SensorData extends IMAVLinkMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2014956483832331236L;
	public int pitch, roll, z;
	
}
