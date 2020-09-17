
/*
 * Generated Smool KP helper
 */
package ENACTConsumer.api;


import ENACTConsumer.model.smoolcore.*;
import ENACTConsumer.model.smoolcore.impl.*;

public abstract class SmoolKPHelper {

	// Helpers for Leaf class Alarm
	public static IAlarm createAlarm (String alarmDescription, String alarmSeverity, String dataID, String timestamp) {
		return createAlarm("", alarmDescription, alarmSeverity, dataID, timestamp);
	}

	public static IAlarm createAlarm (String elemID, String alarmDescription, String alarmSeverity, String dataID, String timestamp) {
		Alarm token = new Alarm();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}
		
		token.setAlarmDescription(alarmDescription);
		token.setAlarmSeverity(alarmSeverity);
		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		return token;
	}

	// Helper for subclass class
	public static IAccelerometerInformation createAccelerometerInformationContinuousInformation (String elemID, Double axisx, Double axisy, Double axisz, String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
		AccelerometerInformation token = new AccelerometerInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setAxisx(axisx);
		token.setAxisy(axisy);
		token.setAxisz(axisz);
		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setUnit(unit);
		token.setValue(value);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static IGasInformation createGasInformationContinuousInformation (String elemID, String dataID, String timestamp, String type, String unit, Double value, ISecurity securityData) {
		GasInformation token = new GasInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setType(type);
		token.setUnit(unit);
		token.setValue(value);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static IHumidityInformation createHumidityInformationContinuousInformation (String elemID, String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
		HumidityInformation token = new HumidityInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setUnit(unit);
		token.setValue(value);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static ILightingInformation createLightingInformationContinuousInformation (String elemID, String dataID, String lightUnit, Double lightValue, String timestamp, String unit, Double value, ISecurity securityData) {
		LightingInformation token = new LightingInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setLightUnit(lightUnit);
		token.setLightValue(lightValue);
		token.setTimestamp(timestamp);
		token.setUnit(unit);
		token.setValue(value);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static INoiseInformation createNoiseInformationContinuousInformation (String elemID, String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
		NoiseInformation token = new NoiseInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setUnit(unit);
		token.setValue(value);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static ITemperatureInformation createTemperatureInformationContinuousInformation (String elemID, String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
		TemperatureInformation token = new TemperatureInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setUnit(unit);
		token.setValue(value);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static IGisLocation createGisLocationPhysicalLocation (String elemID, String dataID, Double latitude, Double longitude, String timestamp) {
		GisLocation token = new GisLocation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setLatitude(latitude);
		token.setLongitude(longitude);
		token.setTimestamp(timestamp);
		return token;
	}

	// Helper for subclass class
	public static ILocation2D createLocation2DPhysicalLocation (String elemID, String dataID, String timestamp, Double x, Double y) {
		Location2D token = new Location2D();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setX(x);
		token.setY(y);
		return token;
	}

	// Helper for subclass class
	public static ILocation3D createLocation3DPhysicalLocation (String elemID, String dataID, String timestamp, Double x, Double y, Double z) {
		Location3D token = new Location3D();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setX(x);
		token.setY(y);
		token.setZ(z);
		return token;
	}

	// Helpers for Leaf class ProgrammeInformation
	public static IProgrammeInformation createProgrammeInformation (String dataID, String end, String start, String timestamp, ISecurity securityData) {
		return createProgrammeInformation("", dataID, end, start, timestamp, securityData);
	}

	public static IProgrammeInformation createProgrammeInformation (String elemID, String dataID, String end, String start, String timestamp, ISecurity securityData) {
		ProgrammeInformation token = new ProgrammeInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}
		
		token.setDataID(dataID);
		token.setEnd(end);
		token.setStart(start);
		token.setTimestamp(timestamp);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static IBlackoutInformation createBlackoutInformationBooleanInformation (String elemID, String dataID, Boolean status, String timestamp, ISecurity securityData) {
		BlackoutInformation token = new BlackoutInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setStatus(status);
		token.setTimestamp(timestamp);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static IFloodInformation createFloodInformationBooleanInformation (String elemID, String dataID, Boolean status, String timestamp, ISecurity securityData) {
		FloodInformation token = new FloodInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setStatus(status);
		token.setTimestamp(timestamp);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static IPresenceInformation createPresenceInformationBooleanInformation (String elemID, String dataID, Boolean status, String timestamp, ISecurity securityData) {
		PresenceInformation token = new PresenceInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setStatus(status);
		token.setTimestamp(timestamp);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static ISmokeInformation createSmokeInformationBooleanInformation (String elemID, String dataID, Boolean status, String timestamp, ISecurity securityData) {
		SmokeInformation token = new SmokeInformation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setStatus(status);
		token.setTimestamp(timestamp);
		token.setSecurityData(securityData);
		return token;
	}

	// Helper for subclass class
	public static ISecurityAuthentication createSecurityAuthenticationSecurity (String elemID, String data, String dataID, String timestamp, String type) {
		SecurityAuthentication token = new SecurityAuthentication();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setData(data);
		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setType(type);
		return token;
	}

	// Helper for subclass class
	public static ISecurityAuthorization createSecurityAuthorizationSecurity (String elemID, String data, String dataID, String timestamp, String type) {
		SecurityAuthorization token = new SecurityAuthorization();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setData(data);
		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setType(type);
		return token;
	}

	// Helper for subclass class
	public static ISecurityConfidentiality createSecurityConfidentialitySecurity (String elemID, String data, String dataID, String timestamp, String type) {
		SecurityConfidentiality token = new SecurityConfidentiality();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setData(data);
		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setType(type);
		return token;
	}

	// Helper for subclass class
	public static ISecurityIntegrity createSecurityIntegritySecurity (String elemID, String data, String dataID, String timestamp, String type) {
		SecurityIntegrity token = new SecurityIntegrity();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setData(data);
		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setType(type);
		return token;
	}

	// Helper for subclass class
	public static ISecurityNonRepudiation createSecurityNonRepudiationSecurity (String elemID, String data, String dataID, String timestamp, String type) {
		SecurityNonRepudiation token = new SecurityNonRepudiation();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setData(data);
		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setType(type);
		return token;
	}


}
