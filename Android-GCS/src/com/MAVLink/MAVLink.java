package com.MAVLink;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.MAVLink.Messages.IMAVLinkMessage;

public class MAVLink {

        public static int CURRENT_SYSID = 0;
        public static int ARDUCOPTER_COMPONENT_ID = 0;

        public static int MAVLINK_ARDU_MEGA_SYSID = 1;
        public static int MAVLINK_ARDU_COPTER_MEGA_SYSID = 7;

        public MAVLink(){}


        public static char[] StringNameToInt(String valueName){
        		return valueName.toCharArray();
                //char[] name = new char[15];
                //for( int i = 0; i < valueName.length(); i++)
                //        name[i] = valueName.charAt(i);
                //return name;
        }

     public static String convertIntNameToString(char[] param_id) {
            char valueName[] = new char[ param_id.length]; 
             
             // Convert the int array to a char[] and then a string later on
             int i = 0;
             for(; i < param_id.length; i++){
                     valueName[i] = /*(char)*/param_id[i];
                    if( valueName[i] == 0)
                            break;

            }
            return new String(valueName, 0, i);

     }
      /**
       * Take the next byte from the input stream (from the drone)
       * If the byte finishes the packet, then return it
       * 
       * @param b Next byte in the stream
       * @return NULL if not a valid packet, a message object otherwise.
        */
      public static native IMAVLinkMessage receivedByte(byte b);

     /**
      * Packs up a message and checksums it to be sent out to the drone
      * @param msg The message to be sent
      * @return the byte stream to be sent, or null if the message was not recognized.
      */
     public static native byte[] createMessage(IMAVLinkMessage msg);

    /**
     * Must be called to initialize some variables in the JNI library
     */
       private static native void init();

    static {
            System.loadLibrary("jmavlink");
            init();

    }
    
	public class MAV_AUTOPILOT{
		public final static int MAV_AUTOPILOT_GENERIC=0; //, * Generic autopilot, full support for everything | *
		public final static int MAV_AUTOPILOT_PIXHAWK=1; //, * PIXHAWK autopilot, http:
		public final static int MAV_AUTOPILOT_SLUGS=2; //, * SLUGS autopilot, http:
		public final static int MAV_AUTOPILOT_ARDUPILOTMEGA=3; //, * ArduPilotMega 
		public final static int MAV_AUTOPILOT_OPENPILOT=4; //, * OpenPilot, http:
		public final static int MAV_AUTOPILOT_GENERIC_WAYPOINTS_ONLY=5; //, * Generic autopilot only supporting simple waypoints | *
		public final static int MAV_AUTOPILOT_GENERIC_WAYPOINTS_AND_SIMPLE_NAVIGATION_ONLY=6; //, * Generic autopilot supporting waypoints and other simple navigation commands | *
		public final static int MAV_AUTOPILOT_GENERIC_MISSION_FULL=7; //, * Generic autopilot supporting the full mission command set | *
		public final static int MAV_AUTOPILOT_INVALID=8; //, * No valid autopilot, e.g. a GCS or other MAVLink component | *
		public final static int MAV_AUTOPILOT_PPZ=9; //, * PPZ UAV - http:
		public final static int MAV_AUTOPILOT_UDB=10; //, * UAV Dev Board | *
		public final static int MAV_AUTOPILOT_FP=11; //, * FlexiPilot | *
		public final static int MAV_AUTOPILOT_ENUM_END=12; //, *  | *
	}

	public class MAV_MODE_FLAG{
		public final static int MAV_MODE_FLAG_CUSTOM_MODE_ENABLED=1; //, * 0b00000001 Reserved for future use. | *
		public final static int MAV_MODE_FLAG_TEST_ENABLED=2; //, * 0b00000010 system has a test mode enabled. This flag is intended for temporary system tests and should not be used for stable implementations. | *
		public final static int MAV_MODE_FLAG_AUTO_ENABLED=4; //, * 0b00000100 autonomous mode enabled, system finds its own goal positions. Guided flag can be set or not, depends on the actual implementation. | *
		public final static int MAV_MODE_FLAG_GUIDED_ENABLED=8; //, * 0b00001000 guided mode enabled, system flies MISSIONs 
		public final static int MAV_MODE_FLAG_STABILIZE_ENABLED=16; //, * 0b00010000 system stabilizes electronically its attitude (and optionally position). It needs however further control inputs to move around. | *
		public final static int MAV_MODE_FLAG_HIL_ENABLED=32; //, * 0b00100000 hardware in the loop simulation. All motors 
		public final static int MAV_MODE_FLAG_MANUAL_INPUT_ENABLED=64; //, * 0b01000000 remote control input is enabled. | *
		public final static int MAV_MODE_FLAG_SAFETY_ARMED=128; //, * 0b10000000 MAV safety set to armed. Motors are enabled 
		public final static int MAV_MODE_FLAG_ENUM_END=129; //, *  | *
	}

