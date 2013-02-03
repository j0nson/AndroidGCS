package com.MAVLink.Messages.common;

import com.MAVLink.Messages.IMAVLinkMessage;

public class msg_waypoint_reached extends IMAVLinkMessage{

	public msg_waypoint_reached(){ messageType = MAVLINK_MSG_ID_WAYPOINT_REACHED; }

	public static final int MAVLINK_MSG_ID_WAYPOINT_REACHED = 46;

	private static final long serialVersionUID = MAVLINK_MSG_ID_WAYPOINT_REACHED;

	public int seq; ///< Sequence

}