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
import ENACTConsumer.model.smoolcore.IMessage;
import ENACTConsumer.model.smoolcore.impl.Message;
import ENACTConsumer.model.smoolcore.impl.MessageReceiveSensor;
import ENACTConsumer.api.MessageReceiveSensorSubscription;
import ENACTConsumer.api.Producer;

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
public class EnactConsumerMain implements MqttCallback {
	public static final String vendor = "CNRS";
	public static final String name = "ACM_GeneSIS_Demo";

	public static MessageReceiveSensor microphone_replay;
	public static MessageReceiveSensor botvacD3_command;
	public static MessageReceiveSensor sensor_cec_source;
	public static MessageReceiveSensor cec_power;
	public static MessageReceiveSensor microphone_record;
	public static MessageReceiveSensor x_sensor_luminance;
	public static MessageReceiveSensor camera_detection;
	public static MessageReceiveSensor botvacD3_state;
	public static MessageReceiveSensor botvacD3_action;
	public static MessageReceiveSensor botvacD3_is_docked;
	public static MessageReceiveSensor botvacD3_is_scheduled;
	public static MessageReceiveSensor botvacD3_is_charging;
	public static MessageReceiveSensor microphone_sound;
	public static MessageReceiveSensor microphone_zcr;
	public static MessageReceiveSensor microphone_mfcc;
	public static MessageReceiveSensor microphone_time;
	public static MessageReceiveSensor cec_status;
	public static MessageReceiveSensor cec_source;	


	//for brokering to NodeRED apps
	public IMqttClient publisher; 
    
    private SecurityAuthorization sec;
    private int counter=0;	
    //hard code the JWT token for now
  	private final String token = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJFbmFjdENvbnN1bWVyIiwib2JqIjoiQmxpbmRQb3NpdGlvbkFjdHVhdG9yIiwiYWN0Ijoid3JpdGUiLCJpYXQiOjE1OTc3NTE1NzEsImV4cCI6MTU5Nzc1MjE3MX0.";
  	

