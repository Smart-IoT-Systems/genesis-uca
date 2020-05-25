package ENACTConsumer.logic;

import java.io.IOException;
import java.util.Observer;
import java.util.UUID;

import org.smool.kpi.model.exception.KPIModelException;

import ENACTConsumer.api.*;
import ENACTConsumer.api.TemperatureSensorSubscription;
import ENACTConsumer.model.smoolcore.IContinuousInformation;
import ENACTConsumer.model.smoolcore.impl.*;
import java.nio.ByteBuffer; 


import org.eclipse.paho.client.mqttv3.*;

/**
 * Subscribe to data generated from KUBIK smart building.
 * <p>
 * This class should be the skeleton fot the IoT application int the SB use case
 * </p>
 * <p>
 * The app is retrieving temperature and other data. When temp is higher or
 * lower than comfort, an actuation order is sent back to the Smart Building to
 * turn the temperature back to normal.
 * </p>
 *
 */
public class SmartEnergyApp_ConsumerMain implements MqttCallback {
	public static final String name = "EnactConsumer" + System.currentTimeMillis() % 10000;
    public static final String vendor = "Tecnalia";
	
	public IMqttClient publisher; 

    private ContinuousInformation blindPos;

	public SmartEnergyApp_ConsumerMain(String sib, String addr, int port) throws Exception {

		SmoolKP.setKPName(name);
		System.out.println("*** " + name + " ***");

		// ---------------------------CONNECT TO SMOOL---------------------
		// SmoolKP.connect();
		// SmoolKP.connect("sib1", "172.24.5.151", 23000);
		SmoolKP.connect(sib, addr, port);
		
		//Create an MQTT client here
		initMQTTclient();

		// ---------------------------SUBSCRIBE TO DATA----------------------
		Consumer consumer = SmoolKP.getConsumer();

		TemperatureSensorSubscription tempSubscription = new TemperatureSensorSubscription(createTemperatureObserver());
		consumer.subscribeToTemperatureSensor(tempSubscription, null);

		SmokeSensorSubscription smokeSubscription = new SmokeSensorSubscription(createSmokeObserver());
		consumer.subscribeToSmokeSensor(smokeSubscription, null);

		GasSensorSubscription gasSubscription = new GasSensorSubscription(createGasObserver());
		consumer.subscribeToGasSensor(gasSubscription, null);

		LightingSensorSubscription lightSubscription = new LightingSensorSubscription(createLightObserver());
		consumer.subscribeToLightingSensor(lightSubscription, null);

        blindPos = new ContinuousInformation(name + "_blindPosition");
        SmoolKP.getProducer().createBlindPositionActuator(SmartEnergyApp_ConsumerMain.name+"_actuator", SmartEnergyApp_ConsumerMain.name, SmartEnergyApp_ConsumerMain.vendor, null, blindPos, null);


		// -----------ATTACH WATCHDOG instead of SLEEP-------
		// Thread.sleep(Long.MAX_VALUE); // keep application alive.
		SmoolKP.watchdog(5 * 60); // 5 min is the maximum interval that at least one message should arrive

	}
	
	/**
	 * Create an MQTT client here just for testing purpose: 
	 * Whenever there is a message from SMOOL send it via MQQT to apps
	 * Whenever there is an actuattion order from app via MQTT send it to SMOOL. 
	 */
	private void initMQTTclient() {
		try{
        	// ---------------------------MQTT Client----------------------------------
    		String publisherId = UUID.randomUUID().toString();
    		publisher = new MqttClient("tcp://127.0.0.1:1883", publisherId);
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
			publisher.setCallback(this);
            publisher.connect(options);
			System.out.println("Connected to broker");


			// All subscriptions for actuation orders
			String myTopic = "/home/A/Output/float/Lights_(Analog)";
			MqttTopic topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

			myTopic = "/home/A/Output/float/Heater_(Analog)";
			topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

			myTopic = "/home/A/Output/bool/Roller_Shades_1_(Up)";
			topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

			myTopic = "/home/A/Output/bool/Roller_Shades_1_(Down)";
			topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

            
        }catch(Exception e){
            throw new RuntimeException("Exception occured in creating MQTT Client");
        }
	}