	public class MAV_MODE_FLAG_DECODE_POSITION{
		public final static int MAV_MODE_FLAG_DECODE_POSITION_CUSTOM_MODE=1; //, * Eighth bit: 00000001 | *
		public final static int MAV_MODE_FLAG_DECODE_POSITION_TEST=2; //, * Seventh bit: 00000010 | *
		public final static int MAV_MODE_FLAG_DECODE_POSITION_AUTO=4; //, * Sixth bit:   00000100 | *
		public final static int MAV_MODE_FLAG_DECODE_POSITION_GUIDED=8; //, * Fifth bit:  00001000 | *
		public final static int MAV_MODE_FLAG_DECODE_POSITION_STABILIZE=16; //, * Fourth bit: 00010000 | *
		public final static int MAV_MODE_FLAG_DECODE_POSITION_HIL=32; //, * Third bit:  00100000 | *
		public final static int MAV_MODE_FLAG_DECODE_POSITION_MANUAL=64; //, * Second bit: 01000000 | *
		public final static int MAV_MODE_FLAG_DECODE_POSITION_SAFETY=128; //, * First bit:  10000000 | *
		public final static int MAV_MODE_FLAG_DECODE_POSITION_ENUM_END=129; //, *  | *
	}

	public class MAV_GOTO{
		public final static int MAV_GOTO_DO_HOLD=0; //, * Hold at the current position. | *
		public final static int MAV_GOTO_DO_CONTINUE=1; //, * Continue with the next item in mission execution. | *
		public final static int MAV_GOTO_HOLD_AT_CURRENT_POSITION=2; //, * Hold at the current position of the system | *
		public final static int MAV_GOTO_HOLD_AT_SPECIFIED_POSITION=3; //, * Hold at the position specified in the parameters of the DO_HOLD action | *
		public final static int MAV_GOTO_ENUM_END=4; //, *  | *
	}

	public class MAV_MODE{
		public final static int MAV_MODE_PREFLIGHT=0; //, * System is not ready to fly, booting, calibrating, etc. No flag is set. | *
		public final static int MAV_MODE_MANUAL_DISARMED=64; //, * System is allowed to be active, under manual (RC) control, no stabilization | *
		public final static int MAV_MODE_TEST_DISARMED=66; //, * UNDEFINED mode. This solely depends on the autopilot - use with caution, intended for developers only. | *
		public final static int MAV_MODE_STABILIZE_DISARMED=80; //, * System is allowed to be active, under assisted RC control. | *
		public final static int MAV_MODE_GUIDED_DISARMED=88; //, * System is allowed to be active, under autonomous control, manual setpoint | *
		public final static int MAV_MODE_AUTO_DISARMED=92; //, * System is allowed to be active, under autonomous control and navigation (the trajectory is decided onboard and not pre-programmed by MISSIONs) | *
		public final static int MAV_MODE_MANUAL_ARMED=192; //, * System is allowed to be active, under manual (RC) control, no stabilization | *
		public final static int MAV_MODE_TEST_ARMED=194; //, * UNDEFINED mode. This solely depends on the autopilot - use with caution, intended for developers only. | *
		public final static int MAV_MODE_STABILIZE_ARMED=208; //, * System is allowed to be active, under assisted RC control. | *
		public final static int MAV_MODE_GUIDED_ARMED=216; //, * System is allowed to be active, under autonomous control, manual setpoint | *
		public final static int MAV_MODE_AUTO_ARMED=220; //, * System is allowed to be active, under autonomous control and navigation (the trajectory is decided onboard and not pre-programmed by MISSIONs) | *
		public final static int MAV_MODE_ENUM_END=221; //, *  | *
	}

	public class MAV_STATE{
		public final static int MAV_STATE_UNINIT=0; //, * Uninitialized system, state is unknown. | *
		public final static int MAV_STATE_BOOT=1; //, * System is booting up. | *
		public final static int MAV_STATE_CALIBRATING=2; //, * System is calibrating and not flight-ready. | *
		public final static int MAV_STATE_STANDBY=3; //, * System is grounded and on standby. It can be launched any time. | *
		public final static int MAV_STATE_ACTIVE=4; //, * System is active and might be already airborne. Motors are engaged. | *
		public final static int MAV_STATE_CRITICAL=5; //, * System is in a non-normal flight mode. It can however still navigate. | *
		public final static int MAV_STATE_EMERGENCY=6; //, * System is in a non-normal flight mode. It lost control over parts or over the whole airframe. It is in mayday and going down. | *
		public final static int MAV_STATE_POWEROFF=7; //, * System just initialized its power-down sequence, will shut down now. | *
		public final static int MAV_STATE_ENUM_END=8; //, *  | *
	}