	public EnactConsumerMain(String sib, String addr, int port) throws Exception {

		SmoolKP.setKPName(name);
		System.out.println("*** " + name + " ***");
		// ------------PREPARE ACTUATION OBJECT--------------------
//		actuation = new CustomActuation(name);

		// ---------------------------CONNECT TO SMOOL---------------------

		SmoolKP.connect(sib, addr, port);
		
		//Create an MQTT client here
		initMQTTclient();

		// ---------------------------DATA----------------------
		Consumer consumer = SmoolKP.getConsumer();
		microphone_replay = new MessageReceiveSensor(name + "_microphone_replay");
		botvacD3_command = new MessageReceiveSensor(name + "_botvacD3_command");
		sensor_cec_source = new MessageReceiveSensor(name + "_cec_source");
		cec_power = new MessageReceiveSensor(name + "_cec_power");
		microphone_record = new MessageReceiveSensor(name + "_microphone_record");

		x_sensor_luminance= new MessageReceiveSensor(name + "_x_sensor_luminance");
		camera_detection= new MessageReceiveSensor(name + "_camera_detection");
		botvacD3_state= new MessageReceiveSensor(name + "_botvacD3_state");
		botvacD3_action= new MessageReceiveSensor(name + "_botvacD3_action");
		botvacD3_is_docked= new MessageReceiveSensor(name + "_botvacD3_is_docked");
		botvacD3_is_scheduled= new MessageReceiveSensor(name + "_botvacD3_is_scheduled");
		botvacD3_is_charging= new MessageReceiveSensor(name + "_botvacD3_is_charging");
		microphone_sound= new MessageReceiveSensor(name + "_microphone_sound");
		microphone_zcr= new MessageReceiveSensor(name + "_microphone_zcr");
		microphone_mfcc= new MessageReceiveSensor(name + "_microphone_mfcc");
		microphone_time= new MessageReceiveSensor(name + "_microphone_time");
		cec_status= new MessageReceiveSensor(name + "_cec_status");
		sensor_cec_source= new MessageReceiveSensor(name + "_sensor_cec_source");


		String timestamp = Long.toString(System.currentTimeMillis());

		Message msg = new Message();
		msg.setBody("Test");
		msg.setTimestamp(timestamp);

		Producer producer = SmoolKP.getProducer();

		//Initialization produce data
		producer.createMessageReceiveSensor(microphone_replay._getIndividualID(), name, vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(botvacD3_command._getIndividualID(), name, vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(cec_power._getIndividualID(), name, vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(sensor_cec_source._getIndividualID(), name, vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(microphone_record._getIndividualID(), name, vendor, null, null, null, msg, null);

		
		//initialization receive data
		MessageReceiveSensorSubscription subscription_x_sensor_luminance = new MessageReceiveSensorSubscription(createObserver_subscription_x_sensor_luminance());
		consumer.subscribeToMessageReceiveSensor(subscription_x_sensor_luminance, x_sensor_luminance._getIndividualID());

		MessageReceiveSensorSubscription subscription_camera_detection = new MessageReceiveSensorSubscription(createObserver_subscription_camera_detection());
		consumer.subscribeToMessageReceiveSensor(subscription_camera_detection, camera_detection._getIndividualID());

		MessageReceiveSensorSubscription subscription_botvacD3_state = new MessageReceiveSensorSubscription(createObserver_subscription_botvacD3_state());
		consumer.subscribeToMessageReceiveSensor(subscription_botvacD3_state, botvacD3_state._getIndividualID());

		MessageReceiveSensorSubscription subscription_botvacD3_action = new MessageReceiveSensorSubscription(createObserver_subscription_botvacD3_action());
		consumer.subscribeToMessageReceiveSensor(subscription_botvacD3_action, botvacD3_action._getIndividualID());

		MessageReceiveSensorSubscription subscription_botvacD3_is_docked = new MessageReceiveSensorSubscription(createObserver_subscription_botvacD3_is_docked());
		consumer.subscribeToMessageReceiveSensor(subscription_botvacD3_is_docked, botvacD3_is_docked._getIndividualID());

		MessageReceiveSensorSubscription subscription_botvacD3_is_scheduled = new MessageReceiveSensorSubscription(createObserver_subscription_botvacD3_is_scheduled());
		consumer.subscribeToMessageReceiveSensor(subscription_botvacD3_is_scheduled, botvacD3_is_scheduled._getIndividualID());

		MessageReceiveSensorSubscription subscription_botvacD3_is_charging = new MessageReceiveSensorSubscription(createObserver_subscription_botvacD3_is_charging());
		consumer.subscribeToMessageReceiveSensor(subscription_botvacD3_is_charging, botvacD3_is_charging._getIndividualID());

		MessageReceiveSensorSubscription subscription_microphone_sound = new MessageReceiveSensorSubscription(createObserver_subscription_microphone_sound());
		consumer.subscribeToMessageReceiveSensor(subscription_microphone_sound, microphone_sound._getIndividualID());

		MessageReceiveSensorSubscription subscription_microphone_zcr = new MessageReceiveSensorSubscription(createObserver_subscription_microphone_zcr());
		consumer.subscribeToMessageReceiveSensor(subscription_microphone_zcr, microphone_zcr._getIndividualID());

		MessageReceiveSensorSubscription subscription_microphone_mfcc = new MessageReceiveSensorSubscription(createObserver_subscription_microphone_mfcc());
		consumer.subscribeToMessageReceiveSensor(subscription_microphone_mfcc, microphone_mfcc._getIndividualID());

		MessageReceiveSensorSubscription subscription_microphone_time = new MessageReceiveSensorSubscription(createObserver_subscription_microphone_time());
		consumer.subscribeToMessageReceiveSensor(subscription_microphone_time, microphone_time._getIndividualID());

		MessageReceiveSensorSubscription subscription_cec_status = new MessageReceiveSensorSubscription(createObserver_subscription_cec_status());
		consumer.subscribeToMessageReceiveSensor(subscription_cec_status, cec_status._getIndividualID());

		MessageReceiveSensorSubscription subscription_cec_source = new MessageReceiveSensorSubscription(createObserver_subscription_cec_source());
		consumer.subscribeToMessageReceiveSensor(subscription_cec_source, sensor_cec_source._getIndividualID());

		/*while (true) {
			Thread.sleep(1000);
			timestamp = Long.toString(System.currentTimeMillis());
			Message message = new Message();
			message.setBody("test");
			producer.updateMessageReceiveSensor(microphone_record._getIndividualID(), name, null, null, null, null, message, null);
			System.out.println("Producing " + microphone_record._getIndividualID() + " (and more concepts)");
			Message message2 = new Message();
			message2.setBody("test2");
			producer.updateMessageReceiveSensor(botvacD3_command._getIndividualID(), name, null, null, null, null, message2, null);
			System.out.println("Producing " + botvacD3_command._getIndividualID() + " (and more concepts)");
		}*/
/Users/ferrynico/Documents/Code/GeneSIS-gitlab/GeneSIS/docs/smartbuilding/ACM_GeneSIS_Demo_SmartBuilding/ENACTConsumer/thingml/Main_SmoolJava_EnactWrapper.thingml

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
		
		// -----------ATTACH WATCHDOG instead of SLEEP-------
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
			// Receive actuation orders and publish them on SMOOL
    		String publisherId = UUID.randomUUID().toString();
    		publisher = new MqttClient("tcp://localhost:1883", publisherId);
    		
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
			publisher.setCallback(this);
            publisher.connect(options);
			System.out.println("Connected to the Apps broker");

			// All subscriptions
			String myTopic = "enact/sensors/microphone/replay";
			MqttTopic topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

			myTopic = "enact/actuators/neato/botvacD3/command";
			topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

			myTopic = "enact/actuators/cec/source";
			topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

			myTopic = "enact/actuators/cec/power";
			topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

			myTopic = "enact/actuators/microphone/record";
			topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);
		
            
		}catch(MqttException e){
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

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    	
		//sec.setData(token).setTimestamp(Long.toString(System.currentTimeMillis()));
		//blindPos.setSecurityData(sec).setTimestamp(Long.toString(System.currentTimeMillis()));
    	
    	//we should send it to Smool Server
		String value_blind=new String(mqttMessage.getPayload());
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + value_blind);
        System.out.println("-------------------------------------------------");
        
		String timestamp = Long.toString(System.currentTimeMillis());
			
			String message_payload = new String(mqttMessage.getPayload());
			Message msg_with_timestamp = new Message();
			msg_with_timestamp.setBody(message_payload);
			msg_with_timestamp.setTimestamp(timestamp);

		switch(s){
			case "enact/actuators/neato/botvacD3/command":
				SmoolKP.getProducer().updateMessageReceiveSensor(botvacD3_command._getIndividualID(), name, vendor, null, null, null, msg_with_timestamp, null);
				break;
			case "enact/actuators/microphone/record":
				SmoolKP.getProducer().updateMessageReceiveSensor(microphone_record._getIndividualID(), name, vendor, null, null, null, msg_with_timestamp, null);
				break;
			case "enact/sensors/microphone/replay":
				SmoolKP.getProducer().updateMessageReceiveSensor(microphone_replay._getIndividualID(), name, vendor, null, null, null, msg_with_timestamp, null);
				break;
			case "enact/actuators/cec/source":
				SmoolKP.getProducer().updateMessageReceiveSensor(sensor_cec_source._getIndividualID(), name, vendor, null, null, null, msg_with_timestamp, null);
				break;
			case "enact/actuators/cec/power":
				SmoolKP.getProducer().updateMessageReceiveSensor(cec_power._getIndividualID(), name, vendor, null, null, null, msg_with_timestamp, null);
				break;
			default:
				System.out.println("Unknown topic");
		}
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        //System.out.println("Pub complete" + new String(token.getMessage().getPayload()));

    }


    /**
	FROM SMOOL to APP
	 */

    private Observer createObserver_subscription_x_sensor_luminance() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_x_sensor_luminance = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_x_sensor_luminance.getMessage();

			if(sensor_x_sensor_luminance.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving sensor Message. Value: " + msg_switch_binary.getBody());
			}

			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/zwave/doors/x/sensor_luminance", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}

		};
	}

