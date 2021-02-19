package ENACTConsumer.logic;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Observer;
import java.util.UUID;

import org.smool.kpi.model.exception.KPIModelException;

import ENACTConsumer.api.Consumer;
import ENACTConsumer.api.HumiditySensorSubscription;
import ENACTConsumer.api.LightingSensorSubscription;
import ENACTConsumer.api.PresenceSensorSubscription;
import ENACTConsumer.api.SmoolKP;
import ENACTConsumer.api.TemperatureSensorSubscription;
import ENACTConsumer.model.smoolcore.IContinuousInformation;
import ENACTConsumer.model.smoolcore.impl.BlindPositionActuator;
import ENACTConsumer.model.smoolcore.impl.ContinuousInformation;
import ENACTConsumer.model.smoolcore.impl.HumiditySensor;
import ENACTConsumer.model.smoolcore.impl.LightingSensor;
import ENACTConsumer.model.smoolcore.impl.PresenceSensor;
import ENACTConsumer.model.smoolcore.impl.SecurityAuthorization;
import ENACTConsumer.model.smoolcore.impl.SmokeSensor;
import ENACTConsumer.model.smoolcore.impl.TemperatureSensor;

import org.eclipse.paho.client.mqttv3.*;

/**
 * Subscribe to data generated from KUBIK smart building.
 * <p>
 * This class should be the skeleton for the IoT application in the SB use case
 * </p>
 * <p>
 * The app is retrieving temperature and other data. When temp is higher or
 * lower than comfort, an actuation order is sent back to the Smart Building to
 * turn the temperature back to normal.
 * </p>
 *
 */
public class SmartEnergyApp_ConsumerMain implements MqttCallback {
	public static final String name = "UserComfortApp_ENACT";// + System.currentTimeMillis() % 10000;
//	public static CustomActuation actuation;
	
	
	//for brokering to NodeRED apps
	public IMqttClient publisher; 

	private BlindPositionActuator actuator;
	private String blindActuatorName = name + "_blindActuator";
    private ContinuousInformation blindPos;
    private double val = 0;
    
    public static final String vendor = "Tecnalia";
    
    private SecurityAuthorization sec;
    private int counter=0;	
    //hard code the JWT token for now
  	private final String token = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJFbmFjdENvbnN1bWVyIiwib2JqIjoiQmxpbmRQb3NpdGlvbkFjdHVhdG9yIiwiYWN0Ijoid3JpdGUiLCJpYXQiOjE1OTc3NTE1NzEsImV4cCI6MTU5Nzc1MjE3MX0.";
  	

	public SmartEnergyApp_ConsumerMain(String sib, String addr, int port) throws Exception {

		SmoolKP.setKPName(name);
		System.out.println("*** " + name + " ***");
		// ------------PREPARE ACTUATION OBJECT--------------------
//		actuation = new CustomActuation(name);

		// ---------------------------CONNECT TO SMOOL---------------------
		// SmoolKP.connect();
		// SmoolKP.connect("sib1", "192.168.1.128", 23000);
		SmoolKP.connect(sib, addr, port);
		
		//Create an MQTT client here
		initMQTTclient();

		// ---------------------------SUBSCRIBE TO DATA----------------------
		Consumer consumer = SmoolKP.getConsumer();

		consumer.subscribeToTemperatureSensor(new TemperatureSensorSubscription(createTemperatureObserver()), null);

		consumer.subscribeToPresenceSensor(new PresenceSensorSubscription(createPresenceObserver()), null);

		consumer.subscribeToLightingSensor(new LightingSensorSubscription(createLightingObserver()), null);

		consumer.subscribeToHumiditySensor(new HumiditySensorSubscription(createHumidityObserver()), null);

		// consumer.subscribeToSmokeSensor(new
		// SmokeSensorSubscription(createSmokeObserver()), null);

		//
		// GasSensorSubscription gasSubscription = new
		// GasSensorSubscription(createGasObserver());
		// consumer.subscribeToGasSensor(gasSubscription, null);

		//For the use of NodeRED apps
		
		actuator = new BlindPositionActuator(blindActuatorName + "_actuator");
		blindPos = new ContinuousInformation(blindActuatorName + "_blindPosition");
		
		sec = new SecurityAuthorization(name + "_security"+Integer.toString(counter++));
		sec.setType("JWT + CASBIN payload");
		
		// if started as insecure, do not send credentials (>java -Dinsecure app.jar)
		if (System.getProperty("insecure") != null) {
			// IMPORTANT, send data = "" instead of null since the global list of props must
			// be sent and if old BlindSobscriptions where created with that triple and then
			// with new insecure requests the data triple is not sent, the subscriptors
			// crash and cannot reconnect until smool server is restarted.
			sec.setData("");
		} else {
			//String token = HTTPS.post("https://localhost:8443/jwt", "{\"id\":\""+kpName+"\"}");
			//String token = getTokenFromCLI(security_command, null);
			sec.setData(token);
		}

		blindPos.setValue(val).setSecurityData(sec).setTimestamp(Long.toString(System.currentTimeMillis()));
		actuator.setBlindPos(blindPos);

		SmoolKP.getProducer().createBlindPositionActuator(blindActuatorName, name, SmartEnergyApp_ConsumerMain.vendor, null, blindPos, null);
		
		
		
		// -----------ATTACH WATCHDOG instead of SLEEP-------
		// Thread.sleep(Long.MAX_VALUE); // keep application alive.
		SmoolKP.watchdog(3600); // maximum interval that at least one message should arrive

	}
	
