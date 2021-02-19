
/*
 * Generated Producer interface
 */
package ENACTConsumer.api;


import org.smool.kpi.model.exception.KPIModelException;

import ENACTConsumer.model.smoolcore.*;

public interface Producer {

	/**
	 * Create method for the BlindPositionActuator concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param blindPos.
	 * @param physicalLoc.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createBlindPositionActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IContinuousInformation blindPos, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Update method for the BlindPositionActuator concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param blindPos.
	 * @param physicalLoc.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateBlindPositionActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IContinuousInformation blindPos, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Remove method for the BlindPositionActuator concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeBlindPositionActuator(String elemID) throws KPIModelException;
	/**
	 * Create method for the HVACActuator concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param physicalLoc.
	 * @param programme.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createHVACActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc, IProgrammeInformation programme) throws KPIModelException;

	/**
	 * Update method for the HVACActuator concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param physicalLoc.
	 * @param programme.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateHVACActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc, IProgrammeInformation programme) throws KPIModelException;

	/**
	 * Remove method for the HVACActuator concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeHVACActuator(String elemID) throws KPIModelException;
	/**
	 * Create method for the LightRangeActuator concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param lightIntensity.
	 * @param physicalLoc.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createLightRangeActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IContinuousInformation lightIntensity, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Update method for the LightRangeActuator concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param lightIntensity.
	 * @param physicalLoc.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateLightRangeActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IContinuousInformation lightIntensity, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Remove method for the LightRangeActuator concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeLightRangeActuator(String elemID) throws KPIModelException;
	/**
	 * Create method for the LightSwitchActuator concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param bool.
	 * @param physicalLoc.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createLightSwitchActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IBooleanInformation bool, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Update method for the LightSwitchActuator concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param bool.
	 * @param physicalLoc.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateLightSwitchActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IBooleanInformation bool, IPhysicalLocation physicalLoc) throws KPIModelException;

	/**
	 * Remove method for the LightSwitchActuator concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeLightSwitchActuator(String elemID) throws KPIModelException;
	/**
	 * Create method for the WashingMachineActuator concept
	 * This method creates a new concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be created.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param bool.
	 * @param physicalLoc.
	 * @param programme.
	 * @return the ID of the generated concept instance.
	 * @throws KPIModelException if an error occurs during concept creation / publishing.
	 */
	public String createWashingMachineActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IBooleanInformation bool, IPhysicalLocation physicalLoc, IProgrammeInformation programme) throws KPIModelException;

	/**
	 * Update method for the WashingMachineActuator concept
	 * This method updates an existing concept instance and publishes it
	 *
	 * @param elemID. The ID of the concept instance ID to be updated.
	 * @param deviceID.
	 * @param vendor.
	 * @param alarms.
	 * @param bool.
	 * @param physicalLoc.
	 * @param programme.
	 * @throws KPIModelException if an error occurs during concept update / publishing.
	 */
	public void updateWashingMachineActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms, IBooleanInformation bool, IPhysicalLocation physicalLoc, IProgrammeInformation programme) throws KPIModelException;

	/**
	 * Remove method for the WashingMachineActuator concept
	 *
	 * @param elemID. The ID of the concept instance ID to be removed.
	 * @throws KPIModelException if an error occurs during concept removal.
	 */
	public void removeWashingMachineActuator(String elemID) throws KPIModelException;

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


}
