
/*
 * Generated Producer interface
 */
package ENACTProducer.api;


import org.smool.kpi.model.exception.KPIModelException;

import ENACTProducer.model.smoolcore.*;

public interface Producer {

	/**
	 * Create method for the Accelerometer concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param accinfo.
	 * @param alarms.
	 * @param physicalLoc.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createAccelerometer(String elemID, String deviceID, String vendor, IAccelerometerInformation accinfo, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Update method for the Accelerometer concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param accinfo.
	 * @param alarms.
	 * @param physicalLoc.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateAccelerometer(String elemID, String deviceID, String vendor, IAccelerometerInformation accinfo, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Remove method for the Accelerometer concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeAccelerometer(String elemID) throws KPIModelException;
	/**
	 * Create method for the BlackoutSensor concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param blackout.
	 * @param physicalLoc.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createBlackoutSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IBlackoutInformation blackout, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Update method for the BlackoutSensor concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param blackout.
	 * @param physicalLoc.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateBlackoutSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IBlackoutInformation blackout, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Remove method for the BlackoutSensor concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeBlackoutSensor(String elemID) throws KPIModelException;
	/**
	 * Create method for the FloodSensor concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param flood.
	 * @param physicalLoc.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createFloodSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IFloodInformation flood, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Update method for the FloodSensor concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param flood.
	 * @param physicalLoc.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateFloodSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IFloodInformation flood, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Remove method for the FloodSensor concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeFloodSensor(String elemID) throws KPIModelException;
	/**
	 * Create method for the GasSensor concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param gas.
	 * @param physicalLoc.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createGasSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IGasInformation gas, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Update method for the GasSensor concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param gas.
	 * @param physicalLoc.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateGasSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IGasInformation gas, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Remove method for the GasSensor concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeGasSensor(String elemID) throws KPIModelException;
	/**
	 * Create method for the HumiditySensor concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param humidity.
	 * @param physicalLoc.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createHumiditySensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IHumidityInformation humidity, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Update method for the HumiditySensor concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param humidity.
	 * @param physicalLoc.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateHumiditySensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IHumidityInformation humidity, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Remove method for the HumiditySensor concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeHumiditySensor(String elemID) throws KPIModelException;
	/**
	 * Create method for the LightingSensor concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param lighting.
	 * @param physicalLoc.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createLightingSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, ILightingInformation lighting, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Update method for the LightingSensor concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param lighting.
	 * @param physicalLoc.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateLightingSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, ILightingInformation lighting, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Remove method for the LightingSensor concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeLightingSensor(String elemID) throws KPIModelException;
	/**
	 * Create method for the MessageReceiveSensor concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param destination.
	 * @param logicalLoc.
	 * @param message.
	 * @param origin.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createMessageReceiveSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, java.util.List<ILogicalLocation> destination, ILogicalLocation logicalLoc, IMessage message, ILogicalLocation origin) throws KPIModelException;

	/**
	 * Update method for the MessageReceiveSensor concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param destination.
	 * @param logicalLoc.
	 * @param message.
	 * @param origin.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateMessageReceiveSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, java.util.List<ILogicalLocation> destination, ILogicalLocation logicalLoc, IMessage message, ILogicalLocation origin) throws KPIModelException;

	/**
	 * Remove method for the MessageReceiveSensor concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeMessageReceiveSensor(String elemID) throws KPIModelException;
	/**
	 * Create method for the NoiseSensor concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param noise.
	 * @param physicalLoc.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createNoiseSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, INoiseInformation noise, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Update method for the NoiseSensor concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param noise.
	 * @param physicalLoc.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateNoiseSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, INoiseInformation noise, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Remove method for the NoiseSensor concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeNoiseSensor(String elemID) throws KPIModelException;
	/**
	 * Create method for the PresenceSensor concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param physicalLoc.
	 * @param presence.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createPresenceSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc, IPresenceInformation presence) throws KPIModelException;

	/**
	 * Update method for the PresenceSensor concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param physicalLoc.
	 * @param presence.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updatePresenceSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc, IPresenceInformation presence) throws KPIModelException;

	/**
	 * Remove method for the PresenceSensor concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removePresenceSensor(String elemID) throws KPIModelException;
	/**
	 * Create method for the SmokeSensor concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param physicalLoc.
	 * @param smoke.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createSmokeSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc, ISmokeInformation smoke) throws KPIModelException;

	/**
	 * Update method for the SmokeSensor concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param physicalLoc.
	 * @param smoke.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateSmokeSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc, ISmokeInformation smoke) throws KPIModelException;

	/**
	 * Remove method for the SmokeSensor concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeSmokeSensor(String elemID) throws KPIModelException;
	/**
	 * Create method for the TemperatureSensor concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param physicalLoc.
	 * @param temperature.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createTemperatureSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc, ITemperatureInformation temperature) throws KPIModelException;

	/**
	 * Update method for the TemperatureSensor concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param physicalLoc.
	 * @param temperature.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateTemperatureSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc, ITemperatureInformation temperature) throws KPIModelException;

	/**
	 * Remove method for the TemperatureSensor concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeTemperatureSensor(String elemID) throws KPIModelException;

}
