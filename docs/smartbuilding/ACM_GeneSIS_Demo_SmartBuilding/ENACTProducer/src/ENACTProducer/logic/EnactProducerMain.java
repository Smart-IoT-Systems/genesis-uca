package ENACTProducer.logic;

import java.io.IOException;
import java.util.Observer;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.smool.kpi.model.exception.KPIModelException;

import ENACTProducer.api.Consumer;
import ENACTProducer.api.MessageReceiveSensorSubscription;
import ENACTProducer.api.Producer;
import ENACTProducer.api.SmoolKP;
import ENACTProducer.model.smoolcore.IMessage;
import ENACTProducer.model.smoolcore.impl.Message;
import ENACTProducer.model.smoolcore.impl.MessageReceiveSensor;

import ENACTProducer.logic.ACM_GeneSIS_Demo_Common;

/**
 * Producer of data for the Smart Building use case for Enact.
 * <p>
 * NOTE: although this app is mainly a producer of sensors data, it can receive
 * actuation orders.
 * </p>
 * <p>
 * This app is sending (via SMOOL) the temperature, gas, etc stataus in the
 * smart building. It is also receiving (via SMOOL) actuation orders, and checking if security data is valid and then it calls
 * the Smart Building SCADA to tune the values.
 * </p>
 *
 */
public class EnactProducerMain implements MqttCallback{
	
	private IMqttClient publisher; 
	

	public EnactProducerMain(String sib, String addr, int port) throws Exception {
		SmoolKP.setKPName(ACM_GeneSIS_Demo_Common.name);
		System.out.println("KPName*** " + ACM_GeneSIS_Demo_Common.name + " *** Vendor:" + ACM_GeneSIS_Demo_Common.vendor);
		// ---------------------------CONNECT TO SMOOL---------------------
		// SmoolKP.connect();
		// SmoolKP.connect("sib1", "172.24.5.151", 23000);
		// SmoolKP.connect("sib1", "192.168.1.128", 23000);
		SmoolKP.connect(sib, addr, port);
		
		//Create an MQTT client here
		initMQTTclient();


		// ---------------------------CREATE SENSOR-----------------------------
		Producer producer = SmoolKP.getProducer();
		
		ACM_GeneSIS_Demo_Common.sensor_luminance = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.sensor_luminance_id);
		ACM_GeneSIS_Demo_Common.camera_detection = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.camera_detection_id);
		ACM_GeneSIS_Demo_Common.botvacD3_state = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_state_id);
		ACM_GeneSIS_Demo_Common.botvacD3_action = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_action_id);
		ACM_GeneSIS_Demo_Common.botvacD3_is_docked = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_is_docked_id);
		ACM_GeneSIS_Demo_Common.botvacD3_is_scheduled = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_is_scheduled_id);
		ACM_GeneSIS_Demo_Common.botvacD3_is_charging = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_is_charging_id);
		ACM_GeneSIS_Demo_Common.botvacD3_command = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_command_id);
		ACM_GeneSIS_Demo_Common.microphone_record = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_record_id);
		ACM_GeneSIS_Demo_Common.microphone_sound = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_sound_id);
		ACM_GeneSIS_Demo_Common.microphone_zcr = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_zcr_id);
		ACM_GeneSIS_Demo_Common.microphone_mfcc = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_mfcc_id);
		ACM_GeneSIS_Demo_Common.microphone_time = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_time_id);
		ACM_GeneSIS_Demo_Common.microphone_replay = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_replay_id);
		ACM_GeneSIS_Demo_Common.sensors_cec_status = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.sensors_cec_status_id);
		ACM_GeneSIS_Demo_Common.sensors_cec_source = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.sensors_cec_source_id);
		ACM_GeneSIS_Demo_Common.actuators_cec_source = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.actuators_cec_source_id);		
		ACM_GeneSIS_Demo_Common.actuators_cec_power = new MessageReceiveSensor(ACM_GeneSIS_Demo_Common.actuators_cec_power_id);		
		
