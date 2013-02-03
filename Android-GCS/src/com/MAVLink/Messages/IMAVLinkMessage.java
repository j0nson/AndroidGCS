package com.MAVLink.Messages;

import java.io.Serializable;

public abstract class IMAVLinkMessage implements Serializable {
	/**
	 *  Simply a common interface for all MAVLink Messages
	 */
	
	/**
	 * Indicates the type of message :)
	 * -1 for AC1 Protocol, otherwise its a MAVLink packet
	 */
	public int messageType = -1;
	public int sysID = -1;
	public int componentID = -1;
	
}