	/**
	 * Create an MQTT client here just for testing purpose: 
	 * Whenever there is a message from SMOOL send it via MQQT to apps
	 * Whenever there is an actuation order from app via MQTT send it to SMOOL. 
	 * @throws InterruptedException 
	 */
	private void initMQTTclient() throws InterruptedException {
		try{
        	// ---------------------------MQTT Client----------------------------------
    		String publisherId = UUID.randomUUID().toString();
//    		publisher = new MqttClient("tcp://127.0.0.1:1883", publisherId);
    		publisher = new MqttClient("tcp://localhost:1883", publisherId);
    		
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
			publisher.setCallback(this);
            publisher.connect(options);
			System.out.println("Connected to the broker to Apps");


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

            
		}catch(MqttException e){
//        	e.printStackTrace();
			Thread.sleep(5000);
			System.out.println("WAITING for the CONNECTION to the broker to the Apps...");
			initMQTTclient();
            //throw new RuntimeException("Exception occurred in creating MQTT Client");
        }catch(Exception e) {
        	//Unable to connect to server (32103) - java.net.ConnectException: Connection refused
        	e.printStackTrace();
			System.exit(1);
        }
	}
	
	/**
	FROM APP to SMOOL
	*/
	 public void connectionLost(Throwable t) {
        System.out.println("Connection lost!");
        // code to reconnect to the broker would go here if desired
        try {
        	System.out.println("RECONNECTING to the broker to Apps");
			initMQTTclient();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void messageArrived(String s, MqttMessage mqttMessage) {
    	
		sec.setData(token).setTimestamp(Long.toString(System.currentTimeMillis()));
		blindPos.setSecurityData(sec).setTimestamp(Long.toString(System.currentTimeMillis()));
    	
    	//we should send it to Smool Server
		String value_blind=new String(mqttMessage.getPayload());
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + value_blind);
        System.out.println("-------------------------------------------------");
        
        if(s.equals("/home/A/Output/bool/Roller_Shades_1_(Down)")){
			System.out.println("Sending message to SMOOL");
			//Later can be intermediary level so we keep double
            if(value_blind.equals("true")){
				blindPos.setValue(100.0);
			}else{
				blindPos.setValue(0.0);
			} 

			// we can also send temp, so the SCADA will contain the temp-to-blind rule
            try {
				SmoolKP.getProducer().updateBlindPositionActuator(blindActuatorName, name, SmartEnergyApp_ConsumerMain.vendor, null, blindPos, null);
			} catch (KPIModelException e) {
				System.out.println("Error: the actuation order cannot be sent to SMOOL server. " + e.getMessage());
			}
        }

    	//IF USING CUSTOMACTUATION
//    	String value_blind=new String(mqttMessage.getPayload());
//    	
//		//we should send it to Smool
//        System.out.println("-------------------------------------------------");
//        System.out.println("| Topic:" + s);
//        System.out.println("| Message: " + new String(mqttMessage.getPayload()));
//        System.out.println("-------------------------------------------------");
//        if(s.equals("home/A/Output/bool/Roller_Shades_1_(Down)")){
//        	System.out.println("Sending message to SMOOL");
//			//Later can be intermediary level so we keep double
//        	if(value_blind.equals("true")){
//            	new Thread(() -> SmartEnergyApp_ConsumerMain.actuation.run(100.0)).start();
//			}else {
//				new Thread(() -> SmartEnergyApp_ConsumerMain.actuation.run(0.0)).start();
//			}
//        }
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
			System.out.println("The current Blind Position with Value = " + info.getValue());
		};
	}
    