//		switch_binary = new MessageSendActuator(name + "_switch_binary");


		// ---------------------------PRODUCE DATA----------------------------------
		String timestamp = Long.toString(System.currentTimeMillis());

		Message msg = new Message();
		msg.setBody("init");
		msg.setTimestamp(timestamp);

		
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.sensor_luminance._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.camera_detection._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_state._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_action._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_is_docked._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_is_scheduled._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_is_charging._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_command._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_record._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_sound._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_zcr._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_mfcc._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_time._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_replay._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.sensors_cec_status._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.sensors_cec_source._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.actuators_cec_source._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(ACM_GeneSIS_Demo_Common.actuators_cec_power._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		

		// ---------------------------CONSUME ACTION----------------------------------
		Consumer consumer = SmoolKP.getConsumer();
		
//		MessageReceiveSensorSubscription sensor_luminance_subscription = new MessageReceiveSensorSubscription();
//		consumer.subscribeToMessageReceiveSensor(sensor_luminance_subscription, sensor_luminance._getIndividualID());
		
		MessageReceiveSensorSubscription subscription = new MessageReceiveSensorSubscription(createObserver());
		
		consumer.subscribeToMessageReceiveSensor(subscription, ACM_GeneSIS_Demo_Common.botvacD3_command._getIndividualID());
		consumer.subscribeToMessageReceiveSensor(subscription, ACM_GeneSIS_Demo_Common.microphone_record._getIndividualID());
		consumer.subscribeToMessageReceiveSensor(subscription, ACM_GeneSIS_Demo_Common.microphone_replay._getIndividualID());
		consumer.subscribeToMessageReceiveSensor(subscription, ACM_GeneSIS_Demo_Common.actuators_cec_source._getIndividualID());
		consumer.subscribeToMessageReceiveSensor(subscription, ACM_GeneSIS_Demo_Common.actuators_cec_power._getIndividualID());
		
//		LightSwitchActuatorSubscription subscription_switch_binary = new LightSwitchActuatorSubscription(createObserver_switch_binary());
//		consumer.subscribeToLightSwitchActuator(subscription_switch_binary, switch_binary._getIndividualID());		
		
		// ---------------------SEND DATA for a testing purpose--------------------------------------------
////		while (true) {
//			Thread.sleep(10000);
//			timestamp = Long.toString(System.currentTimeMillis());
//			Message message = new Message();
//			message.setBody("test");
//			producer.updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.sensor_luminance._getIndividualID(), ACM_GeneSIS_Demo_Common.name, null, null, null, null, message, null);
//			System.out.println("Producing " + ACM_GeneSIS_Demo_Common.sensor_luminance._getIndividualID() + " (and more concepts)");
//			producer.updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.camera_detection._getIndividualID(), ACM_GeneSIS_Demo_Common.name, null, null, null, null, message, null);
//			System.out.println("Producing " + ACM_GeneSIS_Demo_Common.camera_detection._getIndividualID() + " (and more concepts)");
//		}
		
		// -----------ATTACH WATCHDOG instead of SLEEP-------
		SmoolKP.watchdog(3600); // maximum interval that at least one message should arrive
	}
	
	private void initMQTTclient() throws InterruptedException {
		try{
        	// ---------------------------MQTT Client----------------------------------
    		String publisherId = UUID.randomUUID().toString();
    		//Connect to the MQTT broker that is connected to HomeIO app
    		//publisher = new MqttClient("tcp://192.168.1.28:1883", publisherId);
    		publisher = new MqttClient("tcp://localhost:1883", publisherId);
//    		publisher = new MqttClient("tcp://10.0.0.13:1883", publisherId);
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
			publisher.setCallback(this);
            publisher.connect(options);
			System.out.println("Connected to the broker to SMART BUILDING");
			
//			MqttTopic topic = publisher.getTopic(sensor_luminance_topic);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.sensor_luminance_topic, 0);
//			topic = publisher.getTopic(camera_detection_topic);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.camera_detection_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.botvacD3_state_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.botvacD3_action_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.botvacD3_is_docked_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.botvacD3_is_scheduled_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.botvacD3_is_charging_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.microphone_sound_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.microphone_zcr_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.microphone_mfcc_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.microphone_time_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.sensors_cec_status_topic, 0);
			publisher.subscribe(ACM_GeneSIS_Demo_Common.sensors_cec_source_topic, 0);
			
		}catch(MqttException e){
//        	e.printStackTrace();
			Thread.sleep(5000);
			System.out.println("WAITING for the CONNECTION to the broker to SMART BUILDING...");
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
	 @Override
    public void connectionLost(Throwable t) {
        System.out.println("Connection to SMART BUILDING lost!");
        
        // code to reconnect to the broker would go here if desired
        try {
        	System.out.println("RECONNECTING to the broker to SMART BUILDING...");
			initMQTTclient();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
		//Sending sensor data from SMART BUILDING to SMOOL
        	
		String message_payload = new String(mqttMessage.getPayload());
		System.out.println("-------------------------------------------------");
		System.out.println("Forwarding to SMOOL: " + s + " = " + message_payload);
		System.out.println("-------------------------------------------------");
		
		String timestamp = Long.toString(System.currentTimeMillis());
		
		Message msg = new Message();
		msg.setBody(message_payload);
		msg.setTimestamp(timestamp);
		
		if(s.equals(ACM_GeneSIS_Demo_Common.sensor_luminance_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.sensor_luminance._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.camera_detection_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.camera_detection._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.botvacD3_state_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_state._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.botvacD3_action_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_action._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.botvacD3_is_docked_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_is_docked._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.botvacD3_is_scheduled_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_is_scheduled._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.botvacD3_is_charging_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.botvacD3_is_charging._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.microphone_sound_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_sound._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.microphone_zcr_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_zcr._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.microphone_mfcc_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_mfcc._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.microphone_time_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.microphone_time._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.sensors_cec_status_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.sensors_cec_status._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}
		
		if(s.equals(ACM_GeneSIS_Demo_Common.sensors_cec_source_topic)){
			SmoolKP.getProducer().updateMessageReceiveSensor(ACM_GeneSIS_Demo_Common.sensors_cec_source._getIndividualID(), ACM_GeneSIS_Demo_Common.name, ACM_GeneSIS_Demo_Common.vendor, null, null, null, msg, null);
		}

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//        try {
//			System.out.println("Forwarding to SMOOL completed" + new String(iMqttDeliveryToken.getMessage().getPayload()));
//		} catch (MqttException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }

    /**
	 * Processs messages related to any Actuator of ACM_GeneSIS_Demo
	 */
	private Observer createObserver() {
		return (o, concept) -> {
			MessageReceiveSensor actuator = (MessageReceiveSensor) concept;
			IMessage msg_actuator = actuator.getMessage();
			
			//ONLY If the actuator does belong to name="ACM_GeneSIS_Demo", we will process further
			if(actuator.getDeviceID().equals(ACM_GeneSIS_Demo_Common.name)) {
				
				System.out.println(actuator.getDeviceID() + " receiving Actuator Message order. Value: " + msg_actuator.getBody());
				
				if(msg_actuator.getBody() != null){
					MqttMessage msg=null;
					
					msg = new MqttMessage(msg_actuator.getBody().getBytes());
					
					if(msg!=null){
						msg.setQos(0);
						msg.setRetained(true);
						
						try {
							if(publisher.isConnected()) {
								//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
								System.out.println("-------------------------------------------------");
								System.out.println("Is Forwarding Actuation Cmd to SMART BUILDING: " + new String(msg.getPayload()));
								System.out.println("-------------------------------------------------");
								
								if(actuator._getIndividualID().equals(ACM_GeneSIS_Demo_Common.microphone_record_id)) {
									publisher.publish(ACM_GeneSIS_Demo_Common.microphone_record_topic, msg);
								}
								
								if(actuator._getIndividualID().equals(ACM_GeneSIS_Demo_Common.botvacD3_command_id)) {
									publisher.publish(ACM_GeneSIS_Demo_Common.botvacD3_command_topic, msg);
								}
								
								if(actuator._getIndividualID().equals(ACM_GeneSIS_Demo_Common.microphone_replay_id)) {
									publisher.publish(ACM_GeneSIS_Demo_Common.microphone_replay_topic, msg);
								}
								
								if(actuator._getIndividualID().equals(ACM_GeneSIS_Demo_Common.actuators_cec_source_id)) {
									publisher.publish(ACM_GeneSIS_Demo_Common.actuators_cec_source_topic, msg);
								}
								
								if(actuator._getIndividualID().equals(ACM_GeneSIS_Demo_Common.actuators_cec_power_id)) {
									publisher.publish(ACM_GeneSIS_Demo_Common.actuators_cec_power_topic, msg);
								}
								
							}else {
								System.out.println("Cannot send to SMART BUILDING: publisher.isConnected() = " + publisher.isConnected());
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
				new EnactProducerMain(sib, addr, port);
			} catch (KPIModelException | IOException e) {
				e.printStackTrace();
				Thread.sleep(1000);
				System.out.println("RECONNECTING");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
