package com.bvcode.ncopter.AC1Data;

import java.text.DecimalFormat;

import com.MAVLink.Messages.IMAVLinkMessage;

public class ProtocolParser{

	//--------------------------------------------
	private static final char STOP_DATA = 'X';
	private static final char STATUS = 'T';
	private static final char STABLE_PIDS = 'B';
	private static final char ALTITUDE_PIDS = 'F';
	private static final char GPS_PIDS = 'D';
	private static final char ACRO_PIDS = 'P';
	private static final char SENSOR_OFFSETS = 'J';
	private static final char GPS_DATA = '4';
	private static final char SENSOR_DATA = 'Q';
	private static final char TRANSMITTER_VALUES = 'U';
	private static final char FLIGHT_DATA = 'S';
	private static final char TRANSMITTER_SCALE = '2';
	

	private static double[] stringToDoubleArray(String in) {
		String[] values = in.split(",");

		double[] out = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			try {
				out[i] = Double.parseDouble(values[i].trim());

			} catch (NumberFormatException e) {
				out[i] = 0;

			}
		}
		return out;
	}

	private static int[] stringToIntArray(String in) {
		String[] values = in.split(",");

		int[] out = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			try {
				out[i] = Integer.parseInt(values[i].trim());

			} catch (NumberFormatException e) {
				out[i] = 0;

			}
		}
		return out;
	}

	public static IMAVLinkMessage parseCopterData(String lastString, String m) {
		IMAVLinkMessage cData = null;
		if (lastString.length() == 0)
			return cData;

		switch (lastString.charAt(0)) {
		case STATUS:
			StringMessage sm = new StringMessage();
			sm.message = m;
			cData = sm;

			break;

		case STABLE_PIDS:
			double[] stable = stringToDoubleArray(m);
			if (stable.length == 11) {
				StablePids ap = new StablePids();
				ap.rollPID[0] = stable[0];
				ap.rollPID[1] = stable[1];
				ap.rollPID[2] = stable[2];
				ap.pitchPID[0] = stable[3];
				ap.pitchPID[1] = stable[4];
				ap.pitchPID[2] = stable[5];
				ap.yawPID[0] = stable[6];
				ap.yawPID[1] = stable[7];
				ap.yawPID[2] = stable[8];
				ap.KP_rate = stable[9];
				ap.magnetometer = stable[10];
				cData = ap;
			}
			break;

		case ALTITUDE_PIDS:
			double[] altitude = stringToDoubleArray(m);
			if (altitude.length == 3) {
				AltitudePids ap = new AltitudePids();
				ap.altitude = altitude;
				cData = ap;

			}
			break;

		case GPS_PIDS:
			double[] GPS = stringToDoubleArray(m);
			if (GPS.length == 8) {
				GPSPids gp = new GPSPids();
				gp.rollPID[0] = GPS[0];
				gp.rollPID[1] = GPS[1];
				gp.rollPID[2] = GPS[2];
				gp.pitchPID[0] = GPS[3];
				gp.pitchPID[1] = GPS[4];
				gp.pitchPID[2] = GPS[5];
				gp.maxAngle = GPS[6];
				gp.geog_correctionFactor = GPS[7];
				cData = gp;

			}
			break;

		case ACRO_PIDS:
			double[] acro = stringToDoubleArray(m);
			if (acro.length == 10) {
				AcroPIDS ap = new AcroPIDS();
				ap.rollPID[0] = acro[0];
				ap.rollPID[1] = acro[1];
				ap.rollPID[2] = acro[2];
				ap.pitchPID[0] = acro[3];
				ap.pitchPID[1] = acro[4];
				ap.pitchPID[2] = acro[5];
				ap.yawPID[0] = acro[6];
				ap.yawPID[1] = acro[7];
				ap.yawPID[2] = acro[8];
				ap.xMitFactor = acro[9];
				cData = ap;
			}
			break;

		case SENSOR_OFFSETS:
			SensorOffsets so = new SensorOffsets();
			so.lastOffsets = stringToIntArray(m);
			if (so.lastOffsets != null && so.lastOffsets.length == 6) {
				so.roll = so.lastOffsets[3];
				so.pitch = so.lastOffsets[4];
				so.z = so.lastOffsets[5];
				cData = so;
			}

		case GPS_DATA:
			String[] s2 = m.split("\t");
			if (s2.length == 9) {
				GPSData gd = new GPSData();
				gd.time = (int) Double.parseDouble(s2[0]);
				gd.latitude = (int) Double.parseDouble(s2[1]) / 10;
				gd.longitude = (int) Double.parseDouble(s2[2]) / 10;
				gd.altitude = (int) (Double.parseDouble(s2[3]) / 100.0);
				gd.groundSpeed = (int) (Double.parseDouble(s2[4]) / 100.0);
				gd.groundCourse = (int) (Double.parseDouble(s2[5]) / 100.0);
				gd.fix = (int) Double.parseDouble(s2[6]);
				// gd.num_sats = (int)Double.parseDouble(s2[7]);
				cData = gd;
			}
			break;

		case SENSOR_DATA:
			int[] values = stringToIntArray(m);
			if (values != null && values.length == 11) {
				SensorData sd = new SensorData();
				sd.pitch = values[4];
				sd.roll = values[3];
				sd.z = values[5];

				cData = sd;

			}
			break;

		case TRANSMITTER_VALUES:
			TransmitterValues ts = new TransmitterValues();
			ts.transValues = stringToIntArray(m);
			if (ts.transValues.length == 9)
				cData = ts;

			break;

		case FLIGHT_DATA:
			double[] d = stringToDoubleArray(m);
			if (d.length == 21) {
				FlightData fd = new FlightData();
				fd.gyroX = d[1];
				fd.gyroY = d[2];
				fd.gyroYaw = d[3];
				fd.accelX = d[12];
				fd.accelY = d[13];
				fd.accelZ = d[14];
				fd.magX = d[18];
				fd.magY = d[19];
				fd.magZ = d[20];
				cData = fd;

			}
			break;

		case TRANSMITTER_SCALE:
			TransmitterScale ts2 = new TransmitterScale();
			ts2.scaleValues = stringToDoubleArray(m);
			if (ts2.scaleValues.length == 12)
				cData = ts2;

			break;
		};

		return cData;
	}

	public static byte[] saveOffsets(double gyroX, double gyroY, double gyroZ,
			double accelX, double accelY, double accelZ) {
		String cmd = "I " + gyroX + ";" + gyroY + ";" + gyroZ + ";" + accelX
				+ ";" + accelY + ";" + accelZ + ";W";
		return cmd.getBytes();
	}

	public static byte[] saveAcroPids(double[] rollPIDs, double[] pitchPIDs,
			double[] yawPIDs, double xMitFactor) {
		String cmd = "O " + rollPIDs[0] + ";" + rollPIDs[1] + ";" + rollPIDs[2]
				+ ";" + pitchPIDs[0] + ";" + pitchPIDs[1] + ";" + pitchPIDs[2]
				+ ";" + yawPIDs[0] + ";" + yawPIDs[1] + ";" + yawPIDs[2] + ";"
				+ xMitFactor + ";W";
		return cmd.getBytes();

	}

	public static byte[] saveStablePids(double[] r, double[] p, double[] y,
			double stable_KP_Rate, double magnetometer) {
		String cmd = "A " + r[0] + ";" + r[1] + ";" + r[2] + ";" + p[0] + ";"
				+ p[1] + ";" + p[2] + ";" + y[0] + ";" + y[1] + ";" + y[2]
				+ ";" + stable_KP_Rate + ";" + magnetometer + ";W";
		return cmd.getBytes();

	}

	public static byte[] saveGPSPids(double[] r, double[] p, double maxAngle,
			double geog_correction_factor) {
		String cmd = "D " + r[0] + ";" + r[1] + ";" + r[2] + ";" + p[0] + ";"
				+ p[1] + ";" + p[2] + ";" + maxAngle + ";"
				+ geog_correction_factor + ";W";
		return cmd.getBytes();

	}

	public static byte[] saveAltitudePids(double[] r) {
		String cmd = "E " + r[0] + ";" + r[1] + ";" + r[2] + ";W";
		return cmd.getBytes();

	}

	public static byte[] setTransmitterScale(double[] slope, double[] offset) {
		try {
			String cmd = "1 ";
			DecimalFormat n = new DecimalFormat("#.####");
			for (int i = 0; i < 6; i++) {
				cmd = cmd + Double.valueOf(n.format(slope[i])) + ";"
						+ Double.valueOf(n.format(offset[i])) + ";";
			}

			return (cmd).getBytes();

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static byte[] saveTransmitterOffsets() {
		return new String("6W").getBytes();

	}

	public static byte[] saveCommand() {
		return new byte[] { 'W' };

	}

	public static byte[] requestGPSData() {
		char c[] = { GPS_DATA };
		return new String(c).getBytes();

	}

	public static byte[] requestFlightData() {
		char c[] = { FLIGHT_DATA };
		return new String(c).getBytes();
	}

	public static byte[] requestSensorOffsets() {
		char c[] = { SENSOR_OFFSETS };
		return new String(c).getBytes();
	}

	public static byte[] requestSensorData() {
		char c[] = { SENSOR_DATA };
		return new String(c).getBytes();
	}

	public static byte[] requestAcroPIDs() {
		char c[] = { ACRO_PIDS };
		return new String(c).getBytes();
	}

	public static byte[] requestStablePIDs() {
		char c[] = { STABLE_PIDS };
		return new String(c).getBytes();
	}

	public static byte[] requestGPSPIDs() {
		char c[] = { GPS_PIDS };
		return new String(c).getBytes();
	}

	public static byte[] requestAltitudePIDs() {
		char c[] = { ALTITUDE_PIDS };
		return new String(c).getBytes();
	}

	public static byte[] requestTransmitterValues() {
		char c[] = { TRANSMITTER_VALUES };
		return new String(c).getBytes();
	}

	public static byte[] requestTransmitterScale() {
		char c[] = { TRANSMITTER_SCALE };
		return new String(c).getBytes();
	}

	public static byte[] requestStatus() {
		char c[] = { STATUS };
		return new String(c).getBytes();
	}

	public static byte[] requestStopDataFlow() {
		char c[] = { STOP_DATA };
		return new String(c).getBytes();
	}

	
	
	public static boolean passThrough = false;
	public static void setPassThrough(boolean b) {
		passThrough = b;
		
	}

	
	
}
