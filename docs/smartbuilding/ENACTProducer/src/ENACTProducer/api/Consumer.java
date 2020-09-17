
/*
 * Generated Consumer interface
 */
package ENACTProducer.api;


import org.smool.kpi.model.exception.KPIModelException;

import java.util.List;

import ENACTProducer.model.smoolcore.impl.*;

public interface Consumer {

	/**
	 * Subscribe to the BlindPositionActuator concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToBlindPositionActuator(BlindPositionActuatorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the BlindPositionActuator concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToBlindPositionActuator(BlindPositionActuatorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the BlindPositionActuator concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<BlindPositionActuator> queryAllBlindPositionActuator() throws KPIModelException;
	/**
	 * Queries for a single BlindPositionActuator concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public BlindPositionActuator queryBlindPositionActuator(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the HVACActuator concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToHVACActuator(HVACActuatorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the HVACActuator concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToHVACActuator(HVACActuatorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the HVACActuator concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<HVACActuator> queryAllHVACActuator() throws KPIModelException;
	/**
	 * Queries for a single HVACActuator concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public HVACActuator queryHVACActuator(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the LightRangeActuator concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToLightRangeActuator(LightRangeActuatorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the LightRangeActuator concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToLightRangeActuator(LightRangeActuatorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the LightRangeActuator concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<LightRangeActuator> queryAllLightRangeActuator() throws KPIModelException;
	/**
	 * Queries for a single LightRangeActuator concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public LightRangeActuator queryLightRangeActuator(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the LightSwitchActuator concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToLightSwitchActuator(LightSwitchActuatorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the LightSwitchActuator concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToLightSwitchActuator(LightSwitchActuatorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the LightSwitchActuator concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<LightSwitchActuator> queryAllLightSwitchActuator() throws KPIModelException;
	/**
	 * Queries for a single LightSwitchActuator concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public LightSwitchActuator queryLightSwitchActuator(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the MessageReceiveSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToMessageReceiveSensor(MessageReceiveSensorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the MessageReceiveSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToMessageReceiveSensor(MessageReceiveSensorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the MessageReceiveSensor concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<MessageReceiveSensor> queryAllMessageReceiveSensor() throws KPIModelException;
	/**
	 * Queries for a single MessageReceiveSensor concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public MessageReceiveSensor queryMessageReceiveSensor(String individualID) throws KPIModelException;

}