	/**
	FROM APP to SMOOL
	*/
	 public void connectionLost(Throwable t) {
        System.out.println("Connection lost!");
        // code to reconnect to the broker would go here if desired
    }

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
		//we should send it to smmol
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + new String(mqttMessage.getPayload()));
        System.out.println("-------------------------------------------------");
        if(s.equals("home/A/Output/bool/Roller_Shades_1_(Down)")){
            blindPos.setValue(ByteBuffer.wrap(mqttMessage.getPayload()).getDouble()); // we can also send temp, so the SCADA will contain the temp-to-blind rule
            SmoolKP.getProducer().updateBlindPositionActuator(SmartEnergyApp_ConsumerMain.name+"_actuator", SmartEnergyApp_ConsumerMain.name, "TECNALIA", null, blindPos, null);
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        //System.out.println("Pub complete" + new String(token.getMessage().getPayload()));

    }



	/**
	FROM SMOOL to APP
	 */


    private Observer createBlindObserver(){
		return (o, concept) -> {
			BlindPositionActuator actuator = (BlindPositionActuator) concept;
			IContinuousInformation info = actuator.getBlindPos();
			System.out.println("receiving ACTUATION order on blinds. Value: " + info.getValue());
		};
	}

	private Observer createTemperatureObserver() {
		return (o, concept) -> {
			TemperatureSensor sensor = (TemperatureSensor) concept;
			Double temp = sensor.getTemperature().getValue();
			System.out.println("temp  from " + sensor._getIndividualID() + ": " + temp);
			//forwarding the sensor data to the SmartEnergyApp via MQTT
			
			MqttMessage msg = new MqttMessage((temp+"").getBytes());
			
	        msg.setQos(0);
	        msg.setRetained(true);
	        
	        try {
	        	if(publisher.isConnected()) {
	        		//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
	        		publisher.publish("/home/A/Input/float/Thermostat_(Room_Temperature)", msg);
	        	}else {
	        		System.out.println("publisher.isConnected() = " + publisher.isConnected());
	        	}
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}     
		};
	}

	private Observer createSmokeObserver() {
		return (o, concept) -> {
			SmokeSensor sensor = (SmokeSensor) concept;
			System.out.println("smoke  from " + sensor._getIndividualID() + ": " + sensor.getSmoke().getStatus());

			MqttMessage msg = new MqttMessage((sensor.getSmoke().getStatus()+"").getBytes());
			
	        msg.setQos(0);
	        msg.setRetained(true);
	        
	        try {
	        	if(publisher.isConnected()) {
	        		//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
	        		publisher.publish("/home/A/Input/bool/Smoke_Detector", msg);
	        	}else {
	        		System.out.println("publisher.isConnected() = " + publisher.isConnected());
	        	}
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}     
		};
	}

	private Observer createGasObserver() {
		return (o, concept) -> {
			GasSensor sensor = (GasSensor) concept;
			String t = sensor.getGas().getType();
			System.out.println("gas " + t + "  from " + sensor._getIndividualID() + ": " + sensor.getGas().getValue());
		};
	}

	private Observer createLightObserver() {
		return (o, concept) -> {
			LightingSensor sensor = (LightingSensor) concept;
			
			MqttMessage msg = new MqttMessage((sensor.getLighting().getValue()+"").getBytes());
			
	        msg.setQos(0);
	        msg.setRetained(true);
	        
	        try {
	        	if(publisher.isConnected()) {
	        		//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
	        		publisher.publish("/home/A/Input/float/Brightness_Sensor_(Analog)", msg);
	        	}else {
	        		System.out.println("publisher.isConnected() = " + publisher.isConnected());
	        	}
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}     
		};
	}

	public static void main(String[] args) throws Exception {
		String sib = args.length > 0 ? args[0] : "sib1";
		String addr = args.length > 1 ? args[1] : "15.236.132.74";
		int port = args.length > 2 ? Integer.valueOf(args[2]) : 23000;
		// Logger.setDebugging(true);
		// Logger.setDebugLevel(4);
		while (true) {
			try {
				new SmartEnergyApp_ConsumerMain(sib, addr, port);
			} catch (KPIModelException | IOException e) {
				e.printStackTrace();
				Thread.sleep(10000);
				System.out.println("RECONNECTING");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
