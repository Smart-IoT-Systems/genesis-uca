
/*
 * Generated Smool KP helper
 */
package ENACTProducer.api;


import ENACTProducer.model.smoolcore.*;
import ENACTProducer.model.smoolcore.impl.*;

public abstract class SmoolKPHelper {

	// Helpers for Leaf class AccelerometerInformation
	public static IAccelerometerInformation createAccelerometerInformation (Double axisx, Double axisy, Double axisz, String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
		return createAccelerometerInformation("", axisx, axisy, axisz, dataID, timestamp, unit, value, securityData);
	}

	public static IAccelerometerInformation createAccelerometerInformation (String elemID, Double axisx, Double axisy, Double axisz, String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
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

	// Helpers for Leaf class BlackoutInformation
	public static IBlackoutInformation createBlackoutInformation (String dataID, Boolean status, String timestamp, ISecurity securityData) {
		return createBlackoutInformation("", dataID, status, timestamp, securityData);
	}

	public static IBlackoutInformation createBlackoutInformation (String elemID, String dataID, Boolean status, String timestamp, ISecurity securityData) {
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

	// Helpers for Leaf class FloodInformation
	public static IFloodInformation createFloodInformation (String dataID, Boolean status, String timestamp, ISecurity securityData) {
		return createFloodInformation("", dataID, status, timestamp, securityData);
	}

	public static IFloodInformation createFloodInformation (String elemID, String dataID, Boolean status, String timestamp, ISecurity securityData) {
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

	// Helpers for Leaf class GasInformation
	public static IGasInformation createGasInformation (String dataID, String timestamp, String type, String unit, Double value, ISecurity securityData) {
		return createGasInformation("", dataID, timestamp, type, unit, value, securityData);
	}

	public static IGasInformation createGasInformation (String elemID, String dataID, String timestamp, String type, String unit, Double value, ISecurity securityData) {
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

	// Helpers for Leaf class HumidityInformation
	public static IHumidityInformation createHumidityInformation (String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
		return createHumidityInformation("", dataID, timestamp, unit, value, securityData);
	}

	public static IHumidityInformation createHumidityInformation (String elemID, String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
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

	// Helpers for Leaf class LightingInformation
	public static ILightingInformation createLightingInformation (String dataID, String lightUnit, Double lightValue, String timestamp, String unit, Double value, ISecurity securityData) {
		return createLightingInformation("", dataID, lightUnit, lightValue, timestamp, unit, value, securityData);
	}

	public static ILightingInformation createLightingInformation (String elemID, String dataID, String lightUnit, Double lightValue, String timestamp, String unit, Double value, ISecurity securityData) {
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
	public static IEmailAddress createEmailAddressLogicalLocation (String elemID, String dataID, String email, String timestamp, ILogicalLocation logicalLoc) {
		EmailAddress token = new EmailAddress();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setEmail(email);
		token.setTimestamp(timestamp);
		token.setLogicalLoc(logicalLoc);
		return token;
	}

	// Helper for subclass class
	public static IFacebookAccount createFacebookAccountLogicalLocation (String elemID, String dataID, String password, String timestamp, String username, ILogicalLocation logicalLoc) {
		FacebookAccount token = new FacebookAccount();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setPassword(password);
		token.setTimestamp(timestamp);
		token.setUsername(username);
		token.setLogicalLoc(logicalLoc);
		return token;
	}

	// Helper for subclass class
	public static ITwitterAccount createTwitterAccountLogicalLocation (String elemID, String dataID, String password, String timestamp, String username, ILogicalLocation logicalLoc) {
		TwitterAccount token = new TwitterAccount();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setPassword(password);
		token.setTimestamp(timestamp);
		token.setUsername(username);
		token.setLogicalLoc(logicalLoc);
		return token;
	}

	// Helper for subclass class
	public static IWhatsAppAccount createWhatsAppAccountLogicalLocation (String elemID, String dataID, String password, String timestamp, String username, ILogicalLocation logicalLoc) {
		WhatsAppAccount token = new WhatsAppAccount();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setPassword(password);
		token.setTimestamp(timestamp);
		token.setUsername(username);
		token.setLogicalLoc(logicalLoc);
		return token;
	}

	// Helper for subclass class
	public static IIP4Address createIP4AddressLogicalLocation (String elemID, String dataID, String timestamp, ILogicalLocation logicalLoc) {
		IP4Address token = new IP4Address();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setLogicalLoc(logicalLoc);
		return token;
	}

	// Helper for subclass class
	public static IIP6Address createIP6AddressLogicalLocation (String elemID, String dataID, String timestamp, ILogicalLocation logicalLoc) {
		IP6Address token = new IP6Address();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setTimestamp(timestamp);
		token.setLogicalLoc(logicalLoc);
		return token;
	}

	// Helper for subclass class
	public static ITelephoneNumber createTelephoneNumberLogicalLocation (String elemID, String dataID, String number, String timestamp, ILogicalLocation logicalLoc) {
		TelephoneNumber token = new TelephoneNumber();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setDataID(dataID);
		token.setNumber(number);
		token.setTimestamp(timestamp);
		token.setLogicalLoc(logicalLoc);
		return token;
	}

	// Helper for subclass class
	public static IEmailMessage createEmailMessageMessage (String elemID, String body, String dataID, String subject, String timestamp) {
		EmailMessage token = new EmailMessage();

		if (elemID != null && !elemID.equals("")) {
			token._setIndividualID(elemID);
		}

		token.setBody(body);
		token.setDataID(dataID);
		token.setSubject(subject);
		token.setTimestamp(timestamp);
		return token;
	}

	// Helpers for Leaf class NoiseInformation
	public static INoiseInformation createNoiseInformation (String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
		return createNoiseInformation("", dataID, timestamp, unit, value, securityData);
	}

	public static INoiseInformation createNoiseInformation (String elemID, String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
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

	// Helpers for Leaf class PresenceInformation
	public static IPresenceInformation createPresenceInformation (String dataID, Boolean status, String timestamp, ISecurity securityData) {
		return createPresenceInformation("", dataID, status, timestamp, securityData);
	}

	public static IPresenceInformation createPresenceInformation (String elemID, String dataID, Boolean status, String timestamp, ISecurity securityData) {
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

	// Helpers for Leaf class SmokeInformation
	public static ISmokeInformation createSmokeInformation (String dataID, Boolean status, String timestamp, ISecurity securityData) {
		return createSmokeInformation("", dataID, status, timestamp, securityData);
	}

	public static ISmokeInformation createSmokeInformation (String elemID, String dataID, Boolean status, String timestamp, ISecurity securityData) {
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

	// Helpers for Leaf class TemperatureInformation
	public static ITemperatureInformation createTemperatureInformation (String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
		return createTemperatureInformation("", dataID, timestamp, unit, value, securityData);
	}

	public static ITemperatureInformation createTemperatureInformation (String elemID, String dataID, String timestamp, String unit, Double value, ISecurity securityData) {
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