	public class MAV_TYPE{
		public final static int MAV_TYPE_GENERIC=0; //, * Generic micro air vehicle. | *
		public final static int MAV_TYPE_FIXED_WING=1; //, * Fixed wing aircraft. | *
		public final static int MAV_TYPE_QUADROTOR=2; //, * Quadrotor | *
		public final static int MAV_TYPE_COAXIAL=3; //, * Coaxial helicopter | *
		public final static int MAV_TYPE_HELICOPTER=4; //, * Normal helicopter with tail rotor. | *
		public final static int MAV_TYPE_ANTENNA_TRACKER=5; //, * Ground installation | *
		public final static int MAV_TYPE_GCS=6; //, * Operator control unit 
		public final static int MAV_TYPE_AIRSHIP=7; //, * Airship, controlled | *
		public final static int MAV_TYPE_FREE_BALLOON=8; //, * Free balloon, uncontrolled | *
		public final static int MAV_TYPE_ROCKET=9; //, * Rocket | *
		public final static int MAV_TYPE_GROUND_ROVER=10; //, * Ground rover | *
		public final static int MAV_TYPE_SURFACE_BOAT=11; //, * Surface vessel, boat, ship | *
		public final static int MAV_TYPE_SUBMARINE=12; //, * Submarine | *
		public final static int MAV_TYPE_HEXAROTOR=13; //, * Hexarotor | *
		public final static int MAV_TYPE_OCTOROTOR=14; //, * Octorotor | *
		public final static int MAV_TYPE_TRICOPTER=15; //, * Octorotor | *
		public final static int MAV_TYPE_FLAPPING_WING=16; //, * Flapping wing | *
		public final static int MAV_TYPE_ENUM_END=17; //, *  | *
	}

	public class MAV_COMPONENT{
		public final static int MAV_COMP_ID_ALL=0; //, *  | *
		public final static int MAV_COMP_ID_CAMERA=100; //, *  | *
		public final static int MAV_COMP_ID_SERVO1=140; //, *  | *
		public final static int MAV_COMP_ID_SERVO2=141; //, *  | *
		public final static int MAV_COMP_ID_SERVO3=142; //, *  | *
		public final static int MAV_COMP_ID_SERVO4=143; //, *  | *
		public final static int MAV_COMP_ID_SERVO5=144; //, *  | *
		public final static int MAV_COMP_ID_SERVO6=145; //, *  | *
		public final static int MAV_COMP_ID_SERVO7=146; //, *  | *
		public final static int MAV_COMP_ID_SERVO8=147; //, *  | *
		public final static int MAV_COMP_ID_SERVO9=148; //, *  | *
		public final static int MAV_COMP_ID_SERVO10=149; //, *  | *
		public final static int MAV_COMP_ID_SERVO11=150; //, *  | *
		public final static int MAV_COMP_ID_SERVO12=151; //, *  | *
		public final static int MAV_COMP_ID_SERVO13=152; //, *  | *
		public final static int MAV_COMP_ID_SERVO14=153; //, *  | *
		public final static int MAV_COMP_ID_MAPPER=180; //, *  | *
		public final static int MAV_COMP_ID_MISSIONPLANNER=190; //, *  | *
		public final static int MAV_COMP_ID_PATHPLANNER=195; //, *  | *
		public final static int MAV_COMP_ID_IMU=200; //, *  | *
		public final static int MAV_COMP_ID_IMU_2=201; //, *  | *
		public final static int MAV_COMP_ID_IMU_3=202; //, *  | *
		public final static int MAV_COMP_ID_GPS=220; //, *  | *
		public final static int MAV_COMP_ID_UDP_BRIDGE=240; //, *  | *
		public final static int MAV_COMP_ID_UART_BRIDGE=241; //, *  | *
		public final static int MAV_COMP_ID_SYSTEM_CONTROL=250; //, *  | *
		public final static int MAV_COMPONENT_ENUM_END=251; //, *  | *
	}

	public class MAV_FRAME{
		public final static int MAV_FRAME_GLOBAL=0; //, * Global coordinate frame, WGS84 coordinate system. First value 
		public final static int MAV_FRAME_LOCAL_NED=1; //, * Local coordinate frame, Z-up (x: north, y: east, z: down). | *
		public final static int MAV_FRAME_MISSION=2; //, * NOT a coordinate frame, indicates a mission command. | *
		public final static int MAV_FRAME_GLOBAL_RELATIVE_ALT=3; //, * Global coordinate frame, WGS84 coordinate system, relative altitude over ground with respect to the home position. First value 
		public final static int MAV_FRAME_LOCAL_ENU=4; //, * Local coordinate frame, Z-down (x: east, y: north, z: up) | *
		//public final static int MAV_FRAME_ENUM_END=5; //, *  | *
	}

