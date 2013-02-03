package com.MAVLink.Messages.common;

import com.MAVLink.Messages.IMAVLinkMessage;

public class msg_command_long extends IMAVLinkMessage{

	public msg_command_long(){ messageType = MAVLINK_MSG_ID_COMMAND_LONG; }

	public static final int MAVLINK_MSG_ID_COMMAND_LONG = 76;

	private static final long serialVersionUID = MAVLINK_MSG_ID_COMMAND_LONG;

	public int target_system; ///< System which should execute the command
	public int target_component; ///< Component which should execute the command, 0 for all components
	public int command; ///< Command ID, as defined by MAV_CMD enum.
	public int confirmation; ///< 0: First transmission of this command. 1-255: Confirmation transmissions (e.g. for kill command)
	public float param1; ///< Parameter 1, as defined by MAV_CMD enum.
	public float param2; ///< Parameter 2, as defined by MAV_CMD enum.
	public float param3; ///< Parameter 3, as defined by MAV_CMD enum.
	public float param4; ///< Parameter 4, as defined by MAV_CMD enum.
	public float param5; ///< Parameter x
	public float param6; ///< Parameter y
	public float param7; ///< Parameter z
}