    /**
     * Receive temperature data from Smool and forward it to the apps via MQTT broker
     * @return
     */
	private Observer createTemperatureObserver() {
		return (o, concept) -> {
			TemperatureSensor sensor = (TemperatureSensor) concept;
			
//			//NEW: filter data to receive only the sensor data from HOMEIO
//			if(sensor.getDeviceID().equalsIgnoreCase("EnactProducer_HomeIO_tempSensor")) {
			
				double temp = sensor.getTemperature().getValue();
				System.out.println("temp  from " + sensor._getIndividualID() + ": " + temp);
				
				/**
				 * We will not run the custom actuation here but let the Node-RED apps to send actuation orders
				*/ 
	//			if (temp > 24) {
	//				// launch an actuation order to modify the blinds position
	//				new Thread(() -> SmartEnergyApp_ConsumerMain.actuation.run(temp)).start(); // ConsumerMain.actuation.run();
	//			}
				
				
				//forwarding the sensor data to the SmartEnergyApp via MQTT
				MqttMessage msg = new MqttMessage((temp+"").getBytes());
				
		        msg.setQos(0);
		        msg.setRetained(true);
		        
		        try {
		        	if(publisher.isConnected()) {
		        		//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
		        		publisher.publish("/home/A/Input/float/Thermostat_(Room_Temperature)", msg);
		        	}else {
		        		System.out.println("Cannot send to Apps because publisher.isConnected() = " + publisher.isConnected());
		        	}
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}     
//			}
		};
	}
	
//	private Observer createSmokeObserver() {
//		return (o, concept) -> {
//			SmokeSensor sensor = (SmokeSensor) concept;
//			System.out.println("smoke  from " + sensor._getIndividualID() + ": " + sensor.getSmoke().getStatus());
//
//			MqttMessage msg = new MqttMessage((sensor.getSmoke().getStatus()+"").getBytes());
//			
//	        msg.setQos(0);
//	        msg.setRetained(true);
//	        
//	        try {
//	        	if(publisher.isConnected()) {
//	        		//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
//	        		publisher.publish("/home/A/Input/bool/Smoke_Detector", msg);
//	        	}else {
//	        		System.out.println("publisher.isConnected() = " + publisher.isConnected());
//	        	}
//			} catch (MqttPersistenceException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (MqttException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}     
//		};
//	}

	// private Observer createSmokeObserver() {
	// return (o, concept) -> {
	// SmokeSensor sensor = (SmokeSensor) concept;
	// System.out.println("smoke from " + sensor._getIndividualID() + ": " +
	// sensor.getSmoke().getStatus());
	// };
	// }
	//
	// private Observer createGasObserver() {
	// return (o, concept) -> {
	// GasSensor sensor = (GasSensor) concept;
	// String t = sensor.getGas().getType();
	// System.out.println("gas " + t + " from " + sensor._getIndividualID() + ": " +
	// sensor.getGas().getValue());
	// };
	// }

	private Observer createHumidityObserver() { 
		return (o, concept) -> {
			HumiditySensor sensor = (HumiditySensor) concept;
			System.out.println("Humidity   from " + sensor._getIndividualID() + ": "
					+ Double.toString(sensor.getHumidity().getValue()));
		};
	}

	/**
     * Receive lighting data from Smool and forward it to the apps via MQTT broker
     * @return
     */
	private Observer createLightingObserver() {
		return (o, concept) -> {
			LightingSensor sensor = (LightingSensor) concept;
			System.out.println("Light   from " + sensor._getIndividualID() + ": "
					+ Double.toString(sensor.getLighting().getValue()));
			
			MqttMessage msg = new MqttMessage((sensor.getLighting().getValue()+"").getBytes());
			
	        msg.setQos(0);
	        msg.setRetained(true);
	        
	        try {
	        	if(publisher.isConnected()) {
	        		//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
	        		publisher.publish("/home/A/Input/float/Brightness_Sensor_(Analog)", msg);
	        	}else {
	        		System.out.println("Cannot send to Apps because publisher.isConnected() = " + publisher.isConnected());
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

	private Observer createPresenceObserver() {
		return (o, concept) -> {
			PresenceSensor sensor = (PresenceSensor) concept;
			System.out.println("Presence   from " + sensor._getIndividualID() + ": "
					+ Boolean.toString(sensor.getPresence().getStatus()));
		};
	}

	public static void main(String[] args) throws Exception {
		String sib = args.length > 0 ? args[0] : "sib1";
		String addr = args.length > 1 ? args[1] : "15.236.132.74";
		int port = args.length > 2 ? Integer.valueOf(args[2]) : 23000;
//		 Logger.setDebugging(true);
//		 Logger.setDebugLevel(4);
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