	public class MAVLINK_DATA_STREAM_TYPE{
		public final static int MAVLINK_DATA_STREAM_IMG_JPEG=1; //, *  | *
		public final static int MAVLINK_DATA_STREAM_IMG_BMP=2; //, *  | *
		public final static int MAVLINK_DATA_STREAM_IMG_RAW8U=3; //, *  | *
		public final static int MAVLINK_DATA_STREAM_IMG_RAW32U=4; //, *  | *
		public final static int MAVLINK_DATA_STREAM_IMG_PGM=5; //, *  | *
		public final static int MAVLINK_DATA_STREAM_IMG_PNG=6; //, *  | *
		public final static int MAVLINK_DATA_STREAM_TYPE_ENUM_END=7; //, *  | *
	}

	public class MAV_DATA_STREAM{
		public final static int MAV_DATA_STREAM_ALL=0; //, * Enable all data streams | *
		public final static int MAV_DATA_STREAM_RAW_SENSORS=1; //, * Enable IMU_RAW, GPS_RAW, GPS_STATUS packets. | *
		public final static int MAV_DATA_STREAM_EXTENDED_STATUS=2; //, * Enable GPS_STATUS, CONTROL_STATUS, AUX_STATUS | *
		public final static int MAV_DATA_STREAM_RC_CHANNELS=3; //, * Enable RC_CHANNELS_SCALED, RC_CHANNELS_RAW, SERVO_OUTPUT_RAW | *
		public final static int MAV_DATA_STREAM_RAW_CONTROLLER=4; //, * Enable ATTITUDE_CONTROLLER_OUTPUT, POSITION_CONTROLLER_OUTPUT, NAV_CONTROLLER_OUTPUT. | *
		public final static int MAV_DATA_STREAM_POSITION=6; //, * Enable LOCAL_POSITION, GLOBAL_POSITION
		public final static int MAV_DATA_STREAM_EXTRA1=10; //, * Dependent on the autopilot | *
		public final static int MAV_DATA_STREAM_EXTRA2=11; //, * Dependent on the autopilot | *
		public final static int MAV_DATA_STREAM_EXTRA3=12; //, * Dependent on the autopilot | *
		public final static int MAV_DATA_STREAM_ENUM_END=13; //, *  | *
	}

	public class MAV_ROI{
		public final static int MAV_ROI_NONE=0; //, * No region of interest. | *
		public final static int MAV_ROI_WPNEXT=1; //, * Point toward next MISSION. | *
		public final static int MAV_ROI_WPINDEX=2; //, * Point toward given MISSION. | *
		public final static int MAV_ROI_LOCATION=3; //, * Point toward fixed location. | *
		public final static int MAV_ROI_TARGET=4; //, * Point toward of given id. | *
		public final static int MAV_ROI_ENUM_END=5; //, *  | *
	}

