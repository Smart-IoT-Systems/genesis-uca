
/*
 * Generated Consumer interface
 */
package ENACTConsumer.api;


import org.smool.kpi.model.exception.KPIModelException;

import java.util.List;

import ENACTConsumer.model.smoolcore.impl.*;

public interface Consumer {

	/**
	 * Subscribe to the Accelerometer concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToAccelerometer(AccelerometerSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the Accelerometer concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToAccelerometer(AccelerometerSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the Accelerometer concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<Accelerometer> queryAllAccelerometer() throws KPIModelException;
	/**
	 * Queries for a single Accelerometer concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public Accelerometer queryAccelerometer(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the BlackoutSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToBlackoutSensor(BlackoutSensorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the BlackoutSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToBlackoutSensor(BlackoutSensorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the BlackoutSensor concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<BlackoutSensor> queryAllBlackoutSensor() throws KPIModelException;
	/**
	 * Queries for a single BlackoutSensor concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public BlackoutSensor queryBlackoutSensor(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the FloodSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToFloodSensor(FloodSensorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the FloodSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToFloodSensor(FloodSensorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the FloodSensor concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<FloodSensor> queryAllFloodSensor() throws KPIModelException;
	/**
	 * Queries for a single FloodSensor concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public FloodSensor queryFloodSensor(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the GasSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToGasSensor(GasSensorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the GasSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToGasSensor(GasSensorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the GasSensor concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<GasSensor> queryAllGasSensor() throws KPIModelException;
	/**
	 * Queries for a single GasSensor concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public GasSensor queryGasSensor(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the HumiditySensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToHumiditySensor(HumiditySensorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the HumiditySensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToHumiditySensor(HumiditySensorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the HumiditySensor concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<HumiditySensor> queryAllHumiditySensor() throws KPIModelException;
	/**
	 * Queries for a single HumiditySensor concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public HumiditySensor queryHumiditySensor(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the LightingSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToLightingSensor(LightingSensorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the LightingSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToLightingSensor(LightingSensorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the LightingSensor concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<LightingSensor> queryAllLightingSensor() throws KPIModelException;
	/**
	 * Queries for a single LightingSensor concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public LightingSensor queryLightingSensor(String individualID) throws KPIModelException;
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
	/**
	 * Subscribe to the NoiseSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToNoiseSensor(NoiseSensorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the NoiseSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToNoiseSensor(NoiseSensorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the NoiseSensor concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<NoiseSensor> queryAllNoiseSensor() throws KPIModelException;
	/**
	 * Queries for a single NoiseSensor concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public NoiseSensor queryNoiseSensor(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the PresenceSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToPresenceSensor(PresenceSensorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the PresenceSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToPresenceSensor(PresenceSensorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the PresenceSensor concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<PresenceSensor> queryAllPresenceSensor() throws KPIModelException;
	/**
	 * Queries for a single PresenceSensor concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public PresenceSensor queryPresenceSensor(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the SmokeSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToSmokeSensor(SmokeSensorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the SmokeSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToSmokeSensor(SmokeSensorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the SmokeSensor concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<SmokeSensor> queryAllSmokeSensor() throws KPIModelException;
	/**
	 * Queries for a single SmokeSensor concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public SmokeSensor querySmokeSensor(String individualID) throws KPIModelException;
	/**
	 * Subscribe to the TemperatureSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @param individualID. An optional individual ID (might be null).
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void subscribeToTemperatureSensor(TemperatureSensorSubscription subscription, String individualID) throws KPIModelException;
	/**
	 * Unsubscribe to the TemperatureSensor concept
	 *
	 * @param subscription. The subscription object associated to the concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public void unsubscribeToTemperatureSensor(TemperatureSensorSubscription subscription) throws KPIModelException;
	/**
	 * Queries for all the TemperatureSensor concepts
	 *
	 * @return a list of all the individuals of the LightingSensor concept.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public List<TemperatureSensor> queryAllTemperatureSensor() throws KPIModelException;
	/**
	 * Queries for a single TemperatureSensor concept
	 *
	 * @param individualID. The ID of the queried individual.
	 * @return the concept individual or null.
	 * @throws KPIModelException. If an error occurs during publishing.
	 */
	public TemperatureSensor queryTemperatureSensor(String individualID) throws KPIModelException;

}