	private Observer createObserver_subscription_camera_detection() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_camera_detection = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_camera_detection.getMessage();

			if(sensor_camera_detection.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}

			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/camera/detection", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}

		};
	}

	private Observer createObserver_subscription_botvacD3_state() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_botvacD3_state = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_botvacD3_state.getMessage();

			if(sensor_botvacD3_state.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}

			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/neato/botvacD3/state", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}


	private Observer createObserver_subscription_botvacD3_action() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_botvacD3_action = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_botvacD3_action.getMessage();

			if(sensor_botvacD3_action.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}
			
			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/neato/botvacD3/action", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}

	private Observer createObserver_subscription_botvacD3_is_docked() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_botvacD3_is_docked = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_botvacD3_is_docked.getMessage();

			if(sensor_botvacD3_is_docked.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}

			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/neato/botvacD3/is-docked", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}

	private Observer createObserver_subscription_botvacD3_is_scheduled() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_botvacD3_is_scheduled = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_botvacD3_is_scheduled.getMessage();

			if(sensor_botvacD3_is_scheduled.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}

			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/neato/botvacD3/is-scheduled", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}

	private Observer createObserver_subscription_botvacD3_is_charging() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_botvacD3_is_charging = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_botvacD3_is_charging.getMessage();

			if(sensor_botvacD3_is_charging.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}

			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/neato/botvacD3/is-charging", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}

	private Observer createObserver_subscription_microphone_sound() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_microphone_sound = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_microphone_sound.getMessage();

			if(sensor_microphone_sound.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}
			
			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/microphone/sound", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}

	private Observer createObserver_subscription_microphone_zcr() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_microphone_zcr = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_microphone_zcr.getMessage();

			if(sensor_microphone_zcr.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}

			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/microphone/zcr", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}

	private Observer createObserver_subscription_microphone_mfcc() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_microphone_mfcc = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_microphone_mfcc.getMessage();

			if(sensor_microphone_mfcc.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}
			
			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/microphone/mfcc", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}

	private Observer createObserver_subscription_microphone_time() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_microphone_time = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_microphone_time.getMessage();

			if(sensor_microphone_time.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}

			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/microphone/time", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}

	private Observer createObserver_subscription_cec_status() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_cec_status = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_cec_status.getMessage();

			if(sensor_cec_status.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}

			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/cec/status", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}

	private Observer createObserver_subscription_cec_source() {
		return (o, concept) -> {
			MessageReceiveSensor sensor_cec_source = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = sensor_cec_source.getMessage();

			if(sensor_cec_source.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}

			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding sensor message to APPs: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/sensors/cec/source", msg);
							
						}else {
							System.out.println("Cannot send to Apps: publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				}
			}
		};
	}



	public static void main(String[] args) throws Exception {
		String sib = args.length > 0 ? args[0] : "sib1";
		String addr = args.length > 1 ? args[1] : "15.236.132.74";
		int port = args.length > 2 ? Integer.valueOf(args[2]) : 23000;
		while (true) {
			try {
				new EnactConsumerMain(sib, addr, port);
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