	public class MAV_CMD{
		public final static int MAV_CMD_NAV_WAYPOINT=16; /* Navigate to MISSION. |Hold time in decimal seconds. (ignored by fixed wing, time to stay at MISSION for rotary wing)| Acceptance radius in meters (if the sphere with this radius is hit, the MISSION counts as reached)| 0 to pass through the WP, if > 0 radius in meters to pass by WP. Positive value for clockwise orbit, negative value for counter-clockwise orbit. Allows trajectory control.| Desired yaw angle at MISSION (rotary wing)| Latitude| Longitude| Altitude|  */
		public final static int MAV_CMD_NAV_LOITER_UNLIM=17; /* Loiter around this MISSION an unlimited amount of time |Empty| Empty| Radius around MISSION, in meters. If positive loiter clockwise, else counter-clockwise| Desired yaw angle.| Latitude| Longitude| Altitude|  */
		public final static int MAV_CMD_NAV_LOITER_TURNS=18; /* Loiter around this MISSION for X turns |Turns| Empty| Radius around MISSION, in meters. If positive loiter clockwise, else counter-clockwise| Desired yaw angle.| Latitude| Longitude| Altitude|  */
		public final static int MAV_CMD_NAV_LOITER_TIME=19; /* Loiter around this MISSION for X seconds |Seconds (decimal)| Empty| Radius around MISSION, in meters. If positive loiter clockwise, else counter-clockwise| Desired yaw angle.| Latitude| Longitude| Altitude|  */
		public final static int MAV_CMD_NAV_RETURN_TO_LAUNCH=20; /* Return to launch location |Empty| Empty| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_NAV_LAND=21; /* Land at location |Empty| Empty| Empty| Desired yaw angle.| Latitude| Longitude| Altitude|  */
		public final static int MAV_CMD_NAV_TAKEOFF=22; /* Takeoff from ground / hand |Minimum pitch (if airspeed sensor present), desired pitch without sensor| Empty| Empty| Yaw angle (if magnetometer present), ignored without magnetometer| Latitude| Longitude| Altitude|  */
		public final static int MAV_CMD_NAV_ROI=80; /* Sets the region of interest (ROI) for a sensor set or the vehicle itself. This can then be used by the vehicles control system to control the vehicle attitude and the attitude of various sensors such as cameras. |Region of intereset mode. (see MAV_ROI enum)| MISSION index/ target ID. (see MAV_ROI enum)| ROI index (allows a vehicle to manage multiple ROI's)| Empty| x the location of the fixed ROI (see MAV_FRAME)| y| z|  */
		//public final static int MAV_CMD_NAV_PATHPLANNING=81; /* Control autonomous path planning on the MAV. |0: Disable local obstacle avoidance / local path planning (without resetting map), 1: Enable local path planning, 2: Enable and reset local path planning| 0: Disable full path planning (without resetting map), 1: Enable, 2: Enable and reset map/occupancy grid, 3: Enable and reset planned route, but not occupancy grid| Empty| Yaw angle at goal, in compass degrees, [0..360]| Latitude/X of goal| Longitude/Y of goal| Altitude/Z of goal|  */
		//public final static int MAV_CMD_NAV_LAST=95; /* NOP - This command is only used to mark the upper limit of the NAV/ACTION commands in the enumeration |Empty| Empty| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_CONDITION_DELAY=112; /* Delay mission state machine. |Delay in seconds (decimal)| Empty| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_CONDITION_CHANGE_ALT=113; /* Ascend/descend at rate.  Delay mission state machine until desired altitude reached. |Descent / Ascend rate (m/s)| Empty| Empty| Empty| Empty| Empty| Finish Altitude|  */
		public final static int MAV_CMD_CONDITION_DISTANCE=114; /* Delay mission state machine until within desired distance of next NAV point. |Distance (meters)| Empty| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_CONDITION_YAW=115; /* Reach a certain target angle. |target angle: [0-360], 0 is north| speed during yaw change:[deg per second]| direction: negative: counter clockwise, positive: clockwise [-1,1]| relative offset or absolute angle: [ 1,0]| Empty| Empty| Empty|  */
		//public final static int MAV_CMD_CONDITION_LAST=159; /* NOP - This command is only used to mark the upper limit of the CONDITION commands in the enumeration |Empty| Empty| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_SET_MODE=176; /* Set system mode. |Mode, as defined by ENUM MAV_MODE| Empty| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_JUMP=177; /* Jump to the desired command in the mission list.  Repeat this action only the specified number of times |Sequence number| Repeat count| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_CHANGE_SPEED=178; /* Change speed and/or throttle set points. |Speed type (0=Airspeed, 1=Ground Speed)| Speed  (m/s, -1 indicates no change)| Throttle  ( Percent, -1 indicates no change)| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_SET_HOME=179; /* Changes the home location either to the current location or a specified location. |Use current (1=use current location, 0=use specified location)| Empty| Empty| Empty| Latitude| Longitude| Altitude|  */
		public final static int MAV_CMD_DO_SET_PARAMETER=180; /* Set a system parameter.  Caution!  Use of this command requires knowledge of the numeric enumeration value of the parameter. |Parameter number| Parameter value| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_SET_RELAY=181; /* Set a relay to a condition. |Relay number| Setting (1=on, 0=off, others possible depending on system hardware)| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_REPEAT_RELAY=182; /* Cycle a relay on and off for a desired number of cyles with a desired period. |Relay number| Cycle count| Cycle time (seconds, decimal)| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_SET_SERVO=183; /* Set a servo to a desired PWM value. |Servo number| PWM (microseconds, 1000 to 2000 typical)| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_REPEAT_SERVO=184; /* Cycle a between its nominal setting and a desired PWM for a desired number of cycles with a desired period. |Servo number| PWM (microseconds, 1000 to 2000 typical)| Cycle count| Cycle time (seconds)| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_CONTROL_VIDEO=200; /* Control onboard camera system. |Camera ID (-1 for all)| Transmission: 0: disabled, 1: enabled compressed, 2: enabled raw| Transmission mode: 0: video stream, >0: single images every n seconds (decimal)| Recording: 0: disabled, 1: enabled compressed, 2: enabled raw| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_DIGICAM_CONFIGURE=202; /* Mission command to configure an on-board camera controller system. |Modes: P, TV, AV, M, Etc| Shutter speed: Divisor number for one second| Aperture: F stop number| ISO number e.g. 80, 100, 200, Etc| Exposure type enumerator| Command Identity| Main engine cut-off time before camera trigger in seconds/10 (0 means no cut-off)|  */
		public final static int MAV_CMD_DO_DIGICAM_CONTROL=203; /* Mission command to control an on-board camera controller system. |Session control e.g. show/hide lens| Zoom's absolute position| Zooming step value to offset zoom from the current position| Focus Locking, Unlocking or Re-locking| Shooting Command| Command Identity| Empty|  */
		public final static int MAV_CMD_DO_MOUNT_CONFIGURE=204; /* Mission command to configure a camera or antenna mount |Mount operation mode (see MAV_MOUNT_MODE enum)| stabilize roll? (1 = yes, 0 = no)| stabilize pitch? (1 = yes, 0 = no)| stabilize yaw? (1 = yes, 0 = no)| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_MOUNT_CONTROL=205; /* Mission command to control a camera or antenna mount |pitch(deg*100) or lat, depending on mount mode.| roll(deg*100) or lon depending on mount mode| yaw(deg*100) or alt (in cm) depending on mount mode| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_DO_LAST=240; /* NOP - This command is only used to mark the upper limit of the DO commands in the enumeration |Empty| Empty| Empty| Empty| Empty| Empty| Empty|  */
		public final static int MAV_CMD_PREFLIGHT_CALIBRATION=241; /* Trigger calibration. This command will be only accepted if in pre-flight mode. |Gyro calibration: 0: no, 1: yes| Magnetometer calibration: 0: no, 1: yes| Ground pressure: 0: no, 1: yes| Radio calibration: 0: no, 1: yes| Empty| Empty| Empty|  */
		public final static int MAV_CMD_PREFLIGHT_SET_SENSOR_OFFSETS=242; /* Set sensor offsets. This command will be only accepted if in pre-flight mode. |Sensor to adjust the offsets for: 0: gyros, 1: accelerometer, 2: magnetometer, 3: barometer, 4: optical flow| X axis offset (or generic dimension 1), in the sensor's raw units| Y axis offset (or generic dimension 2), in the sensor's raw units| Z axis offset (or generic dimension 3), in the sensor's raw units| Generic dimension 4, in the sensor's raw units| Generic dimension 5, in the sensor's raw units| Generic dimension 6, in the sensor's raw units|  */
		public final static int MAV_CMD_PREFLIGHT_STORAGE=245; /* Request storage of different parameter values and logs. This command will be only accepted if in pre-flight mode. |Parameter storage: 0: READ FROM FLASH/EEPROM, 1: WRITE CURRENT TO FLASH/EEPROM| Mission storage: 0: READ FROM FLASH/EEPROM, 1: WRITE CURRENT TO FLASH/EEPROM| Reserved| Reserved| Empty| Empty| Empty|  */
		//public final static int MAV_CMD_PREFLIGHT_REBOOT_SHUTDOWN=246; /* Request the reboot or shutdown of system components. |0: Do nothing for autopilot, 1: Reboot autopilot, 2: Shutdown autopilot.| 0: Do nothing for onboard computer, 1: Reboot onboard computer, 2: Shutdown onboard computer.| Reserved| Reserved| Empty| Empty| Empty|  */
		public final static int MAV_CMD_OVERRIDE_GOTO=252; /* Hold / continue the current action |MAV_GOTO_DO_HOLD: hold MAV_GOTO_DO_CONTINUE: continue with next item in mission plan| MAV_GOTO_HOLD_AT_CURRENT_POSITION: Hold at current position MAV_GOTO_HOLD_AT_SPECIFIED_POSITION: hold at specified position| MAV_FRAME coordinate frame of hold point| Desired yaw angle in degrees| Latitude / X position| Longitude / Y position| Altitude / Z position|  */
		//public final static int MAV_CMD_MISSION_START=300; /* start running a mission |first_item: the first mission item to run| last_item:  the last mission item to run (after this item is run, the mission ends)|  */
		//public final static int MAV_CMD_COMPONENT_ARM_DISARM=400; /* Arms / Disarms a component |1 to arm, 0 to disarm|  */
		//public final static int MAV_CMD_ENUM_END=401; /*  | */
	}
	
	public class MAV_CMD_ACK{
		public final static int MAV_CMD_ACK_OK=1; //, * Command 
		public final static int MAV_CMD_ACK_ERR_FAIL=2; //, * Generic error message if none of the other reasons fails or if no detailed error reporting is implemented. | *
		public final static int MAV_CMD_ACK_ERR_ACCESS_DENIED=3; //, * The system is refusing to accept this command from this source 
		public final static int MAV_CMD_ACK_ERR_NOT_SUPPORTED=4; //, * Command or mission item is not supported, other commands would be accepted. | *
		public final static int MAV_CMD_ACK_ERR_COORDINATE_FRAME_NOT_SUPPORTED=5; //, * The coordinate frame of this command 
		public final static int MAV_CMD_ACK_ERR_COORDINATES_OUT_OF_RANGE=6; //, * The coordinate frame of this command is ok, but he coordinate values exceed the safety limits of this system. This is a generic error, please use the more specific error messages below if possible. | *
		public final static int MAV_CMD_ACK_ERR_X_LAT_OUT_OF_RANGE=7; //, * The X or latitude value is out of range. | *
		public final static int MAV_CMD_ACK_ERR_Y_LON_OUT_OF_RANGE=8; //, * The Y or longitude value is out of range. | *
		public final static int MAV_CMD_ACK_ERR_Z_ALT_OUT_OF_RANGE=9; //, * The Z or altitude value is out of range. | *
		public final static int MAV_CMD_ACK_ENUM_END=10; //, *  | *
	}

	public class MAV_VAR{
		public final static int MAV_VAR_FLOAT=0; //, * 32 bit float | *
		public final static int MAV_VAR_UINT8=1; //, * 8 bit unsigned integer | *
		public final static int MAV_VAR_INT8=2; //, * 8 bit signed integer | *
		public final static int MAV_VAR_UINT16=3; //, * 16 bit unsigned integer | *
		public final static int MAV_VAR_INT16=4; //, * 16 bit signed integer | *
		public final static int MAV_VAR_UINT32=5; //, * 32 bit unsigned integer | *
		public final static int MAV_VAR_INT32=6; //, * 32 bit signed integer | *
		public final static int MAV_VAR_ENUM_END=7; //, *  | *
	}

	public class MAV_RESULT{
		public final static int MAV_RESULT_ACCEPTED=0; //, * Command ACCEPTED and EXECUTED | *
		public final static int MAV_RESULT_TEMPORARILY_REJECTED=1; //, * Command TEMPORARY REJECTED
		public final static int MAV_RESULT_DENIED=2; //, * Command PERMANENTLY DENIED | *
		public final static int MAV_RESULT_UNSUPPORTED=3; //, * Command UNKNOWN
		public final static int MAV_RESULT_FAILED=4; //, * Command executed, but failed | *
		public final static int MAV_RESULT_ENUM_END=5; //, *  | *
	}

	public class MAV_MISSION_RESULT{
		public final static int MAV_MISSION_ACCEPTED=0; //, * mission accepted OK | *
		public final static int MAV_MISSION_ERROR=1; //, * generic error 
		public final static int MAV_MISSION_UNSUPPORTED_FRAME=2; //, * coordinate frame is not supported | *
		public final static int MAV_MISSION_UNSUPPORTED=3; //, * command is not supported | *
		public final static int MAV_MISSION_NO_SPACE=4; //, * mission item exceeds storage space | *
		public final static int MAV_MISSION_INVALID=5; //, * one of the parameters has an invalid value | *
		public final static int MAV_MISSION_INVALID_PARAM1=6; //, * param1 has an invalid value | *
		public final static int MAV_MISSION_INVALID_PARAM2=7; //, * param2 has an invalid value | *
		public final static int MAV_MISSION_INVALID_PARAM3=8; //, * param3 has an invalid value | *
		public final static int MAV_MISSION_INVALID_PARAM4=9; //, * param4 has an invalid value | *
		public final static int MAV_MISSION_INVALID_PARAM5_X=10; //, * x
		public final static int MAV_MISSION_INVALID_PARAM6_Y=11; //, * y
		public final static int MAV_MISSION_INVALID_PARAM7=12; //, * param7 has an invalid value | *
		public final static int MAV_MISSION_INVALID_SEQUENCE=13; //, * received waypoint out of sequence | *
		public final static int MAV_MISSION_DENIED=14; //, * not accepting any mission commands from this communication partner | *
		public final static int MAV_MISSION_RESULT_ENUM_END=15; //, *  | *
	}

	public class MAV_ACTION{
		public final static int    MAV_ACTION_HOLD = 0; //,
		public final static int    MAV_ACTION_MOTORS_START = 1; //,
		public final static int    MAV_ACTION_LAUNCH = 2; //,
		public final static int    MAV_ACTION_RETURN = 3; //,
		public final static int    MAV_ACTION_EMCY_LAND = 4; //,
		public final static int    MAV_ACTION_EMCY_KILL = 5; //,
		public final static int    MAV_ACTION_CONFIRM_KILL = 6; //,
		public final static int    MAV_ACTION_CONTINUE = 7; //,
		public final static int    MAV_ACTION_MOTORS_STOP = 8; //,
		public final static int    MAV_ACTION_HALT = 9; //,
		public final static int    MAV_ACTION_SHUTDOWN = 10; //,
		public final static int    MAV_ACTION_REBOOT = 11; //,
		public final static int    MAV_ACTION_SET_MANUAL = 12; //,
		public final static int    MAV_ACTION_SET_AUTO = 13; //,
		public final static int    MAV_ACTION_STORAGE_READ = 14; //,
		public final static int    MAV_ACTION_STORAGE_WRITE = 15; //,
		public final static int    MAV_ACTION_CALIBRATE_RC = 16; //,
		public final static int    MAV_ACTION_CALIBRATE_GYRO = 17; //,
		public final static int    MAV_ACTION_CALIBRATE_MAG = 18; //,
		public final static int    MAV_ACTION_CALIBRATE_ACC = 19; //,
		public final static int    MAV_ACTION_CALIBRATE_PRESSURE = 20; //,
		public final static int    MAV_ACTION_REC_START = 21; //,
		public final static int    MAV_ACTION_REC_PAUSE = 22; //,
		public final static int    MAV_ACTION_REC_STOP = 23; //,
		public final static int    MAV_ACTION_TAKEOFF = 24; //,
		public final static int    MAV_ACTION_NAVIGATE = 25; //,
		public final static int    MAV_ACTION_LAND = 26; //,
		public final static int    MAV_ACTION_LOITER = 27; //,
		public final static int    MAV_ACTION_SET_ORIGIN = 28; //,
		public final static int    MAV_ACTION_RELAY_ON = 29; //,
		public final static int    MAV_ACTION_RELAY_OFF = 30; //,
		public final static int    MAV_ACTION_GET_IMAGE = 31; //,
		public final static int    MAV_ACTION_VIDEO_START = 32; //,
		public final static int    MAV_ACTION_VIDEO_STOP = 33; //,
		public final static int    MAV_ACTION_RESET_MAP = 34; //,
		public final static int    MAV_ACTION_RESET_PLAN = 35; //,
		public final static int    MAV_ACTION_DELAY_BEFORE_COMMAND = 36; //,
		public final static int    MAV_ACTION_ASCEND_AT_RATE = 37; //,
		public final static int    MAV_ACTION_CHANGE_MODE = 38; //,
		public final static int    MAV_ACTION_LOITER_MAX_TURNS = 39; //,
		public final static int    MAV_ACTION_LOITER_MAX_TIME = 40; //,
		public final static int    MAV_ACTION_START_HILSIM = 41; //,
		public final static int    MAV_ACTION_STOP_HILSIM = 42; //,    
		public final static int    MAV_ACTION_NB         = 43; //
	}

	public class MODES_ARDUPILOT{
		public final static int    Manual=0;    
		public final static int    Circle=1; 
		public final static int    Stabilize=2; 
		public final static int    Training=3;      					
		public final static int    FBWA=5; 
		public final static int    FBWB=6;	
		public final static int    Auto=10;
		public final static int    RTL=11; 
		public final static int    Loiter=12;
		public final static int    Guided=13; 
	}
	public class MODES_ARDUCOPTER{
		public final static int    Stabilize=0;    
		public final static int    Acro=1; 
		public final static int    AltHold=2; 
		public final static int    Auto=3;      					
		public final static int    Guided=4; 
		public final static int    Loiter=5;	
		public final static int    RTL=6;
		public final static int    Circle=7; 
		public final static int    PotitionHold=8;
		public final static int    Land=9; 
		public final static int    OFLoiter=10;
		public final static int    ToyAuto=11;
		public final static int    ToyManual=12;
	}
	 
	public static String getMavCmd(int a ){
		return getMAVfield(MAV_CMD.class, a);
	}
	
	public static String getMode(int i){
		return getMAVfield(MAV_MODE.class, i);
	}

	public static String getState(int i){
		return getMAVfield(MAV_STATE.class, i);

	}

	public static String getMavFrame(int a ){
		return getMAVfield(MAV_FRAME.class, a);

	}
	public static String getMavResult(int a ){
		return getMAVfield(MAV_RESULT.class, a);

	}
	
//	public static String getNav(int i){
//
//		String s = getMAVfield(MAV_NAV.class, i);
//		String ret = "";
//
//		if( !s.equals("")){
//			s.replace("MAV_NAV_", "");
//			ret = "MAV " + s.charAt(0);
//
//			s = s.substring(1);
//			ret += s.toLowerCase();
//
//		}
//
//		return ret;
//
//	}
	public static String getMAVfield(Class<?> cls, int a ){
		Field[] field = cls.getFields();

		try {
			for (Field f : field) {
				if (f.getInt(null) == a)
					return f.getName();
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "" + a;
	}
}
